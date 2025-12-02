package xyz.crunchmunch.mods.noxesium.fabric

import com.noxcrew.noxesium.api.protocol.ClientSettings
import com.noxcrew.noxesium.api.protocol.NoxesiumFeature
import com.noxcrew.noxesium.api.protocol.NoxesiumServerManager
import com.noxcrew.noxesium.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.network.clientbound.ClientboundServerInformationPacket
import com.noxcrew.noxesium.network.clientbound.ClientboundSetExtraEntityDataPacket
import it.unimi.dsi.fastutil.ints.IntImmutableList
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import xyz.crunchmunch.mods.noxesium.fabric.event.NoxesiumPlayerEvents
import xyz.crunchmunch.mods.noxesium.fabric.rules.RemoteServerRule
import xyz.crunchmunch.mods.noxesium.fabric.rules.RuleContainer
import xyz.crunchmunch.mods.noxesium.fabric.rules.RuleFunction
import xyz.crunchmunch.mods.noxesium.fabric.rules.RuleHolder
import java.util.*

object NoxesiumFabricManager : NoxesiumServerManager<ServerPlayer> {
    val playerSettings: MutableMap<UUID, ClientSettings> = Collections.synchronizedMap(mutableMapOf())
    val usingNoxesium: MutableSet<UUID> = Collections.synchronizedSet(mutableSetOf())
    val playerNoxesiumVersions: MutableMap<UUID, PlayerNoxesiumVersions> = Collections.synchronizedMap(mutableMapOf())

    val serverRuleContainer = RuleContainer()
    val entityRuleContainer = RuleContainer()

    val entityRules: MutableMap<Entity, RuleHolder> = Collections.synchronizedMap(WeakHashMap())
    val playerRules: MutableMap<UUID, RuleHolder> = Collections.synchronizedMap(mutableMapOf())

    init {
        NoxesiumPlayerEvents.UPDATE_CLIENT_SETTINGS.register { player, settings ->
            this.playerSettings[player.uuid] = settings
        }

        NoxesiumPlayerEvents.INIT_WITH_NOXESIUM.register { player, protocolVersion, versionString ->
            NoxesiumFabricServer.logger.info("Player ${player.gameProfile.name} (${player.uuid}) logged in with Noxesium version $versionString (protocol: $protocolVersion)")

            this.playerNoxesiumVersions[player.uuid] = PlayerNoxesiumVersions(protocolVersion, versionString)

            if (protocolVersion < NoxesiumFeature.API_V2.minProtocolVersion) {
                NoxesiumFabricServer.logger.error("Player ${player.gameProfile.name} (${player.uuid}) is using an invalid protocol version!")
                return@register
            }

            this.usingNoxesium.add(player.uuid)

            NoxesiumPlayerEvents.AFTER_NOXESIUM_INIT.invoker().onPlayerEvent(player)
        }

        // TODO: make this actually support the features Noxesium adds
        ServerPlayerEvents.JOIN.register { player ->
            ServerPlayNetworking.send(player, ClientboundServerInformationPacket(NoxesiumFeature.CUSTOM_GLOW_COLOR.minProtocolVersion))
        }

        ServerPlayerEvents.LEAVE.register { player ->
            this.playerSettings.remove(player.uuid)
            this.usingNoxesium.remove(player.uuid)
        }

        EntityTrackingEvents.START_TRACKING.register { trackedEntity, player ->
            val rules = this.entityRules[trackedEntity] ?: return@register
            if (player.isUsingNoxesium) {
                val updatedRules = rules.filter { this.entityRuleContainer.isAvailable(it.key, this.getProtocolVersion(player) ?: -1) }
                    .ifEmpty { null } ?: return@register

                ServerPlayNetworking.send(player, ClientboundSetExtraEntityDataPacket(
                    trackedEntity.id,
                    IntImmutableList(updatedRules.keys.toIntArray()),
                    updatedRules.values.toList()
                ))
            }
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            synchronized(this.playerRules) {
                for ((uuid, rules) in this.playerRules) {
                    val player = server.playerList.getPlayer(uuid) ?: continue

                    if (rules.needsUpdate) {
                        updateRules(player, rules)
                    }
                }
            }

            synchronized(this.entityRules) {
                for ((entity, rules) in this.entityRules) {
                    if (!rules.needsUpdate)
                        continue

                    for (player in server.playerList.players) {
                        if (player.isUsingNoxesium) {
                            val updatedRules = rules.filter { it.value.changePending && this.entityRuleContainer.isAvailable(it.key, this.getProtocolVersion(player) ?: -1) }
                                .ifEmpty { null } ?: continue

                            ServerPlayNetworking.send(player, ClientboundSetExtraEntityDataPacket(
                                entity.id,
                                IntImmutableList(updatedRules.keys.toIntArray()),
                                updatedRules.values.toList()
                            ))
                        }
                    }

                    rules.markAllUpdated()
                }
            }
        }

        ServerEntityEvents.ENTITY_UNLOAD.register { entity, world ->
            this.entityRules.remove(entity)
        }
    }

    fun init() {}

    private fun updateRules(player: ServerPlayer, rules: RuleHolder) {
        val rulesToUpdate = rules.filter { it.value.changePending }

        ServerPlayNetworking.send(player, ClientboundChangeServerRulesPacket(
            IntImmutableList(rulesToUpdate.keys.toIntArray()), rulesToUpdate.values.toList()
        ))
    }

    val ServerPlayer.isUsingNoxesium: Boolean
        get() = usingNoxesium.contains(this.uuid)

    fun <T : Any> getServerRule(player: ServerPlayer, rule: RuleFunction<T>): RemoteServerRule<T>? = getServerRule(player, rule.index)

    override fun <T : Any> getServerRule(player: ServerPlayer, index: Int): RemoteServerRule<T>? {
        return this.playerRules.computeIfAbsent(player.uuid) { RuleHolder() }.let { holder ->
            this.serverRuleContainer.create(index, holder, this.getProtocolVersion(player) ?: -1)
        }
    }

    /** Returns the given [rule] for [entity]. */
    fun <T : Any> getEntityRule(entity: Entity, rule: RuleFunction<T>): RemoteServerRule<T>? =
        getEntityRule(entity, rule.index)

    /** Returns the given [ruleIndex] for [entity]. */
    fun <T : Any> getEntityRule(entity: Entity, ruleIndex: Int): RemoteServerRule<T>? =
        this.entityRules.computeIfAbsent(entity) { RuleHolder() }.let { holder ->
            this.entityRuleContainer.create(ruleIndex, holder)
        }

    override fun getClientSettings(player: ServerPlayer): ClientSettings? {
        return this.playerSettings[player.uuid]
    }

    override fun getClientSettings(player: UUID): ClientSettings? {
        return this.playerSettings[player]
    }

    override fun getProtocolVersion(player: ServerPlayer): Int? {
        return this.playerNoxesiumVersions[player.uuid]?.protocolVersion
    }

    override fun getExactVersion(player: ServerPlayer): String? {
        return this.playerNoxesiumVersions[player.uuid]?.exactVersion
    }

    override fun getProtocolVersion(uuid: UUID?): Int? {
        return this.playerNoxesiumVersions[uuid]?.protocolVersion
    }

    override fun getExactVersion(uuid: UUID): String? {
        return this.playerNoxesiumVersions[uuid]?.exactVersion
    }
}

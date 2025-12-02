package xyz.crunchmunch.mods.noxesium.fabric

import com.noxcrew.noxesium.network.NoxesiumPackets
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.LoggerFactory
import xyz.crunchmunch.mods.noxesium.fabric.event.NoxesiumPlayerEvents
import xyz.crunchmunch.mods.noxesium.fabric.event.QibEvents
import xyz.crunchmunch.mods.noxesium.fabric.mixin.NoxesiumModAccessor

class NoxesiumFabricServer : ModInitializer {
    override fun onInitialize() {
        initNoxesium()

        ServerPlayNetworking.registerGlobalReceiver(NoxesiumPackets.SERVER_QIB_TRIGGERED.type) { packet, ctx ->
            val entity = ctx.player().level().getEntity(packet.entityId)

            if (entity == null) {
                logger.warn("Failed to get entity ${packet.entityId} for qib triggered by player ${ctx.player().gameProfile.name}/${ctx.player().uuid} (interaction type: ${packet.qibType.name})")
                return@registerGlobalReceiver
            }

            QibEvents.TRIGGERED.invoker().onQibTriggeredEvent(ctx.player(), entity, packet.qibType)
        }

        ServerPlayNetworking.registerGlobalReceiver(NoxesiumPackets.SERVER_RIPTIDE.type) { packet, ctx ->
            // TODO: implement proper support for this
            NoxesiumPlayerEvents.RIPTIDED.invoker().onPlayerRiptide(ctx.player(), packet.slot)
        }

        ServerPlayNetworking.registerGlobalReceiver(NoxesiumPackets.SERVER_CLIENT_SETTINGS.type) { packet, ctx ->
            NoxesiumPlayerEvents.UPDATE_CLIENT_SETTINGS.invoker().onPlayerUpdateClientSettings(ctx.player(), packet.settings)
        }

        NoxesiumFabricManager.init()
    }

    private fun initNoxesium() {
        NoxesiumModAccessor.setPlatform(NoxesiumFabricPlatformHook)

        // Class load the packets so we actually register them
        logger.info("Registering Noxesium packet groups ${NoxesiumPackets.getRegisteredGroups().joinToString(",")}")
    }

    companion object {
        val logger = LoggerFactory.getLogger(NoxesiumFabricServer::class.java)
    }
}
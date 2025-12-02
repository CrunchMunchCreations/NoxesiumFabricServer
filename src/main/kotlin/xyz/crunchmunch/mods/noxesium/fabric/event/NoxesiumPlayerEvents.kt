package xyz.crunchmunch.mods.noxesium.fabric.event

import com.noxcrew.noxesium.api.protocol.ClientSettings
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.level.ServerPlayer

object NoxesiumPlayerEvents {
    @JvmField
    val RIPTIDED: Event<PlayerRiptideEvent> = EventFactory.createArrayBacked(PlayerRiptideEvent::class.java) { callbacks ->
        PlayerRiptideEvent { player, slot ->
            for (event in callbacks) {
                event.onPlayerRiptide(player, slot)
            }
        }
    }

    @JvmField
    val UPDATE_CLIENT_SETTINGS: Event<PlayerUpdateClientSettingsEvent> = EventFactory.createArrayBacked(PlayerUpdateClientSettingsEvent::class.java) { callbacks ->
        PlayerUpdateClientSettingsEvent { player, settings ->
            for (event in callbacks) {
                event.onPlayerUpdateClientSettings(player, settings)
            }
        }
    }

    @JvmField
    val INIT_WITH_NOXESIUM: Event<PlayerNoxesiumInitEvent> = EventFactory.createArrayBacked(PlayerNoxesiumInitEvent::class.java) { callbacks ->
        PlayerNoxesiumInitEvent { player, protocolVersion, versionString ->
            for (event in callbacks) {
                event.onPlayerInitWithNoxesium(player, protocolVersion, versionString)
            }
        }
    }

    @JvmField
    val AFTER_NOXESIUM_INIT: Event<GenericPlayerEvent> = createGenericEvent()

    fun createGenericEvent(): Event<GenericPlayerEvent> {
        return EventFactory.createArrayBacked(GenericPlayerEvent::class.java) { callbacks ->
            GenericPlayerEvent { player ->
                for (event in callbacks) {
                    event.onPlayerEvent(player)
                }
            }
        }
    }

    fun interface GenericPlayerEvent {
        fun onPlayerEvent(player: ServerPlayer)
    }

    fun interface PlayerRiptideEvent {
        fun onPlayerRiptide(player: ServerPlayer, slot: Int)
    }

    fun interface PlayerUpdateClientSettingsEvent {
        fun onPlayerUpdateClientSettings(player: ServerPlayer, settings: ClientSettings)
    }

    fun interface PlayerNoxesiumInitEvent {
        fun onPlayerInitWithNoxesium(player: ServerPlayer, protocolVersion: Int, versionString: String)
    }
}
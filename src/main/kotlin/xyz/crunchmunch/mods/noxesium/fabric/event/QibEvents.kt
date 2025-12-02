package xyz.crunchmunch.mods.noxesium.fabric.event

import com.noxcrew.noxesium.network.serverbound.ServerboundQibTriggeredPacket
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

object QibEvents {
    @JvmField
    val TRIGGERED = EventFactory.createArrayBacked(TriggeredQibEvent::class.java) { callbacks ->
        TriggeredQibEvent { player, qibEntity, type ->
            for (callback in callbacks) {
                callback.onQibTriggeredEvent(player, qibEntity, type)
            }
        }
    }

    fun interface TriggeredQibEvent {
        fun onQibTriggeredEvent(player: ServerPlayer, qibEntity: Entity, type: ServerboundQibTriggeredPacket.Type)
    }
}
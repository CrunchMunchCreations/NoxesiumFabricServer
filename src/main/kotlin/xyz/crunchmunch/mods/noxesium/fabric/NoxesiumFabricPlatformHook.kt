package xyz.crunchmunch.mods.noxesium.fabric

import com.noxcrew.noxesium.NoxesiumPlatformHook
import com.noxcrew.noxesium.network.NoxesiumPacket
import com.noxcrew.noxesium.network.NoxesiumPayloadType
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import java.nio.file.Path

object NoxesiumFabricPlatformHook : NoxesiumPlatformHook {
    override fun getConfigDirectory(): Path {
        return FabricLoader.getInstance().configDir
    }

    override fun isModLoaded(modName: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(modName)
    }

    override fun getNoxesiumVersion(): String {
        return "fabric-${FabricLoader.getInstance().getModContainer("noxesium-fabric").orElseThrow().metadata.version.friendlyString}"
    }

    override fun registerTickEventHandler(runnable: Runnable) {
        ServerTickEvents.END_SERVER_TICK.register { runnable.run() }
    }

    override fun registerKeyBinding(keyMapping: KeyMapping?) {
        TODO("Not yet implemented")
    }

    override fun canSend(type: NoxesiumPayloadType<*>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : NoxesiumPacket?> registerPacket(
        type: NoxesiumPayloadType<T>,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
        clientToServer: Boolean
    ) {
        (if (clientToServer)
            PayloadTypeRegistry.playC2S()
        else
            PayloadTypeRegistry.playS2C()
        )
            .register(type.type, codec)
    }

    override fun sendPacket(packet: ServerboundNoxesiumPacket) {
        TODO("Not yet implemented")
    }

    override fun <T : CustomPacketPayload?> registerReceiver(
        type: CustomPacketPayload.Type<T?>?,
        global: Boolean
    ) {
        TODO("Not yet implemented")
    }
}
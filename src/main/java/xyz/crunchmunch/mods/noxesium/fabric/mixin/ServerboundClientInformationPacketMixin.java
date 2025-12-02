package xyz.crunchmunch.mods.noxesium.fabric.mixin;

import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerboundClientInformationPacket.class)
public abstract class ServerboundClientInformationPacketMixin {
    @Redirect(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;readInt()I"))
    private static int aeltumnWhyYouDoDis(RegistryFriendlyByteBuf instance) {
        return instance.readByte();
    }
}

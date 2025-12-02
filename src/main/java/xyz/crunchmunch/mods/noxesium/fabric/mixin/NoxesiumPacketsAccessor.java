package xyz.crunchmunch.mods.noxesium.fabric.mixin;

import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(NoxesiumPackets.class)
public interface NoxesiumPacketsAccessor {
    @Accessor
    static Map<String, Pair<String, NoxesiumPayloadType<?>>> getClientboundPackets() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static Map<String, String> getServerboundPackets() {
        throw new UnsupportedOperationException();
    }
}

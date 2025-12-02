package xyz.crunchmunch.mods.noxesium.fabric.mixin;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumPlatformHook;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoxesiumMod.class)
public interface NoxesiumModAccessor {
    @Accessor
    static void setPlatform(NoxesiumPlatformHook platform) {
        throw new UnsupportedOperationException();
    }
}

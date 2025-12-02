package xyz.crunchmunch.mods.noxesium.fabric.mixin;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.feature.rule.RuleIndexProvider;
import com.noxcrew.noxesium.network.clientbound.ClientboundChangeServerRulesPacket;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.crunchmunch.mods.noxesium.fabric.NoxesiumFabricManager;

import java.util.List;

@Mixin(ClientboundChangeServerRulesPacket.class)
public abstract class ClientboundChangeServerRulesPacketMixin {
    @Shadow
    @Final
    private IntList indices;

    @Shadow
    @Final
    private List<Object> values;

    /**
     * @author BluSpring
     * @reason We need to write our indices without relying on making NoxesiumMod work on the server.
     */
    @Overwrite
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeIntIdList(this.indices);
        var idx = 0;

        for (var index : this.indices) {
            // If we don't know one rule the whole packet is useless
            var rule = NoxesiumFabricManager.INSTANCE.getServerRules().get(index);
            if (rule == null)
                throw new UnsupportedOperationException("Invalid rule index " + index);

            var data = this.values.get(idx++);
            rule.writeUnsafe(data, buf);
        }
    }
}

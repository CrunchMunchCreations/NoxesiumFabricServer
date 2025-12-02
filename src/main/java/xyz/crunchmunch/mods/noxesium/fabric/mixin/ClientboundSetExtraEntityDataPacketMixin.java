package xyz.crunchmunch.mods.noxesium.fabric.mixin;

import com.noxcrew.noxesium.network.clientbound.ClientboundSetExtraEntityDataPacket;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.crunchmunch.mods.noxesium.fabric.NoxesiumFabricManager;
import xyz.crunchmunch.mods.noxesium.fabric.injected.EntityContextInjection;

import java.util.List;

@Mixin(ClientboundSetExtraEntityDataPacket.class)
public abstract class ClientboundSetExtraEntityDataPacketMixin implements EntityContextInjection {
    @Shadow
    @Final
    private int entityId;

    @Shadow
    @Final
    private IntList indices;

    @Shadow
    @Final
    private List<Object> values;

    private Entity entity;

    @Override
    public @NotNull Entity getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(@NotNull Entity entity) {
        this.entity = entity;
    }

    /**
     * @author BluSpring
     * @reason why does this also rely on the mod.
     */
    @Overwrite
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);

        buf.writeIntIdList(this.indices);
        var idx = 0;

        for (var index : this.indices) {
            // If we don't know one rule the whole packet is useless
            var rule = NoxesiumFabricManager.INSTANCE.getEntityRule(this.entity, index);
            if (rule == null)
                throw new UnsupportedOperationException("Invalid rule index " + index);

            var data = this.values.get(idx++);
            rule.writeUnsafe(data, buf);
        }
    }
}

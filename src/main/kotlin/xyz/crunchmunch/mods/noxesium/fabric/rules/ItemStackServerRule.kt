package xyz.crunchmunch.mods.noxesium.fabric.rules

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*
import kotlin.jvm.optionals.getOrNull

/** A server rule that stores an item stack. */
public class ItemStackServerRule(
    index: Int,
    default: ItemStack = ItemStack(Items.AIR),
) : RemoteServerRule<ItemStack>(index, default) {
    public companion object {
        /** Writes [item] to the given [buffer]. */
        public fun write(buffer: RegistryFriendlyByteBuf, item: ItemStack) {
            if (item.isEmpty) {
                buffer.writeVarInt(0)
            } else {
                val nms = item.copy()
                buffer.writeVarInt(item.count)
                buffer.writeUtf(
                    nms.itemHolder
                        .unwrapKey()
                        .getOrNull()
                        ?.location()
                        ?.toString() ?: "unknown"
                )
                val components = mutableListOf<Map.Entry<DataComponentType<*>, Optional<*>>>()
                val emptyComponents = mutableListOf<Map.Entry<DataComponentType<*>, Optional<*>>>()
                for (entry in nms.componentsPatch.entrySet()) {
                    if (entry.value.isPresent) {
                        components += entry
                    } else {
                        emptyComponents += entry
                    }
                }
                buffer.writeVarInt(components.size)
                buffer.writeVarInt(emptyComponents.size)
                for ((key, value) in components) {
                    buffer.writeUtf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(key).toString())
                    key.streamCodec().encode(buffer, value)
                }
                for ((key) in emptyComponents) {
                    buffer.writeUtf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(key).toString())
                }
            }
        }

        /** Encodes [value] into [buffer] using this codec. */
        public fun <T : Any> StreamCodec<in RegistryFriendlyByteBuf, T>.encode(buffer: RegistryFriendlyByteBuf, value: Optional<*>) {
            encode(buffer, value.get() as T)
        }
    }

    override fun write(value: ItemStack, buffer: RegistryFriendlyByteBuf) {
        write(buffer, value)
    }
}
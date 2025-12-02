package xyz.crunchmunch.mods.noxesium.fabric.injected

import net.minecraft.world.entity.Entity

interface EntityContextInjection {
    var entity: Entity
}
package xyz.crunchmunch.mods.noxesium.fabric.rules

import com.noxcrew.noxesium.api.protocol.rule.EntityRuleIndices
import xyz.crunchmunch.mods.noxesium.fabric.NoxesiumFabricManager
import java.awt.Color
import java.util.*

/** Stores all known entity rules supported by Noxesium. */
object EntityRules {
    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    val disableBubbles: RuleFunction<Boolean> = register(EntityRuleIndices.DISABLE_BUBBLES, 7, ::BooleanServerRule)

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    val beamColor: RuleFunction<Optional<Color>> = register(EntityRuleIndices.BEAM_COLOR, 7, ::ColorServerRule)

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package.
     */
    val qibBehavior: RuleFunction<String> = register(EntityRuleIndices.QIB_BEHAVIOR, 9) { StringServerRule(it, "") }

    /**
     * Allows defining the width of an interaction entity on the Z-axis for the context of
     * qib collisions. The regular width is seen as its width on the X-axis.
     */
    val interactionWidthZ: RuleFunction<Double> = register(EntityRuleIndices.QIB_WIDTH_Z, 9) { DoubleServerRule(it, 1.0) }

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    val beamFadeColor: RuleFunction<Optional<Color>> = register(EntityRuleIndices.BEAM_COLOR_FADE, 12, ::ColorServerRule)

    /**
     * Defines a custom color to use for glowing by this entity.
     */
    val customGlowColor: RuleFunction<Optional<Color>> = register(EntityRuleIndices.CUSTOM_GLOW_COLOR, 17, ::ColorServerRule)

    /** Registers a new [rule]. */
    private fun <T : Any> register(index: Int, minimumProtocol: Int, rule: (Int) -> RemoteServerRule<T>,): RuleFunction<T> {
        val function = RuleFunction(index, rule)
        NoxesiumFabricManager.entityRuleContainer.register(index, minimumProtocol, function)
        return function
    }
}
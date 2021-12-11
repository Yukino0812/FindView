package me.yukino.plugin.findview.model

import com.intellij.ide.util.PropertiesComponent
import kotlin.reflect.KProperty

/**
 * @author Hoshiiro Yukino
 */

object Properties {

    var isAddM by booleanComponent()
    var isTarget26 by booleanComponent()
    var isKotlin by booleanComponent()
    var isKotlinExt by booleanComponent()

    private fun booleanComponent() = PropertiesComponentBooleanDelegate()

    private class PropertiesComponentBooleanDelegate {

        private var value: Boolean? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            if (value == null) {
                value = PropertiesComponent.getInstance().getBoolean(property.name, false)
            }
            return value!!
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            this.value = value
            PropertiesComponent.getInstance().setValue(property.name, value)
        }

    }

}
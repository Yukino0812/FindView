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
    var isIgnorePrefix by booleanComponent()

    var ignorePrefix by stringComponent()

    private fun booleanComponent() = PropertiesComponentBooleanDelegate()
    private fun stringComponent() = PropertiesComponentStringDelegate()

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

    private class PropertiesComponentStringDelegate {

        private var value: String? = null
        private var isInit = false

        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
            if (!isInit) {
                this.value = PropertiesComponent.getInstance().getValue(property.name)
                this.isInit = true
            }
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            this.value = value
            this.isInit = true
            PropertiesComponent.getInstance().setValue(property.name, value)
        }

    }

}
package me.yukino.plugin.findview.model

import me.yukino.plugin.findview.model.ViewPart.Companion.ViewIdPreProcessor
import me.yukino.plugin.findview.util.Definitions
import me.yukino.plugin.findview.util.Utils
import java.util.regex.Pattern

/**
 * Created by Jaeger
 * 15/11/25
 */
class ViewPart {

    var type: String? = null
        private set
    var typeFull: String? = null
        private set
    var id: String? = null
        set(value) {
            field = value
            generateName()
        }
    var name: String? = null
    private var scrNameFromId: String? = null
    var isSelected = true

    fun generateName() {
        var tempId = this.id ?: return
        viewIdPreProcessors.forEach {
            tempId = it.process(tempId)
        }
        val pattern = Pattern.compile("_([a-zA-Z])")
        val matcher = pattern.matcher(tempId)
        val chars = tempId.toCharArray()
        scrNameFromId = String(chars)
        while (matcher.find()) {
            val index = matcher.start(1)
            chars[index] = chars[index].uppercaseChar()
        }
        var name = String(chars)
        name = name.replace("_".toRegex(), "")
        this.name = name
    }

    fun setType(type: String) {
        val packages = type.split("\\.".toRegex()).toTypedArray()
        if (packages.size > 1) {
            typeFull = type
            this.type = packages[packages.size - 1]
        } else {
            typeFull = null
            this.type = type
        }
    }

    fun getDeclareString(isViewHolder: Boolean, isShow: Boolean): String {
        return if (isViewHolder) {
            String.format(OUTPUT_DECLARE_STRING_NOT_PRIVATE, type, name)
        } else {
            if (isShow) {
                return String.format(OUTPUT_DECLARE_STRING, type, name)
            }
            val realType: String? =
                if (!Utils.isEmptyString(typeFull)) {
                    typeFull
                } else if (Definitions.paths.containsKey(type)) {
                    Definitions.paths[type]
                } else {
                    "android.widget.$type"
                }
            String.format(OUTPUT_DECLARE_STRING, realType, name)
        }
    }

    fun getFindViewStringWithRootView(rootView: String?, isTarget26: Boolean): String {
        return if (isTarget26) String.format(OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW_TARGET26, name, rootView, id) else String.format(OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW, name, type, rootView, id)
    }

    fun getFindViewString(isTarget26: Boolean): String {
        return if (isTarget26) String.format(OUTPUT_FIND_VIEW_STRING_TARGET26, name, id) else String.format(OUTPUT_FIND_VIEW_STRING, name, type, id)
    }

    fun getFindViewStringKt(isExtensions: Boolean): String {
        val lName: String? =
            if (isExtensions) {
                scrNameFromId
            } else {
                name
            }
        return String.format(OUTPUT_FIND_VIEW_STRING_KOTLIN, lName, type, type, id)
    }

    fun getFindViewStringForViewHolder(rootView: String?, isTarget26: Boolean): String {
        return if (isTarget26) String.format(OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER_TARGET26, name, rootView, id) else String.format(OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER, name, type, rootView, id)
    }

    override fun toString(): String {
        return "ViewPart{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", selected=" + isSelected +
                '}'
    }

    companion object {
        private const val OUTPUT_DECLARE_STRING = "private %s %s;\n"
        private const val OUTPUT_DECLARE_STRING_NOT_PRIVATE = "%s %s;\n"
        private const val OUTPUT_FIND_VIEW_STRING = "%s = (%s) findViewById(R.id.%s);\n"
        private const val OUTPUT_FIND_VIEW_STRING_TARGET26 = "%s = findViewById(R.id.%s);\n"
        private const val OUTPUT_FIND_VIEW_STRING_KOTLIN = "private val %s: %s by lazy { findViewById<%s>(R.id.%s) }\n"
        private const val OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW = "%s = (%s) %s.findViewById(R.id.%s);\n"
        private const val OUTPUT_FIND_VIEW_STRING_WITH_ROOT_VIEW_TARGET26 = "%s = %s.findViewById(R.id.%s);\n"
        private const val OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER = "viewHolder.%s = (%s) %s.findViewById(R.id.%s);\n"
        private const val OUTPUT_FIND_VIEW_STRING_FOR_VIEW_HOLDER_TARGET26 =
            "viewHolder.%s = %s.findViewById(R.id.%s);\n"

        private fun interface ViewIdPreProcessor {
            fun process(id: String): String
        }

        private val addMPreProcessor = ViewIdPreProcessor {
            if (Properties.isAddM) {
                "m_$it"
            } else {
                it
            }
        }

        private val ignorePrefixPreProcessor = ViewIdPreProcessor {
            if (Properties.isIgnorePrefix && !Properties.ignorePrefix.isNullOrEmpty()) {
                it.replaceFirst(Regex(Properties.ignorePrefix!!), "")
            } else {
                it
            }
        }

        private val viewIdPreProcessors = listOf(
            ignorePrefixPreProcessor,
            addMPreProcessor
        )
    }
}
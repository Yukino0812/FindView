package me.yukino.plugin.findview.util

import me.yukino.plugin.findview.model.Properties
import me.yukino.plugin.findview.model.ViewPart
import org.xml.sax.SAXException
import java.io.IOException
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableModel
import javax.xml.parsers.ParserConfigurationException

/**
 * Created by Jaeger
 * 16/5/28.
 */
object ActionUtil {
    private val HEADERS = arrayOf("selected", "type", "id", "name")
    fun getViewPartList(viewSaxHandler: ViewSaxHandler?, oriContact: String): List<ViewPart?>? {
        try {
            viewSaxHandler!!.createViewList(oriContact)
            return viewSaxHandler.getViewPartList()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    fun switchAddM(viewParts: List<ViewPart?>?, isAddM: Boolean) {
        for (viewPart in viewParts!!) {
            viewPart!!.generateName()
        }
    }

    fun generateCode(
        viewParts: List<ViewPart?>?,
        isViewHolder: Boolean,
        isTarget26: Boolean,
        isAddRootView: Boolean,
        rootView: String?
    ): String {
        viewParts!!.forEach { viewPart ->
            viewPart!!.generateName()
        }
        val stringBuilder = StringBuilder()
        for (viewPart in viewParts) {
            if (viewPart!!.isSelected) {
                stringBuilder.append(viewPart.getDeclareString(isViewHolder, true))
            }
        }
        stringBuilder.append("\n")
        for (viewPart in viewParts) {
            if (viewPart!!.isSelected) {
                if (isViewHolder) {
                    stringBuilder.append(viewPart.getFindViewStringForViewHolder(rootView, isTarget26))
                } else if (isAddRootView && !rootView.isNullOrEmpty()) {
                    stringBuilder.append(viewPart.getFindViewStringWithRootView(rootView, isTarget26))
                } else {
                    stringBuilder.append(viewPart.getFindViewString(isTarget26))
                }
            }
        }
        return stringBuilder.toString()
    }

    fun generateCode(
        viewParts: List<ViewPart?>?,
        isViewHolder: Boolean,
        isTarget26: Boolean,
        isAddRootView: Boolean,
        rootView: String?,
        isKotlin: Boolean,
        isExtensions: Boolean
    ): String {
        viewParts!!.forEach { viewPart ->
            viewPart!!.generateName()
        }
        val stringBuilder = StringBuilder()
        if (isKotlin) {
            for (viewPart in viewParts) {
                stringBuilder.append(viewPart!!.getFindViewStringKt(isExtensions))
            }
        } else {
            for (viewPart in viewParts!!) {
                if (viewPart!!.isSelected) {
                    stringBuilder.append(viewPart.getDeclareString(isViewHolder, true))
                }
            }
            stringBuilder.append("\n")
            for (viewPart in viewParts) {
                if (viewPart!!.isSelected) {
                    if (isViewHolder) {
                        stringBuilder.append(viewPart.getFindViewStringForViewHolder(rootView, isTarget26))
                    } else if (isAddRootView && !rootView.isNullOrEmpty()) {
                        stringBuilder.append(viewPart.getFindViewStringWithRootView(rootView, isTarget26))
                    } else {
                        stringBuilder.append(viewPart.getFindViewString(isTarget26))
                    }
                }
            }
        }
        return stringBuilder.toString()
    }

    fun getTableModel(viewParts: List<ViewPart?>, tableModelListener: TableModelListener?): DefaultTableModel {
        val tableModel: DefaultTableModel
        val size = viewParts.size
        val cellData = Array(size) { arrayOfNulls<Any>(4) }
        for (i in 0 until size) {
            val viewPart = viewParts[i]
            for (j in 0..3) {
                when (j) {
                    0 -> cellData[i][j] = viewPart?.isSelected
                    1 -> cellData[i][j] = viewPart?.type
                    2 -> cellData[i][j] = viewPart?.id
                    3 -> cellData[i][j] = viewPart?.name
                }
            }
        }
        tableModel = object : DefaultTableModel(cellData, HEADERS) {
            val typeArray = arrayOf(java.lang.Boolean::class.java, Any::class.java, Any::class.java, Any::class.java)
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return column == 0
            }

            override fun getColumnClass(column: Int): Class<*> {
                return typeArray[column]
            }
        }
        tableModel.addTableModelListener(tableModelListener)
        return tableModel
    }

    fun filter(viewParts: List<ViewPart?>?): List<ViewPart?> {
        viewParts ?: return emptyList()
        if (Properties.filterStr.isNullOrEmpty() || !Properties.isFilter) {
            return viewParts
        }
        return viewParts
            .filter {
                it?.id?.contains(Properties.filterStr!!) == true
            }
    }
}
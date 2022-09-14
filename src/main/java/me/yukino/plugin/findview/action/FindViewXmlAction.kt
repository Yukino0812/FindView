package me.yukino.plugin.findview.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.wm.WindowManager
import me.yukino.plugin.findview.action.FindViewDialog.OnClickListener
import me.yukino.plugin.findview.model.Properties
import me.yukino.plugin.findview.model.ViewPart
import me.yukino.plugin.findview.util.ActionUtil
import me.yukino.plugin.findview.util.ActionUtil.filter
import me.yukino.plugin.findview.util.ViewSaxHandler
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableModel

/**
 * Created by Jaeger
 * 15/11/25
 */
class FindViewXmlAction : AnAction() {
    private var viewSaxHandler: ViewSaxHandler? = null
    private var findViewDialog: FindViewDialog? = null
    private var viewParts: List<ViewPart?>? = null
    private var tableModel: DefaultTableModel? = null

    //当前map中的位置
    private var currentListSelect = 0

    //上次搜索的关键字
    private var oldKeyword = ""

    //搜索出来匹配的 map key 为在总数据中所在的位置. value 为name
    private val keywordArr = HashMap<Int, String?>()

    //当前是否匹配上
    private var isMatch = false
    private var keys = ArrayList<Int>()

    /**
     * 启动时触发
     */
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        viewSaxHandler = ViewSaxHandler()
        if (findViewDialog == null) {
            findViewDialog = FindViewDialog()
        }
        getViewList(anActionEvent)
        ActionUtil.switchAddM(
            viewParts, Properties.isAddM
        )
        updateTable()
        findViewDialog!!.title = "FindView in XML"
        findViewDialog!!.setOnClickListener(OnClickListener)
        findViewDialog!!.pack()
        findViewDialog!!.setLocationRelativeTo(WindowManager.getInstance().getFrame(anActionEvent.project))
        findViewDialog!!.isVisible = true
    }

    /**
     * 获取View列表
     *
     * @param event 触发事件
     */
    private fun getViewList(event: AnActionEvent) {
        val psiFile = event.getData(LangDataKeys.PSI_FILE)
        val editor = event.getData(PlatformDataKeys.EDITOR)
        if (psiFile == null || editor == null) {
            return
        }
        val contentStr = psiFile.text
        if (psiFile.parent != null) {
            viewSaxHandler?.layoutPath = psiFile.containingDirectory.toString().replace("PsiDirectory:", "")
            viewSaxHandler?.project = event.project
        }
        viewParts = ActionUtil.getViewPartList(viewSaxHandler, contentStr)
    }

    /**
     * FindView 对话框回调
     */
    private val OnClickListener: OnClickListener = object : OnClickListener {
        override fun onUpdateRootView() {
            generateCode()
        }

        override fun onUpdateIgnorePrefix() {
            updateTable()
        }

        override fun onUpdateFilter() {
            updateTable()
        }

        override fun onOK() {
            val clip = Toolkit.getDefaultToolkit().systemClipboard
            val tText: Transferable = StringSelection(findViewDialog!!.textCode!!.text)
            clip.setContents(tText, null)
        }

        override fun onSelectAll() {
            for (viewPart in viewParts!!) {
                viewPart?.isSelected = true
            }
            updateTable()
        }

        override fun onSelectNone() {
            for (viewPart in viewParts!!) {
                viewPart?.isSelected = false
            }
            updateTable()
        }

        override fun onNegativeSelect() {
            for (viewPart in viewParts!!) {
                viewPart?.isSelected = !viewPart!!.isSelected
            }
            updateTable()
        }

        override fun onSwitchAddRootView() {
            generateCode()
        }

        override fun onSwitchAddM(addM: Boolean) {
            ActionUtil.switchAddM(viewParts, addM)
            updateTable()
        }

        override fun onSwitchIsViewHolder() {
            generateCode()
        }

        override fun onSwitchIsKotlin(isKotlin: Boolean) {
            generateCode()
        }

        override fun onSwitchExtensions(isExtensions: Boolean) {
            generateCode()
        }

        override fun onSwitchIsTarget26(target26: Boolean) {
            generateCode()
        }

        override fun onSwitchIgnorePrefix() {
            updateTable()
        }

        override fun onFinish() {
            viewParts = null
            viewSaxHandler = null
            findViewDialog = null
        }
    }

    /**
     * 生成FindViewById代码
     */
    private fun generateCode() {
        findViewDialog!!.setTextCode(
            ActionUtil.generateCode(
                filter(viewParts),
                Properties.isViewHolder,
                Properties.isTarget26,
                Properties.isAddRootView,
                Properties.rootViewStr,
                Properties.isKotlin,
                Properties.isKotlinExt
            )
        )
    }

    /**
     * 更新 View 表格
     */
    fun updateTable() {
        if (viewParts.isNullOrEmpty()) {
            return
        }
        viewParts!!.forEach {
            it?.generateName()
        }
        tableModel = ActionUtil.getTableModel(filter(viewParts), tableModelListener)
        findViewDialog!!.setTableModel(tableModel)
        generateCode()
    }

    var tableModelListener = TableModelListener { event ->
        if (tableModel == null) {
            return@TableModelListener
        }
        val row = event.firstRow
        val column = event.column
        if (column == 0) {
            val isSelected = tableModel!!.getValueAt(row, column) as Boolean
            viewSaxHandler?.getViewPartList()?.get(row)?.isSelected = isSelected
            generateCode()
        }
    }
}
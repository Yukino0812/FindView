package me.yukino.plugin.findview.action

import com.intellij.ide.util.PropertiesComponent
import me.yukino.plugin.findview.model.PropertiesKey
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel

class FindViewDialog : JDialog() {
    private lateinit var contentPane: JPanel
    lateinit var btnCopyCode: JButton
    lateinit var btnClose: JButton
    lateinit var chbAddRootView: JCheckBox
    lateinit var textRootView: JTextField
    lateinit var textCode: JTextArea
    lateinit var chbAddM: JCheckBox
    lateinit var tableViews: JTable
    lateinit var btnSelectAll: JButton
    lateinit var btnSelectNone: JButton
    lateinit var btnNegativeSelect: JButton
    private lateinit var chbIsViewHolder: JCheckBox
    private lateinit var chbIsTarget26: JCheckBox
    private lateinit var editSearch: JTextField
    private lateinit var btnSearch: JButton
    private lateinit var chbIsKotlin: JCheckBox
    private lateinit var chbIsExtensions: JCheckBox
    private var onClickListener: OnClickListener? = null

    init {
        setContentPane(contentPane)
        isModal = true
        textRootView.isEnabled = false
        initStatus()
        btnSearch.addActionListener {
            if (onClickListener != null) {
                onClickListener?.onSearch(search)
            }
        }
        btnCopyCode.addActionListener {
            if (onClickListener != null) {
                onClickListener?.onOK()
            }
            onCancel()
        }
        textRootView.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                if (onClickListener != null) {
                    onClickListener?.onUpdateRootView()
                }
            }

            override fun removeUpdate(e: DocumentEvent) {
                if (onClickListener != null) {
                    onClickListener?.onUpdateRootView()
                }
            }

            override fun changedUpdate(e: DocumentEvent) {
                if (onClickListener != null) {
                    onClickListener?.onUpdateRootView()
                }
            }
        })
        chbAddM.addChangeListener {
            if (onClickListener != null) {
                onClickListener?.onSwitchAddM(chbAddM.isSelected)
                PropertiesComponent.getInstance().setValue(PropertiesKey.SAVE_ADD_M_ACTION, chbAddM.isSelected)
            }
        }
        chbIsViewHolder.addChangeListener {
            if (onClickListener != null) {
                onClickListener?.onSwitchIsViewHolder(chbIsViewHolder.isSelected)
            }
        }
        chbIsTarget26.addChangeListener {
            if (onClickListener != null) {
                onClickListener?.onSwitchIsTarget26(chbIsTarget26.isSelected)
                PropertiesComponent.getInstance().setValue(PropertiesKey.IS_TARGET_26, chbIsTarget26.isSelected)
            }
        }
        chbIsKotlin.addChangeListener {
            if (onClickListener != null) {
                onClickListener?.onSwitchIsKotlin(chbIsKotlin.isSelected)
                PropertiesComponent.getInstance().setValue(PropertiesKey.IS_KT, chbIsKotlin.isSelected)
            }
        }
        chbIsExtensions.addChangeListener {
            if (onClickListener != null) {
                onClickListener?.onSwitchExtensions(chbIsExtensions.isSelected)
                PropertiesComponent.getInstance().setValue(PropertiesKey.IS_KT_ETX, chbIsExtensions.isSelected)
            }
        }
        chbAddRootView.addChangeListener {
            val isAdd = chbAddRootView.isSelected
            if (onClickListener != null) {
                onClickListener?.onSwitchAddRootView(isAdd)
            }
            textRootView.isEnabled = isAdd
        }
        btnClose.addActionListener { onCancel() }
        btnSelectAll.addActionListener {
            if (onClickListener != null) {
                onClickListener?.onSelectAll()
            }
        }
        btnSelectNone.addActionListener {
            if (onClickListener != null) {
                onClickListener?.onSelectNone()
            }
        }
        btnNegativeSelect.addActionListener {
            if (onClickListener != null) {
                onClickListener?.onNegativeSelect()
            }
        }
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })
        contentPane.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        contentPane.registerKeyboardAction({
            if (onClickListener != null) {
                onClickListener?.onOK()
            }
            onCancel()
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)
    }

    private fun initStatus() {
        chbAddM.isSelected = PropertiesComponent.getInstance().getBoolean(PropertiesKey.SAVE_ADD_M_ACTION, false)
        chbIsTarget26.isSelected = PropertiesComponent.getInstance().getBoolean(PropertiesKey.IS_TARGET_26, false)
        chbIsKotlin.isSelected = PropertiesComponent.getInstance().getBoolean(PropertiesKey.IS_KT, false)
        chbIsExtensions.isSelected = PropertiesComponent.getInstance().getBoolean(PropertiesKey.IS_KT_ETX, false)
    }

    private fun onCancel() {
        dispose()
        if (onClickListener != null) {
            onClickListener?.onFinish()
        }
    }

    fun setTextCode(codeStr: String?) {
        textCode.text = codeStr
    }

    fun setSelect(position: Int) {
//        System.out.println("开始的位置"+position);
        tableViews.grabFocus()
        tableViews.changeSelection(position, 1, false, false)
    }

    interface OnClickListener {
        fun onUpdateRootView()
        fun onOK()
        fun onSelectAll()
        fun onSearch(string: String)
        fun onSelectNone()
        fun onNegativeSelect()
        fun onSwitchAddRootView(isAddRootView: Boolean)
        fun onSwitchAddM(addM: Boolean)
        fun onSwitchIsViewHolder(isViewHolder: Boolean)
        fun onSwitchIsKotlin(isKotlin: Boolean)
        fun onSwitchExtensions(isExtensions: Boolean)
        fun onSwitchIsTarget26(target26: Boolean)
        fun onFinish()
    }

    fun setOnClickListener(OnClickListener: OnClickListener?) {
        this.onClickListener = OnClickListener
    }

    fun setModel(model: DefaultTableModel?) {
        tableViews.model = model
        tableViews.columnModel.getColumn(0).preferredWidth = 20
    }

    val rootView: String
        get() = textRootView.text.trim { it <= ' ' }
    val search: String
        get() = editSearch.text.trim { it <= ' ' }
}
package me.yukino.plugin.findview.action

import me.yukino.plugin.findview.model.Properties
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
            onClickListener?.onSearch(searchText)
        }
        btnCopyCode.addActionListener {
            onClickListener?.onOK()
            onCancel()
        }
        textRootView.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                onClickListener?.onUpdateRootView()
            }

            override fun removeUpdate(e: DocumentEvent) {
                onClickListener?.onUpdateRootView()
            }

            override fun changedUpdate(e: DocumentEvent) {
                onClickListener?.onUpdateRootView()
            }
        })
        chbAddM.addChangeListener {
            Properties.isAddM = chbAddM.isSelected
            onClickListener?.onSwitchAddM(chbAddM.isSelected)
        }
        chbIsViewHolder.addChangeListener {
            onClickListener?.onSwitchIsViewHolder(chbIsViewHolder.isSelected)
        }
        chbIsTarget26.addChangeListener {
            Properties.isTarget26 = chbIsTarget26.isSelected
            onClickListener?.onSwitchIsTarget26(chbIsTarget26.isSelected)
        }
        chbIsKotlin.addChangeListener {
            Properties.isKotlin = chbIsKotlin.isSelected
            onClickListener?.onSwitchIsKotlin(chbIsKotlin.isSelected)
        }
        chbIsExtensions.addChangeListener {
            Properties.isKotlinExt = chbIsExtensions.isSelected
            onClickListener?.onSwitchExtensions(chbIsExtensions.isSelected)
        }
        chbAddRootView.addChangeListener {
            val isAdd = chbAddRootView.isSelected
            onClickListener?.onSwitchAddRootView(isAdd)
            textRootView.isEnabled = isAdd
        }
        btnClose.addActionListener {
            onCancel()
        }
        btnSelectAll.addActionListener {
            onClickListener?.onSelectAll()
        }
        btnSelectNone.addActionListener {
            onClickListener?.onSelectNone()
        }
        btnNegativeSelect.addActionListener {
            onClickListener?.onNegativeSelect()
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
        contentPane.registerKeyboardAction(
            {
                onClickListener?.onOK()
                onCancel()
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        )
    }

    private fun initStatus() {
        chbAddM.isSelected = Properties.isAddM
        chbIsTarget26.isSelected = Properties.isTarget26
        chbIsKotlin.isSelected = Properties.isKotlin
        chbIsExtensions.isSelected = Properties.isKotlinExt
    }

    private fun onCancel() {
        dispose()
        onClickListener?.onFinish()
    }

    fun setTextCode(codeStr: String?) {
        textCode.text = codeStr
    }

    fun setSelect(position: Int) {
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

    fun setTableModel(model: DefaultTableModel?) {
        tableViews.model = model
        tableViews.columnModel.getColumn(0).preferredWidth = 20
    }

    val rootViewText: String
        get() = textRootView.text.trim { it <= ' ' }
    val searchText: String
        get() = editSearch.text.trim { it <= ' ' }
}
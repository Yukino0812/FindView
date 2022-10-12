package me.yukino.plugin.findview.action

import me.yukino.plugin.findview.model.Properties
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
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
    private lateinit var editFilter: JTextField
    private lateinit var chbIsKotlin: JCheckBox
    private lateinit var chbIsExtensions: JCheckBox
    private lateinit var cbIgnorePrefix: JCheckBox
    private lateinit var etIgnorePrefix: JTextField
    private lateinit var chbFilter: JCheckBox
    private var onClickListener: OnClickListener? = null

    init {
        setContentPane(contentPane)
        isModal = true
        initStatus()
        btnCopyCode.addActionListener {
            onClickListener?.onOK()
            onCancel()
        }
        textRootView.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                Properties.rootViewStr = textRootView.text.trim { it <= ' ' }
                onClickListener?.onUpdateRootView()
            }

            override fun removeUpdate(e: DocumentEvent) {
                Properties.rootViewStr = textRootView.text.trim { it <= ' ' }
                onClickListener?.onUpdateRootView()
            }

            override fun changedUpdate(e: DocumentEvent) {
                Properties.rootViewStr = textRootView.text.trim { it <= ' ' }
                onClickListener?.onUpdateRootView()
            }
        })
        etIgnorePrefix.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                Properties.ignorePrefix = etIgnorePrefix.text
                onClickListener?.onUpdateIgnorePrefix()
            }

            override fun removeUpdate(e: DocumentEvent) {
                Properties.ignorePrefix = etIgnorePrefix.text
                onClickListener?.onUpdateIgnorePrefix()
            }

            override fun changedUpdate(e: DocumentEvent) {
                Properties.ignorePrefix = etIgnorePrefix.text
                onClickListener?.onUpdateIgnorePrefix()
            }
        })
        editFilter.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                Properties.filterStr = editFilter.text.trim { it <= ' ' }
                onClickListener?.onUpdateFilter()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                Properties.filterStr = editFilter.text.trim { it <= ' ' }
                onClickListener?.onUpdateFilter()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                Properties.filterStr = editFilter.text.trim { it <= ' ' }
                onClickListener?.onUpdateFilter()
            }
        })
        chbAddM.addChangeListener {
            Properties.isAddM = chbAddM.isSelected
            onClickListener?.onSwitchAddM(chbAddM.isSelected)
        }
        chbIsViewHolder.addChangeListener {
            Properties.isViewHolder = chbIsViewHolder.isSelected
            onClickListener?.onSwitchIsViewHolder()
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
            Properties.isAddRootView = chbAddRootView.isSelected
            onClickListener?.onSwitchAddRootView()
        }
        cbIgnorePrefix.addActionListener {
            Properties.isIgnorePrefix = cbIgnorePrefix.isSelected
            onClickListener?.onSwitchIgnorePrefix()
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
        chbFilter.addActionListener {
            Properties.isFilter = chbFilter.isSelected
            onClickListener?.onUpdateFilter()
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
        chbAddRootView.isSelected = Properties.isAddRootView
        chbIsViewHolder.isSelected = Properties.isViewHolder
        cbIgnorePrefix.isSelected = Properties.isIgnorePrefix
        chbFilter.isSelected = Properties.isFilter

        textRootView.text = Properties.rootViewStr
        etIgnorePrefix.text = Properties.ignorePrefix
    }

    fun onStart() {
        Properties.filterStr = ""
        editFilter.text = ""
    }

    private fun onCancel() {
        dispose()
        onClickListener?.onFinish()
    }

    fun setTextCode(codeStr: String?) {
        textCode.text = codeStr
    }

    interface OnClickListener {
        fun onUpdateRootView()
        fun onUpdateIgnorePrefix()
        fun onUpdateFilter()
        fun onOK()
        fun onSelectAll()
        fun onSelectNone()
        fun onNegativeSelect()
        fun onSwitchAddRootView()
        fun onSwitchAddM(addM: Boolean)
        fun onSwitchIsViewHolder()
        fun onSwitchIsKotlin(isKotlin: Boolean)
        fun onSwitchExtensions(isExtensions: Boolean)
        fun onSwitchIsTarget26(target26: Boolean)
        fun onSwitchIgnorePrefix()
        fun onFinish()
    }

    fun setOnClickListener(OnClickListener: OnClickListener?) {
        this.onClickListener = OnClickListener
    }

    fun setTableModel(model: DefaultTableModel?) {
        tableViews.model = model
        tableViews.columnModel.getColumn(0).preferredWidth = 20
    }

}
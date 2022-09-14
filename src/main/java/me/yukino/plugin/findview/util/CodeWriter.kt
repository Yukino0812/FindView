package me.yukino.plugin.findview.util

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.EverythingGlobalScope
import me.yukino.plugin.findview.model.ViewPart

/**
 * Created by pengwei on 16/5/20.
 */
class CodeWriter(
    private val psiFile: PsiFile?,
    clazz: PsiClass?,
    viewPartList: List<ViewPart?>?,
    isViewHolder: Boolean,
    isTarget26: Boolean,
    isAddRootView: Boolean,
    rootViewStr: String?,
    editor: Editor?
) : WriteCommandAction.Simple<Any?>(clazz!!.project, "") {
    private val viewPartList: List<ViewPart?>?
    var mProject: Project
    var mClass: PsiClass?
    var mFactory: PsiElementFactory
    private val mEditor: Editor?
    private val isAddRootView: Boolean
    private val isViewHolder: Boolean
    private val isTarget26: Boolean
    private val rootViewStr: String?

    init {
        mProject = clazz!!.project
        mClass = clazz
        mFactory = JavaPsiFacade.getElementFactory(mProject)
        mEditor = editor
        this.viewPartList = viewPartList
        this.isAddRootView = isAddRootView
        this.isViewHolder = isViewHolder
        this.isTarget26 = isTarget26
        this.rootViewStr = rootViewStr
    }

    /**
     * judge field exists
     *
     * @param part
     * @return
     */
    private fun fieldExist(part: ViewPart?): Boolean {
        val fields = mClass!!.allFields
        for (field in fields) {
            if (field.name == part?.name) {
                return true
            }
        }
        return false
    }

    /**
     * get initView method
     *
     * @return
     */
    private val initView: PsiMethod?
        get() {
            val methods = mClass!!.findMethodsByName("initView", true)
            for (method in methods) {
                if (method.returnType == PsiType.VOID) {
                    return method
                }
            }
            return null
        }

    /**
     * Add the initView() after onCreate()
     *
     * @param rootViewStr
     */
    private fun addInitViewAfterOnCreate(rootViewStr: String?) {
        val initViewStatement = getInitViewStatementAsString(rootViewStr)
        val createMethod = mClass!!.findMethodsByName("onCreate", false)[0]
        for (statement in createMethod.body!!.statements) {
            if (statement.text == initViewStatement) {
                return
            }
        }
        createMethod.body!!.add(mFactory.createStatementFromText(initViewStatement, mClass))
    }

    /**
     * Add the `initView` method after onCreateView()
     *
     * @param rootViewStr
     */
    private fun addInitViewAfterOnCreateView(rootViewStr: String?) {
        val initViewStatement = getInitViewStatementAsString(rootViewStr)
        val createMethod = mClass!!.findMethodsByName("onCreateView", false)[0]
        for (statement in createMethod.body!!.statements) {
            if (statement.text == initViewStatement) {
                return
            }
        }
        val inflaterStatement = findInflaterStatement(createMethod.body!!.statements)
        createMethod.body!!.addAfter(mFactory.createStatementFromText(initViewStatement, mClass), inflaterStatement)
    }

    /**
     * Creates a string representing the initView method.
     *
     *
     * If `rootViewStr` is provided then it will generate a method with
     * `rootViewStr` as a param. A no-params method in case it's not provided.
     *
     * @param rootViewStr the name of root view
     * @return the method to append
     */
    private fun getInitViewStatementAsString(rootViewStr: String?): String {
        var initViewStatement = "initView();"
        if (!rootViewStr.isNullOrEmpty()) {
            initViewStatement = "initView($rootViewStr);"
        }
        return initViewStatement
    }

    private fun findInflaterStatement(psiStatements: Array<PsiStatement>): PsiStatement? {
        for (psiStatement in psiStatements) {
            if (psiStatement.text.contains(".inflate(")) {
                return psiStatement
            }
        }
        return null
    }

    @Throws(Throwable::class)
    override fun run() {
        var fieldCount = 0
        val initViewMethod = initView
        val methodBuild: StringBuilder =
            if (isAddRootView && !rootViewStr.isNullOrEmpty()) {
                StringBuilder("private void initView(View $rootViewStr) {")
            } else {
                StringBuilder("private void initView() {")
            }
        for (viewPart in viewPartList!!) {
            if (viewPart == null || !viewPart.isSelected || fieldExist(viewPart)) {
                continue
            }
            mClass!!.add(mFactory.createFieldFromText(viewPart.getDeclareString(isViewHolder = false, isShow = false), mClass))
            if (initViewMethod != null) {
                initViewMethod.body!!.add(mFactory.createStatementFromText(viewPart.getFindViewString(isTarget26), mClass))
            } else {
                if (isViewHolder) {
                    methodBuild.append(viewPart.getFindViewStringForViewHolder(rootViewStr, isTarget26))
                } else if (isAddRootView && !rootViewStr.isNullOrEmpty()) {
                    methodBuild.append(viewPart.getFindViewStringWithRootView(rootViewStr, isTarget26))
                } else {
                    methodBuild.append(viewPart.getFindViewString(isTarget26))
                }
                fieldCount++
            }
        }
        methodBuild.append("}")
        if (fieldCount > 0) {
            mClass!!.add(mFactory.createMethodFromText(methodBuild.toString(), mClass))
        }
        addInit(rootViewStr)

        // reformat class
        val styleManager = JavaCodeStyleManager.getInstance(mProject)
        styleManager.optimizeImports(psiFile!!)
        styleManager.shortenClassReferences(mClass!!)
        ReformatCodeProcessor(mProject, mClass!!.containingFile, null, false).runWithoutProgress()
    }

    private fun addInit(rootViewStr: String?) {
        val activityClass = JavaPsiFacade.getInstance(mProject).findClass(
            "android.app.Activity", EverythingGlobalScope(mProject)
        )
        val fragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
            "android.app.Fragment", EverythingGlobalScope(mProject)
        )
        val supportFragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
            "android.support.v4.app.Fragment", EverythingGlobalScope(mProject)
        )

        // Check for Activity class
        if (activityClass != null && mClass!!.isInheritor(activityClass, true)) {
            addInitViewAfterOnCreate(rootViewStr)
            // Check for Fragment class
        } else if (fragmentClass != null && mClass!!.isInheritor(fragmentClass, true) || supportFragmentClass != null && mClass!!.isInheritor(supportFragmentClass, true)) {
            addInitViewAfterOnCreateView(rootViewStr)
        } else {
            Utils.showInfoNotification(mEditor!!.project, "Add " + getInitViewStatementAsString(rootViewStr) + " where relevant!")
        }
    }
}
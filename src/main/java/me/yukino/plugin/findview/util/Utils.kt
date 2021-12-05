package me.yukino.plugin.findview.util

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.search.EverythingGlobalScope
import com.intellij.psi.search.FilenameIndex
import com.intellij.ui.awt.RelativePoint
import java.util.Locale

/**
 * author: TomasKypta
 * source: https://github.com/avast/android-butterknife-zelezny
 */
object Utils {
    /**
     * Is using Android SDK?
     */
    fun findAndroidSDK(): Sdk? {
        return ProjectJdkTable.getInstance().allJdks.find {
            it.sdkType.name.lowercase(Locale.getDefault()).contains("android")
        }
    }

    /**
     * Try to find layout XML file in current source on cursor's position
     *
     * @param editor
     * @param file
     * @return
     */
    fun getLayoutFileFromCaret(editor: Editor?, file: PsiFile?): PsiFile? {
        editor ?: return null
        file ?: return null
        val offset = editor.caretModel.offset
        val candidateA = file.findElementAt(offset)
        val candidateB = file.findElementAt(offset - 1)
        val layout = findLayoutResource(candidateA)
        return layout ?: findLayoutResource(candidateB)
    }

    /**
     * Try to find layout XML file in selected element
     *
     * @param element
     * @return
     */
    fun findLayoutResource(element: PsiElement?): PsiFile? {
        if (element == null) {
            return null // nothing to be used
        }
        if (element !is PsiIdentifier) {
            return null // nothing to be used
        }
        val layout = element.getParent().firstChild
            ?: return null // no file to process
        if ("R.layout" != layout.text) {
            return null // not layout file
        }
        val project = element.getProject()
        val name = String.format("%s.xml", element.getText())
        return resolveLayoutResourceFile(element, project, name)
    }

    private fun resolveLayoutResourceFile(element: PsiElement, project: Project, name: String): PsiFile? {
        // restricting the search to the current module - searching the whole project could return wrong layouts
        val module = ModuleUtil.findModuleForPsiElement(element)
        var files: Array<PsiFile?>? = null
        if (module != null) {
            val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
            files = FilenameIndex.getFilesByName(project, name, moduleScope)
        }
        if (files == null || files.isEmpty()) {
            // fallback to search through the whole project
            // useful when the project is not properly configured - when the resource directory is not configured
            files = FilenameIndex.getFilesByName(project, name, EverythingGlobalScope(project))
            if (files.isEmpty()) {
                return null //no matching files
            }
        }

        // TODO - we have a problem here - we still can have multiple layouts (some coming from a dependency)
        // we need to resolve R class properly and find the proper layout for the R class
        return files[0]
    }

    /**
     * Try to find layout XML file by name
     *
     * @param file
     * @param project
     * @param fileName
     * @return
     */
    fun findLayoutResource(file: PsiFile, project: Project, fileName: String?): PsiFile? {
        val name = String.format("%s.xml", fileName)
        // restricting the search to the module of layout that includes the layout we are seaching for
        return resolveLayoutResourceFile(file, project, name)
    }

    /**
     * Get layout name from XML identifier (@layout/....)
     *
     * @param layout
     * @return
     */
    fun getLayoutName(layout: String?): String? {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null // it's not layout identifier
        }
        val parts = layout.split("/".toRegex()).toTypedArray()
        return if (parts.size != 2) {
            null // not enough parts
        } else parts[1]
    }

    /**
     * Display simple notification - information
     *
     * @param project
     * @param text
     */
    fun showInfoNotification(project: Project?, text: String?) {
        showNotification(project, MessageType.INFO, text)
    }

    /**
     * Display simple notification - error
     *
     * @param project
     * @param text
     */
    fun showErrorNotification(project: Project?, text: String?) {
        showNotification(project, MessageType.ERROR, text)
    }

    /**
     * Display simple notification of given type
     *
     * @param project
     * @param type
     * @param text
     */
    fun showNotification(project: Project?, type: MessageType?, text: String?) {
        val statusBar = WindowManager.getInstance().getStatusBar(project)
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(text!!, type, null)
            .setFadeoutTime(7500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight)
    }

    /**
     * Easier way to check if string is empty
     *
     * @param text
     * @return
     */
    fun isEmptyString(text: String?): Boolean {
        return text == null || text.trim { it <= ' ' }.isEmpty()
    }

    /**
     * Check whether classpath of a module that corresponds to a [PsiElement] contains given class.
     *
     * @param project Project
     * @param psiElement Element for which we check the class
     * @param className Class name of the searched class
     * @return True if the class is present on the classpath
     * @since 1.3
     */
    fun isClassAvailableForPsiFile(project: Project, psiElement: PsiElement, className: String): Boolean {
        val module = ModuleUtil.findModuleForPsiElement(psiElement) ?: return false
        val moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false)
        val classInModule = JavaPsiFacade.getInstance(project).findClass(className, moduleScope)
        return classInModule != null
    }

    /**
     * Check whether classpath of a whole project contains given class.
     * This is only fallback for wrongly setup projects.
     *
     * @param project Project
     * @param className Class name of the searched class
     * @return True if the class is present on the classpath
     * @since 1.3.1
     */
    fun isClassAvailableForProject(project: Project, className: String): Boolean {
        val classInModule = JavaPsiFacade.getInstance(project).findClass(
            className,
            EverythingGlobalScope(project)
        )
        return classInModule != null
    }

    /**
     * 弹窗
     *
     * @param msg
     */
    fun alert(msg: String?) {
        Messages.showMessageDialog(msg, "FindView", Messages.getInformationIcon())
    }

    /**
     * @param src 主串
     * @param sub 字串（模式串）
     */
    fun bruteFore(src: String?, sub: String): Int {
        return src!!.uppercase(Locale.getDefault()).indexOf(sub.uppercase(Locale.getDefault()))
    }
}
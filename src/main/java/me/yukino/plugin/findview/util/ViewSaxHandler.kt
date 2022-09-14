package me.yukino.plugin.findview.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import me.yukino.plugin.findview.model.ViewPart
import me.yukino.plugin.findview.util.ActionUtil.filter
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class ViewSaxHandler : DefaultHandler() {
    private var viewPartList: MutableList<ViewPart?>? = null
    var layoutPath = ""
    var project: Project? = null

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun createViewList(string: String) {
        val xmlStream: InputStream = ByteArrayInputStream(string.toByteArray(charset("UTF-8")))
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(xmlStream, this)
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        if (viewPartList == null) {
            viewPartList = ArrayList()
        }
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if (qName == "include") {
            val includeLayout = attributes.getValue("layout")
            if (includeLayout != null) {
                val file = File(layoutPath, includeLayout.replace("@layout", "") + ".xml")
                if (file.exists()) {
                    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: return
                    val psiFile = PsiManager.getInstance(project!!).findFile(virtualFile)
                    try {
                        if (psiFile != null) {
                            this.createViewList(psiFile.text)
                        }
                    } catch (e: ParserConfigurationException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            val id = attributes.getValue("android:id")
            if (id != null) {
                val viewPart = ViewPart()
                viewPart.setType(qName)
                viewPart.id = id.replace("@+id/", "").replace("@id/", "").replace("@android:id/", "")
                viewPartList!!.add(viewPart)
            }
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
    }

    fun getViewPartList(): List<ViewPart?> {
        return filter(viewPartList)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val str = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:orientation="vertical"
              android:layout_width="fill_parent"
              android:layout_height="fill_parent">
    <TextView
            android:id="@id/hello_world"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:text="Hello World, MyActivity"/>
    <TextView
            android:id="@+id/hello_world_plus"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:text="Hello World, MyActivity"/>
</LinearLayout>

"""
            val handler = ViewSaxHandler()
            try {
                handler.createViewList(str)
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val viewParts = handler.getViewPartList()
            for (viewPart in viewParts!!) {
                println(viewPart.toString())
            }
        }
    }
}
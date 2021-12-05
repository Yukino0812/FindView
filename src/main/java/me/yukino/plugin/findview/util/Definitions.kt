package me.yukino.plugin.findview.util

object Definitions {

    // special classes; default package is android.widget.*
    val paths = mapOf(
        "WebView" to "android.webkit.WebView",
        "View" to "android.view.View",
    )

    // adapters
    val adapters = listOf(
        "android.widget.ListAdapter",
        "android.widget.ArrayAdapter",
        "android.widget.BaseAdapter",
        "android.widget.HeaderViewListAdapter",
        "android.widget.SimpleAdapter",
        "android.support.v4.widget.CursorAdapter",
        "android.support.v4.widget.SimpleCursorAdapter",
        "android.support.v4.widget.ResourceCursorAdapter",
    )

}
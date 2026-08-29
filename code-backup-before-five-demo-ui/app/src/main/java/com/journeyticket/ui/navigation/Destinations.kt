package com.journeyticket.ui.navigation

/**
 * 导航路由占位（开发文档 §2.2 UI 层六屏）。
 * 参数化路由（如 preview/{recordId}）在对应功能落地时补充。
 */
enum class Destinations(val route: String) {
    HOME("home"),
    TIMELINE("timeline"),
    CAPTURE("capture"),
    CONFIRM("confirm"),
    PREVIEW("preview"),
    SETTINGS("settings"),
}

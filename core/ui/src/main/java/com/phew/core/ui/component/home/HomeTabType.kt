package com.phew.core.ui.component.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.phew.core_common.log.SooumLog
import com.phew.core_design.R
import com.phew.core_design.icon.SooumIcon


private const val FEED_PREFIX = "feed"
private const val WRITE_PREFIX = "write"
private const val TAG_PREFIX = "tag"
private const val MY_PREFIX = "my"

enum class HomeTabType(
    val prefix: String,
    val graph: String,
    val route: String,
    @DrawableRes val selectedIconId: Int,
    @DrawableRes val unselectedIconId: Int,
    @StringRes val iconTextId: Int
) {
    FEED(
        prefix = FEED_PREFIX,
        graph = buildGraph(FEED_PREFIX),
        route = buildHomeRoute(FEED_PREFIX),
        selectedIconId = SooumIcon.HomeFilled.resId,
        unselectedIconId = SooumIcon.HomeFilled.resId,
        iconTextId = R.string.bottom_view_home
    ),

    WRITE(
       prefix = WRITE_PREFIX,
        graph = buildGraph(WRITE_PREFIX),
        route = buildHomeRoute(WRITE_PREFIX),
        selectedIconId = SooumIcon.WriteFilled.resId,
        unselectedIconId = SooumIcon.WriteFilled.resId,
        iconTextId = R.string.bottom_view_add_card
    ),

    TAG(
        prefix = TAG_PREFIX,
        graph = buildGraph(TAG_PREFIX),
        route = buildHomeRoute(TAG_PREFIX),
        selectedIconId = SooumIcon.TagFilled.resId,
        unselectedIconId = SooumIcon.TagFilled.resId,
        iconTextId = R.string.bottom_view_tag
    ),

    MY(
        prefix = MY_PREFIX,
        graph = buildGraph(MY_PREFIX),
        route = buildHomeRoute(MY_PREFIX),
        selectedIconId = SooumIcon.UserFilled.resId,
        unselectedIconId = SooumIcon.UserFilled.resId,
        iconTextId = R.string.bottom_view_my
    );

    companion object {
        const val SEPARATOR = "-"
        const val GRAPH = "graph"
        const val HOME_ROUTE = "home_route"

        fun HomeTabType.buildRoute(value: String): String = prefix + HomeTabType.SEPARATOR + value
        fun findHome(value: String?): HomeTabType {
            return if (!value.isNullOrEmpty()) {
                HomeTabType.entries.firstOrNull { value.startsWith(it.prefix) } ?: FEED
            } else {
                FEED
            }
        }
        fun isFeedHome(value: String?): Boolean {
            SooumLog.d("HomeTabType", "FEED.route: ${FEED.route}")
            if (value.isNullOrEmpty()) return false
            return value == FEED.route || value.startsWith(FEED.graph)
        }
    }
}

private fun buildGraph(prefix: String): String = prefix + HomeTabType.SEPARATOR + HomeTabType.GRAPH
private fun buildHomeRoute(prefix: String): String = prefix + HomeTabType.SEPARATOR + HomeTabType.HOME_ROUTE

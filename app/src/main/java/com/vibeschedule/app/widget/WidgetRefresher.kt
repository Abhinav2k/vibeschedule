package com.vibeschedule.app.widget

import android.content.Context
import com.vibeschedule.app.widget.VibeWidgetProvider

/** Small bridge used by UI code to refresh the widget after timer state changes. */
object WidgetRefresher {
    fun refresh(context: Context) = VibeWidgetProvider.updateAll(context)
}

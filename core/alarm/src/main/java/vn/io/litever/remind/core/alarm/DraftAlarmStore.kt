package vn.io.litever.remind.core.alarm

import vn.io.litever.remind.core.model.Alarm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftAlarmStore @Inject constructor() {
    private var draftAlarm: Alarm? = null

    fun setDraft(alarm: Alarm?) {
        draftAlarm = alarm
    }

    fun getDraft(): Alarm? = draftAlarm
}

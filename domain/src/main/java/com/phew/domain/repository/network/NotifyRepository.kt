package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification

interface NotifyRepository {
    suspend fun requestNotice(pageSize: Int, source: NoticeSource): DataResult<Pair<Int, List<Notice>>>
    suspend fun requestNoticePatch(lastId: Int, pageSize: Int, source: NoticeSource): DataResult<Pair<Int, List<Notice>>>
    suspend fun requestNotificationUnRead(): DataResult<Pair<Int, List<Notification>>>
    suspend fun requestNotificationUnReadPatch(lastId: Long): DataResult<Pair<Int, List<Notification>>>
    suspend fun requestNotificationRead(): DataResult<Pair<Int, List<Notification>>>
    suspend fun requestNotificationReadPatch(lastId: Long): DataResult<Pair<Int, List<Notification>>>
    suspend fun requestReadNotify(notifyId : Long) : Int
}
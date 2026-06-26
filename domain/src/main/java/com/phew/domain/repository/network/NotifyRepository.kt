package com.phew.domain.repository.network

import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification

interface NotifyRepository {
    suspend fun requestNotice(pageSize: Int, source: NoticeSource): Result<Pair<Int, List<Notice>>>
    suspend fun requestNoticePatch(lastId: Int, pageSize: Int, source: NoticeSource): Result<Pair<Int, List<Notice>>>
    suspend fun requestNotificationUnRead(): Result<Pair<Int, List<Notification>>>
    suspend fun requestNotificationUnReadPatch(lastId: Long): Result<Pair<Int, List<Notification>>>
    suspend fun requestNotificationRead(): Result<Pair<Int, List<Notification>>>
    suspend fun requestNotificationReadPatch(lastId: Long): Result<Pair<Int, List<Notification>>>
    suspend fun requestReadNotify(notifyId : Long) : Int
}
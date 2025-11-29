package com.phew.repository

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.dto.Notification
import com.phew.domain.repository.network.NotifyRepository
import com.phew.network.retrofit.NotifyHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class NotifyRepositoryImpl @Inject constructor(private val notifyHttp: NotifyHttp) :
    NotifyRepository {

    override suspend fun requestNotice(pageSize: Int, source: NoticeSource): DataResult<Pair<Int, List<Notice>>> {
        try {
            val request = notifyHttp.requestNotice(
                pageSize = pageSize,
                source = source.value
            )
            if (!request.isSuccessful) return DataResult.Fail(
                code = request.code(),
                message = request.message()
            )
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val body = request.body()!!
            if (body.notices.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            return DataResult.Success(
                Pair(request.code(), body.notices.map { data ->
                    Notice(
                        content = data.title,
                        url = data.url ?: "",
                        createdAt = data.createdAt,
                        id = data.id,
                        noticeType =data.noticeType
                    )
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNoticePatch(
        lastId: Int,
        pageSize: Int,
        source: NoticeSource,
    ): DataResult<Pair<Int, List<Notice>>> {
        try {
            val request = notifyHttp.requestNoticePatch(
                lastId = lastId,
                pageSize = pageSize,
                source = source.value
            )
            if (!request.isSuccessful) return DataResult.Fail(
                code = request.code(),
                message = request.message()
            )
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val body = request.body()!!
            if (body.notices.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            return DataResult.Success(
                Pair(request.code(), body.notices.map { data ->
                    data.toDomain()
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationUnRead(): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request = notifyHttp.requestNotificationUnRead()
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { response ->
                response.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationUnReadPatch(
        lastId: Long,
    ): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request =
                notifyHttp.requestNotificationUnReadPatch(lastId = lastId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { response ->
                response.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationRead(): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request =
                notifyHttp.requestNotificationRead()
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { response ->
                response.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationReadPatch(
        lastId: Long,
    ): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request = notifyHttp.requestNotificationReadPatch(lastId = lastId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { response ->
                response.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestReadNotify(notifyId: Long): Int{
        try{
            return notifyHttp.requestReadActivateNotify(lastId = notifyId).code()
        }catch (e: Exception){
            e.printStackTrace()
            return APP_ERROR_CODE
        }
    }
}
package com.phew.core_common

import com.phew.core_common.log.SooumLog
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtils {
    private val DEFAULT_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    
    /**
     * ISO 8601 형식의 날짜 문자열을 파싱하는 DateFormat (UTC 기준)
     * 마이크로초까지 지원
     */
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * 백업용 ISO 8601 포맷 (밀리초만 있는 경우)
     */
    private val iso8601FormatFallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    /**
     * 타이머 문자열을 파싱하여 밀리초로 변환
     * @param timerString "HH:mm:ss" 형식의 시간 문자열
     * @return 밀리초 단위의 시간, 파싱 실패시 0
     */
    fun parseTimerToMillis(timerString: String): Long {
        return try {
            val parts = timerString.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                val seconds = parts[2].toInt()
                TimeUnit.HOURS.toMillis(hours.toLong()) +
                TimeUnit.MINUTES.toMillis(minutes.toLong()) +
                TimeUnit.SECONDS.toMillis(seconds.toLong())
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 밀리초를 "HH:mm:ss" 형식으로 변환
     * @param millis 밀리초
     * @return "HH:mm:ss" 형식의 시간 문자열
     */
    fun formatMillisToTimer(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    /**
     * ISO 8601 날짜 문자열을 "yyyy년MM월dd일HH시mm분" 형식으로 포맷팅
     * @param dateString ISO 8601 형식의 날짜 문자열 (예: "2024-12-25T14:30:00.000Z")
     * @return "yyyy년MM월dd일HH시mm분" 형식의 문자열, 파싱 실패시 원본 반환
     */
    fun formatToKoreanDateTime(dateString: String): String {
        return try {
            if (dateString.isBlank()) {
                return dateString
            }
            
            // 기존 ISO 8601 파싱 로직 활용 (마이크로초 우선, 밀리초 폴백)
            val parsedTime = try {
                iso8601Format.parse(dateString)?.time
            } catch (e: Exception) {
                try {
                    iso8601FormatFallback.parse(dateString)?.time
                } catch (e: Exception) {
                    null
                }
            }
            
            if (parsedTime != null) {
                val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Seoul") // 한국 시간대로 출력
                }
                outputFormat.format(parsedTime)
            } else {
                SooumLog.w(TAG, "Failed to parse ISO 8601 date: $dateString")
                dateString
            }
        } catch (e: Exception) {
            SooumLog.w(TAG, "Failed to format date: $dateString, ${e.message}")
            dateString
        }
    }


    /**
     * ISO 8601 MM.dd 형식 출력
     */
    private val MM_DD_FORMATTER = DateTimeFormatter.ofPattern("MM월 d일")

    /**
     * ISO 8601 yyyy.MM.dd 형식 출력
     */
    private val DOT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    fun formatToDotDate(createAt: String): String {
        if (createAt.trim().isEmpty()) return ""
        return try {
            LocalDate.parse(createAt).format(DOT_DATE_FORMATTER)
        } catch (e: Exception) {
            e.printStackTrace()
            // 파싱 실패시 ISO 8601 원본에서 날짜 부분만 추출하여 포맷팅 시도
            try {
                LocalDateTime.parse(createAt, DateTimeFormatter.ISO_DATE_TIME).format(DOT_DATE_FORMATTER)
            } catch (e2: Exception) {
                e2.printStackTrace()
                ""
            }
        }
    }

    fun formatToDate(createAt: String): String {
        if (createAt.trim().isEmpty()) return ""
        return try {
            LocalDate.parse(createAt).format(MM_DD_FORMATTER)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * ISO 8601 년 월 일 만 출력
     */
    fun convertIsoToDateString(isoString: String): String {
        val parsedDate = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREA)
        return parsedDate.format(outputFormatter)
    }

    /**
     * ISO 8601 종료 시각에서 현재까지 남은 시간을 밀리초로 환산
     */
    fun remainingMillisUntil(expirationIsoString: String?): Long {
        if (expirationIsoString.isNullOrBlank()) return 0L
        val expirationMillis = parseExpirationMillis(expirationIsoString) ?: return 0L
        return (expirationMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    private fun parseExpirationMillis(value: String): Long? {
        // OffsetDateTime + timezone
        val offsetResult = runCatching {
            OffsetDateTime.parse(value).toInstant().toEpochMilli()
        }.getOrNull()
        if (offsetResult != null) return offsetResult

        // LocalDateTime with microseconds
        val microResult = runCatching {
            val normalized = if ('.' in value) {
                val (head, tail) = value.split('.', limit = 2)
                val fractional = tail.takeWhile { it.isDigit() }
                val padded = fractional.padEnd(6, '0')
                "$head.$padded"
            } else {
                "$value.000000"
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            LocalDateTime.parse(normalized, formatter)
                .atZone(DEFAULT_ZONE)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        if (microResult != null) return microResult

        // LocalDateTime with milliseconds
        val milliFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val milliResult = runCatching {
                LocalDateTime.parse(value, milliFormatter)
                    .atZone(DEFAULT_ZONE)
                    .toInstant()
                    .toEpochMilli()
        }.getOrNull()
        if (milliResult != null) return milliResult

        SooumLog.w(TAG, "Failed to parse expiration time: $value")
        return null
    }

    @JvmStatic
    fun formatToWithdrawalDate(dateString: String): String {
        try {
            // yyyy-MM-dd 형식 처리
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedTime = simpleDateFormat.parse(dateString)?.time
            
            if (parsedTime != null) {
                val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                return outputFormat.format(parsedTime)
            }
            
            // ISO 8601 형식도 시도
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val isoParsedTime = isoFormat.parse(dateString)?.time
            
            if (isoParsedTime != null) {
                val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                return outputFormat.format(isoParsedTime)
            }
            
            return dateString
        } catch (e: Exception) {
            SooumLog.w(TAG, "Failed to format withdrawal date: $dateString, ${e.message}")
            return dateString
        }
    }
    
    /**
     * createAt 시간을 기반으로 상대적 시간 표시
     * 8단계 시간 표기 정책:
     * 1. ~1분: "방금전"
     * 2. 1-9분: "N분전" 
     * 3. 10-59분: "N0분 전" (10단위 반올림)
     * 4. 1-23시간: "N시간 전"
     * 5. 1-6일: "N일 전"
     * 6. 7-29일: "N주 전" (특별 계산)
     * 7. 30-368일: "N개월 전"
     * 8. 369일+: "N년 전"
     * 
     * @param createAt ISO 8601 형식의 생성 시간
     * @return 정책에 맞는 상대적 시간 문자열
     */
    fun getRelativeTimeString(createAt: String): String {
        return try {
            // 빈 문자열 체크
            if (createAt.isBlank()) {
                SooumLog.w(TAG, "Empty createAt string provided")
                return ""
            }
            
            // 여러 포맷으로 파싱 시도
            val createdTime = parseExpirationMillis(createAt) ?: return createAt
            // 현재 시간도 UTC 기준으로 계산
            val currentTime = System.currentTimeMillis()
            val diffMillis = currentTime - createdTime
            
            val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            
            // --- FIX START ---
            // 날짜만 비교하여 일(day) 차이 계산 (시간 무시)
            val createdLocalDate = Instant.ofEpochMilli(createdTime)
                .atZone(ZoneOffset.UTC) // UTC 기준으로 날짜만 추출
                .toLocalDate()

            val currentLocalDate = Instant.ofEpochMilli(currentTime)
                .atZone(ZoneOffset.UTC) // UTC 기준으로 날짜만 추출
                .toLocalDate()

            val diffDaysBasedOnCalendar = ChronoUnit.DAYS.between(createdLocalDate, currentLocalDate)

            // 상세 디버깅 로그 (필요 시 다시 주석 제거)
//            SooumLog.d(TAG, "=== Time Calculation Debug ===")
//            SooumLog.d(TAG, "createAt input: $createAt")
//            SooumLog.d(TAG, "createdTime (UTC millis): $createdTime")
//            SooumLog.d(TAG, "currentTime (local millis): $currentTime")
//            SooumLog.d(TAG, "diffMillis: $diffMillis")
//            SooumLog.d(TAG, "diffMinutes: $diffMinutes")
//            SooumLog.d(TAG, "diffHours: $diffHours")
//            SooumLog.d(TAG, "diffDays (calendar-based): $diffDaysBasedOnCalendar") // 새로운 디버그 로그
//
//            // 현재 시간과 생성 시간을 Date 객체로 출력
//            SooumLog.d(TAG, "createdDate: ${Date(createdTime)}")
//            SooumLog.d(TAG, "currentDate: ${Date(currentTime)}")
//            SooumLog.d(TAG, "==============================")
            when {
                // 1. ~1분: "방금전"
                diffMinutes < 1 -> "방금전"
                
                // 2. 1-9분: "N분전"
                diffMinutes in 1..9 -> "${diffMinutes}분전"
                
                // 3. 10-59분: "N0분 전" (10단위 반올림)
                diffMinutes in 10..59 -> {
                    val roundedMinutes = (diffMinutes / 10) * 10
                    "${roundedMinutes}분 전"
                }
                
                // 4. 1-23시간: "N시간 전"
                diffHours in 1..23 -> "${diffHours}시간 전"
                
                // 5. 1-6일: "N일 전"
                diffDaysBasedOnCalendar in 1..6 -> "${diffDaysBasedOnCalendar}일 전"
                
                // 6. 7-29일: "N주 전" (특별 계산)
                diffDaysBasedOnCalendar in 7..29 -> {
                    val weeks = when (diffDaysBasedOnCalendar) {
                        in 7..7 -> 1      // 7일: 1주 전
                        in 8..14 -> 2     // 8-14일: 2주 전
                        in 15..21 -> 3    // 15-21일: 3주 전
                        in 22..29 -> 4    // 22-29일: 4주 전
                        else -> 1 // 이 경우는 발생하지 않아야 함
                    }
                    "${weeks}주 전"
                }
                
                // 7. 30-368일: "N개월 전"
                diffDaysBasedOnCalendar in 30..368 -> {
                    val months = diffDaysBasedOnCalendar / 30
                    "${months}개월 전"
                }
                
                // 8. 369일 이후: "N년 전"
                else -> {
                    val years = diffDaysBasedOnCalendar / 365
                    "${years}년 전"
                }
            }
        } catch (e: Exception) {
            createAt
        }
    }
}

private const val TAG = "TimeUtils"

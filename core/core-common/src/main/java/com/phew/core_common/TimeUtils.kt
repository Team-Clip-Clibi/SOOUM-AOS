package com.phew.core_common

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {
    
    /**
     * ISO 8601 형식의 날짜 문자열을 파싱하는 DateFormat
     */
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
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
            val createdTime = iso8601Format.parse(createAt)?.time ?: return createAt
            val currentTime = System.currentTimeMillis()
            val diffMillis = currentTime - createdTime
            
            val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
            
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
                
                // 4. 1-23시간 59분: "N시간 전"
                diffHours in 1..23 -> "${diffHours}시간 전"
                
                // 5. 1-6일: "N일 전"
                diffDays in 1..6 -> "${diffDays}일 전"
                
                // 6. 7-29일: "N주 전" (특별 계산)
                diffDays in 7..29 -> {
                    val weeks = when (diffDays) {
                        in 7..7 -> 1      // 7일: 1주 전
                        in 8..14 -> 2     // 8-14일: 2주 전
                        in 15..21 -> 3    // 15-21일: 3주 전
                        in 22..29 -> 4    // 22-29일: 4주 전
                        else -> 1
                    }
                    "${weeks}주 전"
                }
                
                // 7. 30-368일: "N개월 전"
                diffDays in 30..368 -> {
                    val months = diffDays / 30
                    "${months}개월 전"
                }
                
                // 8. 369일 이후: "N년 전"
                else -> {
                    val years = diffDays / 365
                    "${years}년 전"
                }
            }
        } catch (e: Exception) {
            createAt
        }
    }
}
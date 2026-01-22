package com.phew.core.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.phew.core.ui.R

object InquiryUtils {
    
    /**
     * 문의 메일 앱을 열어 숨 팀에 문의 메일을 보낼 수 있도록 합니다.
     * @param context Android Context
     * @param refreshToken 현재 사용자의 리프레시 토큰
     */
    fun openInquiryMail(
        context: Context,
        refreshToken: String
    ) {
        val emailAddress = context.getString(R.string.inquiry_email_address)
        val emailSubject = context.getString(R.string.inquiry_email_subject)
        val emailBody = context.getString(
            R.string.inquiry_email_body,
            refreshToken
        )
        val chooserTitle = context.getString(R.string.inquiry_email_chooser_title)
        val noClientMessage = context.getString(R.string.inquiry_email_client_not_found)

        val gmailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
            `package` = "com.google.android.gm"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(gmailIntent)
        } catch (gmailNotFound: ActivityNotFoundException) {
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(Intent.createChooser(fallbackIntent, chooserTitle))
            } catch (noEmailClient: Exception) {
                Toast.makeText(context, noClientMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
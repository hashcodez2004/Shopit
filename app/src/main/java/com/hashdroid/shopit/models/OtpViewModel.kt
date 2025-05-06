package com.hashdroid.shopit.models

import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OtpViewModel : ViewModel() {

    private val _resendText = MutableLiveData<SpannableString>()
    val resendText: LiveData<SpannableString> = _resendText

    private var timer: CountDownTimer? = null
    private val totalTime = 30_000L // 30 seconds
    private var onResendClick: (() -> Unit)? = null
    private var isTimerRunning = false


    fun startResendTimer(onResendClick: () -> Unit) {
        if (isTimerRunning) return

        isTimerRunning = true
        this.onResendClick = onResendClick
        timer?.cancel()

        timer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val fullText = "Resend OTP in $seconds second(s)"
                val spannable = SpannableString(fullText)

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    fullText.indexOf("$seconds second(s)"),
                    fullText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                _resendText.postValue(spannable)
            }

            override fun onFinish() {
                val fullText = "Resend OTP"
                val spannable = SpannableString(fullText)

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onResendClick?.invoke()
                        startResendTimer(onResendClick!!)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                        ds.color = ds.linkColor
                    }
                }

                spannable.setSpan(
                    clickableSpan,
                    0,
                    fullText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                _resendText.postValue(spannable)
            }
        }

        timer?.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}

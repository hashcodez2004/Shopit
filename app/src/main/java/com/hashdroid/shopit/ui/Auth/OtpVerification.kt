package com.hashdroid.shopit.ui.Auth

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hashdroid.shopit.R
import com.hashdroid.shopit.databinding.FragmentForgotPasswordBinding
import com.hashdroid.shopit.databinding.FragmentOtpVerificationBinding
import com.hashdroid.shopit.models.ForgotPasswordViewModel

class OtpVerification : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentOtpVerificationBinding
    private val viewModel: ForgotPasswordViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtpVerificationBinding.inflate(inflater,container,false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupSpannableClickListener()

        // ✅ Always reassign resend action after rotation
        viewModel.startResendTimer {
            Toast.makeText(requireContext(), "Resend OTP clicked", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_OTP_SignUp_to_details_SignUp)
        }
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val desiredHeight = (screenHeight * 0.60).toInt() // 60% of screen height
            it.layoutParams.height = desiredHeight
            it.requestLayout()

            val color = ContextCompat.getColor(requireContext(), R.color.color1)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(color))


        }
    }

    private fun setupSpannableClickListener() {
        viewModel.resendText.observe(viewLifecycleOwner) { spannable ->
            binding.tvResendOTP.text = spannable
            binding.tvResendOTP.movementMethod = LinkMovementMethod.getInstance()
            binding.tvResendOTP.highlightColor = Color.TRANSPARENT
        }

        // 🟡 Optional: force re-binding last value in case LiveData already has data
        viewModel.resendText.value?.let { spannable ->
            binding.tvResendOTP.text = spannable
            binding.tvResendOTP.movementMethod = LinkMovementMethod.getInstance()
            binding.tvResendOTP.highlightColor = Color.TRANSPARENT
        }
    }
}
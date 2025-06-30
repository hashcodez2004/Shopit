package com.hashdroid.shopit.ui.Auth

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hashdroid.shopit.R
import com.hashdroid.shopit.databinding.FragmentOtpSignupBinding
import com.hashdroid.shopit.models.OtpViewModel

class OTP_SignUp : Fragment() {

    private lateinit var binding: FragmentOtpSignupBinding
    private val viewModel: OtpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpSignupBinding.inflate(inflater, container, false)
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

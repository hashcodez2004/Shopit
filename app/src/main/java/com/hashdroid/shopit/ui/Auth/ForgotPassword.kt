package com.hashdroid.shopit.ui.Auth

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hashdroid.shopit.databinding.FragmentForgotPasswordBinding
import com.hashdroid.shopit.models.ForgotPasswordViewModel



class ForgotPassword : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val desiredHeight = (screenHeight * 0.75).toInt() // 75% of screen height
            it.layoutParams.height = desiredHeight
            it.requestLayout()
        }
    }
}
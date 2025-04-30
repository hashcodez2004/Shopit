package com.hashdroid.shopit

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hashdroid.shopit.databinding.FragmentSignInBinding
import com.hashdroid.shopit.databinding.FragmentSignUpBinding

class SignUp : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //making "register" bold using spannable string
        val fullText = "Already have an account? Sign In"
        val boldWord = "Sign In"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(boldWord)
        val end = start + boldWord.length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tv3.text = spannable
    }
}
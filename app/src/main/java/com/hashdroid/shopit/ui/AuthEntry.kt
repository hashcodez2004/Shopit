package com.hashdroid.shopit.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hashdroid.shopit.R
import com.hashdroid.shopit.databinding.FragmentAuthEntryBinding


class AuthEntry : Fragment() {
    private var _binding: FragmentAuthEntryBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentAuthEntryBinding.inflate(inflater, container, false)

        binding.btnNewHere.setOnClickListener{
            findNavController().navigate(R.id.action_authEntry_to_signUp)
        }

        binding.btnAlreadyMember.setOnClickListener {
            findNavController().navigate(R.id.action_authEntry_to_signIn)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
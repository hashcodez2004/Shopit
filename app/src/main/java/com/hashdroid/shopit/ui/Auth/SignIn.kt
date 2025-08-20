package com.hashdroid.shopit.ui.Auth

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hashdroid.shopit.R
import com.hashdroid.shopit.databinding.FragmentSignInBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignIn : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    // Firebase auth
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val REQUEST_CODE_SIGN_IN = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // --- (spannable text & forgot password) ---
        val fullText = "Don't have an account? Register"
        val boldWord = "Register"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(boldWord)
        val end = start + boldWord.length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tv2.text = spannable

        binding.tvForgot.setOnClickListener {
            val bottomSheet = ForgotPassword()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }


        // Mirror your MainActivity approach: build options+client on click and use startActivityForResult
        binding.btnGoogleSignIn.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // NOTE: use the same string name you used in MainActivity; here I'm using webclient_id
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val signInClient = GoogleSignIn.getClient(requireActivity(), options)
            signInClient.signInIntent.also {
                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
            }
        }
    }

    // This follows the same logic as your MainActivity version
    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Successfully logged in", Toast.LENGTH_LONG).show()
                    // TODO: navigate to next screen or update UI
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), e.message ?: "Auth error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            // This matches the exact pattern you provided: use .result
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
                account?.let {
                    googleAuthForFirebase(it)
                } ?: run {
                    Toast.makeText(requireContext(), "Google account is null", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == REQUEST_CODE_SIGN_IN) {
            // user cancelled or other non-OK result
            Toast.makeText(requireContext(), "Sign in cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

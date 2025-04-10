package aarambh.apps.safepay_apk.auth

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.Utils
import aarambh.apps.safepay_apk.databinding.FragmentSignInBinding
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import aarambh.apps.safepay_apk.OrderRepository
import aarambh.apps.safepay_apk.api.RetrofitClient

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val auth = FirebaseAuth.getInstance()
    private val repository = OrderRepository(RetrofitClient.orderApiService)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        setStatusBarColor()

        getUserNumber()

        onContinueButtonClick()


        return binding.root
    }

    private fun onContinueButtonClick() {
        binding.btnContinue.setOnClickListener {
            val number = binding.etUserNumber.text.toString()
            if(number.isEmpty() || number.length != 10 || !number.all { it.isDigit() }){
                Utils.showToast(requireContext(), "Please enter valid 10-digit mobile number")
            }
            else{
                val bundle = Bundle()
                bundle.putString("number", number)
                findNavController().navigate(R.id.action_signInFragment_to_OTPFragment, bundle)
            }
        }


    }

    private fun getUserNumber() {
        binding.etUserNumber.addTextChangedListener ( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val len = number?.length
                if (len == 10){
                    binding.btnContinue.isEnabled = true
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(),
                        R.color.blue
                    ))
                }else{
                    binding.btnContinue.isEnabled = false
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(),
                        R.color.grayb
                    ))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            statusBarColor = Color.TRANSPARENT // Set the status bar color to transparent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun updateFcmToken() {
        auth.currentUser?.uid?.let { uid ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("SignInFragment", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d("SignInFragment", "FCM Token: $token")

                // Send token to your server
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.updateFcmToken(uid, token).fold(
                        onSuccess = { success ->
                            if (success) {
                                Log.d("SignInFragment", "Successfully updated FCM token")
                            } else {
                                Log.e("SignInFragment", "Failed to update FCM token")
                            }
                        },
                        onFailure = { exception ->
                            Log.e("SignInFragment", "Error updating FCM token", exception)
                        }
                    )
                }
            }
        }
    }

    // Call this method after successful sign in
    private fun onSignInSuccess() {
        updateFcmToken()
        // ... rest of your sign in success handling
    }
}
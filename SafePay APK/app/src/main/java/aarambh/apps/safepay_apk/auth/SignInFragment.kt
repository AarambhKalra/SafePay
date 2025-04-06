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

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding

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
}
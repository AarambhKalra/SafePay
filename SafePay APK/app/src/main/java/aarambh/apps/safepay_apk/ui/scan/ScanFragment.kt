package aarambh.apps.safepay_apk.ui.scan

import aarambh.apps.safepay_apk.viewmodels.OrderViewModel
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ScanFragment : Fragment() {
    private lateinit var storageRef: StorageReference
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageRef = FirebaseStorage.getInstance().reference
    }

    private suspend fun uploadVideoToFirebase(videoUri: Uri, orderId: String) {
        try {
            val videoRef = storageRef.child("videos/$orderId/${System.currentTimeMillis()}.mp4")
            val uploadTask = videoRef.putFile(videoUri)
            
            uploadTask.addOnSuccessListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val downloadUrl = videoRef.downloadUrl.await().toString()
                        viewModel.setVideoUrl(downloadUrl)
                        Toast.makeText(context, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to get video URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to upload video: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error uploading video: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleVideoRecordingComplete(videoUri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedOrder.value?.let { order ->
                uploadVideoToFirebase(videoUri, order.orderId)
            } ?: run {
                Toast.makeText(context, "No order selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearMediaUrls()
    }
} 
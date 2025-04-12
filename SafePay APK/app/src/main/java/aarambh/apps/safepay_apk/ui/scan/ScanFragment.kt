package aarambh.apps.safepay_apk.ui.scan

import aarambh.apps.safepay_apk.viewmodels.OrderViewModel
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class ScanFragment : Fragment() {
    private val TAG = "SafePay_Scan"
    private lateinit var storageRef: StorageReference
    private val viewModel: OrderViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageRef = FirebaseStorage.getInstance().reference
        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe scan upload status
                launch {
                    viewModel.scanUploadStatus.collect { success ->
                        success?.let {
                            if (it) {
                                Toast.makeText(context, "Video successfully uploaded and sent to API", Toast.LENGTH_SHORT).show()
                                // Navigate back or show success UI
                            } else {
                                Toast.makeText(context, "Failed to process video", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun uploadVideoToFirebase(videoUri: Uri, orderId: String) {
        try {
            Log.d(TAG, "Starting video upload for order: $orderId")
            val videoRef = storageRef.child("videos/$orderId/${System.currentTimeMillis()}.mp4")
            
            // Start the upload
            val uploadTask = videoRef.putFile(videoUri)

            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d(TAG, "Upload progress: $progress%")
            }

            try {
                // Wait for upload to complete
                uploadTask.await()
                
                // Get download URL
                val downloadUrl = videoRef.downloadUrl.await().toString()
                Log.d(TAG, "Upload successful. Download URL: $downloadUrl")
                
                // Send URL to API through ViewModel
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        Log.d(TAG, "Sending video URL to API for order: $orderId")
                        viewModel.verifyOrder(orderId, downloadUrl)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending URL to API", e)
                        Toast.makeText(context, "Failed to process video URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting download URL", e)
                Toast.makeText(context, "Failed to get video URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in upload process", e)
            Toast.makeText(context, "Error uploading video: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleVideoRecordingComplete(videoUri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedOrder.value?.let { order ->
                Log.d(TAG, "Video recording complete. Processing for order: ${order.orderId}")
                uploadVideoToFirebase(videoUri, order.orderId)
            } ?: run {
                Log.e(TAG, "No order selected for video upload")
                Toast.makeText(context, "No order selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearMediaUrls()
        viewModel.resetScanUploadStatus()
    }
} 
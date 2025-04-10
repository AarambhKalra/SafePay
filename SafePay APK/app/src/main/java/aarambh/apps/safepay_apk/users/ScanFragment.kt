package aarambh.apps.safepay_apk.users

import aarambh.apps.safepay_apk.databinding.FragmentScanBinding
import aarambh.apps.safepay_apk.viewmodels.OrderViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.MediaController
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderViewModel by activityViewModels()

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var recordedVideoUri: Uri? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupButtons()
    }

    private fun setupButtons() {
        binding.recordBtn.setOnClickListener {
            if (recording != null) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        binding.retakeBtn.setOnClickListener {
            recordedVideoUri = null
            binding.videoView.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
            binding.retakeBtn.visibility = View.GONE
            binding.sendBtn.visibility = View.GONE
            binding.recordBtn.visibility = View.VISIBLE
            startCamera()
        }

        binding.sendBtn.setOnClickListener {
            recordedVideoUri?.let {
                Toast.makeText(requireContext(), "Uploading video...", Toast.LENGTH_SHORT).show()
                uploadVideoToFirebase(it)
            } ?: Toast.makeText(requireContext(), "No video to send", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        cameraProvider?.unbindAll()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setTargetRotation(binding.previewView.display.rotation)
                    .build()

                preview.setSurfaceProvider(binding.previewView.surfaceProvider)

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )

                Log.d(TAG, "Camera initialized successfully")

            } catch (exc: Exception) {
                Log.e(TAG, "Camera initialization failed: ${exc.message}", exc)
                Toast.makeText(requireContext(), "Camera failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return
        binding.recordBtn.text = "Stop Recording"

        val videoFile = File(requireContext().cacheDir, "SafePay-${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        try {
            val pendingRecording = videoCapture.output
                .prepareRecording(requireContext(), outputOptions)

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pendingRecording.withAudioEnabled()
            }

            recording = pendingRecording
                .start(ContextCompat.getMainExecutor(requireContext())) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            Log.d(TAG, "Recording started")
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!event.hasError()) {
                                recordedVideoUri = Uri.fromFile(videoFile)
                                showRecordedVideo()
                                Log.d(TAG, "Recording completed: $recordedVideoUri")
                            } else {
                                recording?.close()
                                recording = null
                                Log.e(TAG, "Video capture failed: ${event.error}")
                                Toast.makeText(requireContext(), "Recording failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "Recording error: ${e.message}", e)
            Toast.makeText(requireContext(), "Start failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        binding.recordBtn.text = "Start Recording"
        recording?.stop()
        recording = null
    }

    private fun showRecordedVideo() {
        recordedVideoUri?.let { uri ->
            binding.videoView.apply {
                setVideoURI(uri)
                setMediaController(MediaController(requireContext()))
                visibility = View.VISIBLE
                start()
            }
            binding.previewView.visibility = View.GONE
            binding.recordBtn.visibility = View.GONE
            binding.retakeBtn.visibility = View.VISIBLE
            binding.sendBtn.visibility = View.VISIBLE
        }
    }

    private fun uploadVideoToFirebase(uri: Uri) {
        binding.uploadProgress.visibility = View.VISIBLE
        val storageRef = FirebaseStorage.getInstance().reference
        val videoRef = storageRef.child("scan_videos/${UUID.randomUUID()}.mp4")

        videoRef.putFile(uri)
            .addOnSuccessListener {
                binding.uploadProgress.visibility = View.GONE
                Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_SHORT).show()
                viewModel.setScanUploadStatus(true)

                // Delete the file after successful upload
                File(uri.path!!).delete()

                findNavController().navigateUp()
            }
            .addOnFailureListener {
                binding.uploadProgress.visibility = View.GONE
                Toast.makeText(requireContext(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraProvider?.unbindAll()
    }

    companion object {
        private const val TAG = "ScanFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}

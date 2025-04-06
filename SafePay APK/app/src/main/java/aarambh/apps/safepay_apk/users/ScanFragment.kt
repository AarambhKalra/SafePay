package aarambh.apps.safepay_apk.users

import aarambh.apps.safepay_apk.databinding.FragmentScanBinding
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
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.*

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var recordedVideoUri: Uri? = null
    private var recordedVideoFile: File? = null

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
            recordedVideoFile?.let {
                uploadVideoToFirebase(it)
            } ?: Toast.makeText(requireContext(), "No video to send", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, videoCapture)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return
        val file = File(requireContext().cacheDir, "${System.currentTimeMillis()}.mp4")
        recordedVideoFile = file

        val outputOptions = FileOutputOptions.Builder(file).build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), outputOptions)
            .start(ContextCompat.getMainExecutor(requireContext())) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        binding.recordBtn.text = "Stop Recording"
                        Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
                    }
                    is VideoRecordEvent.Finalize -> {
                        recording = null
                        if (event.hasError()) {
                            Toast.makeText(requireContext(), "Recording failed", Toast.LENGTH_SHORT).show()
                        } else {
                            recordedVideoUri = Uri.fromFile(file)
                            showVideoPreview(recordedVideoUri!!)
                        }
                        binding.recordBtn.text = "Start Recording"
                    }
                }
            }
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    private fun showVideoPreview(uri: Uri) {
        binding.previewView.visibility = View.GONE
        binding.videoView.apply {
            setVideoURI(uri)
            setMediaController(MediaController(requireContext()))
            requestFocus()
            start()
            visibility = View.VISIBLE
        }

        binding.recordBtn.visibility = View.GONE
        binding.retakeBtn.visibility = View.VISIBLE
        binding.sendBtn.visibility = View.VISIBLE
    }

    private fun uploadVideoToFirebase(videoFile: File) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val videoRef = storageRef.child("verification_videos/${UUID.randomUUID()}.mp4")
        val uri = Uri.fromFile(videoFile)

        val uploadTask = videoRef.putFile(uri)
        Toast.makeText(requireContext(), "Uploading... \n Please Wait", Toast.LENGTH_SHORT).show()

        uploadTask.addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Toast.makeText(requireContext(), "Uploaded successfully", Toast.LENGTH_SHORT).show()
                Log.d("Upload", "Video URL: $downloadUri")

                // Send success flag back
                parentFragmentManager.setFragmentResult("upload_result", Bundle().apply {
                    putBoolean("upload_success", true)
                })


                // Navigate back to OrderInfoFragment
                findNavController().navigateUp()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}

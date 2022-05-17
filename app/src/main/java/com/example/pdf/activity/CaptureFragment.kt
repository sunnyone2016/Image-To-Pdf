package com.example.pdf.activity


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pdf.R
import com.example.pdf.helper.UploadUtility
import com.example.pdf.models.PickImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File


class CaptureFragment : Fragment() {
    private lateinit var pickImageTV: TextView
    private lateinit var imageView: ImageView
    private lateinit var nextStep:TextView
    var croppedImage: Uri? = null
    var select:ImageView?=null
    var pickImage=ArrayList<PickImage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.select_fragment, container, false)
    }
    override  fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickImageTV = view.findViewById<View>(R.id.imageTextView) as TextView
        imageView = view.findViewById<View>(R.id.ImageView) as ImageView
        nextStep= view.findViewById(R.id.nextStep) as TextView
        select=view.findViewById(R.id.select)
        // Setting click listener to the image TextView
        pickImageTV.setOnClickListener { selectImage() }
        imageView.setOnClickListener { selectImage() }
        nextStep.setOnClickListener {
            startIntent()
        }
        select!!.setOnClickListener { selectImage() }
    }

    override fun onResume() {
        super.onResume()
//        pickImage.clear()
    }
    private fun selectImage() {
        // Creating AlertDialog
//        val choice = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
//        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
//        myAlertDialog.setTitle("Select Image")
//        myAlertDialog.setItems(choice, DialogInterface.OnClickListener { dialog, item ->
//            when {
//                choice[item] == "Choose from Gallery" -> {
//                    val pickFromGallery = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                    pickFromGallery.type = "image/*"
//                    startActivityForResult(pickFromGallery, 1)
//
//                }
//                choice[item] == "Take Photo" -> {
//
//                    var cameraPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
//                    try {
//                        startActivityForResult(cameraPicture, 0)
//                    }
//                   catch (e:Exception){
//                       Toast.makeText(activity,"Can't access camera",Toast.LENGTH_SHORT).show()
//                   }
//                }
//                choice[item] == "Cancel" -> {
//                    myAlertDialog.setCancelable(true)
//                }
//            }
//        })
//        myAlertDialog.show()
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(requireContext(), this);
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
//                0 -> if (resultCode == Activity.RESULT_OK && data != null) {
//
//                    val imageCapture = data.extras!!["data"] as Bitmap?
//                    val bytes = ByteArrayOutputStream()
//                    imageCapture?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//                    val path = Uri.parse(
//                        MediaStore
//                        .Images
//                        .Media
//                        .insertImage(context?.contentResolver, imageCapture, Calendar.getInstance().get(Calendar.MILLISECOND).toString(), null)
//                        .toString())
//                    CropImage.activity(path)
//                        .start(requireContext(), this);
////                    imageView.setImageBitmap(imageCapture)
//
//                }
//                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
//                    val uri= data.data
//                    CropImage.activity(uri)
//                        .start(requireContext(), this);
////                    val input = data.data?.let { activity?.contentResolver?.openInputStream(it) }
////                    imageView.setImageBitmap(BitmapFactory.decodeStream(input,null,BitmapFactory.Options()))
////                    nextStep.text="Next >>"
//                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE->{
                    val result = CropImage.getActivityResult(data)
                    if (resultCode === Activity.RESULT_OK) {
                        croppedImage = result.uri
                        select?.setImageURI(croppedImage)
                        pickImage.add(PickImage(croppedImage.toString()))
                        pickImageTV.text=""
                        nextStep.text="${pickImage.size} ảnh được chọn! Tiếp >>"

                        imageView?.visibility=View.GONE
                    } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Toast.makeText(activity,result.error.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun startIntent(){
            activity?.let {
                MaterialAlertDialogBuilder(it)
                    .setMessage("Bạn có muốn sử dụng OCR để tạo PDF ?")
                    .setNeutralButton("thoát") { dialog, which ->
                        dialog.cancel();
                    }
                    .setPositiveButton("có") { dialog, which ->
                        val file = File(croppedImage?.path)
                        val sharedPref: SharedPreferences =
                            requireContext().getSharedPreferences("PDF",
                                AppCompatActivity.MODE_PRIVATE
                            )
                        val key = sharedPref.getString("key","").toString()
                        if (key!=""||key!=null){
                            UploadUtility(this.requireActivity()).uploadFile(file,key = key)
                        }
                        pickImage.clear()
                    }
                    .setNegativeButton("không") { dialog, which ->
                        var intent=Intent(this.activity,CreateActivity::class.java)
                        intent.putExtra("image",Gson().toJson(pickImage))
                        startActivity(intent)
                        pickImage.clear()
                    }
                    .show()
            }

    }

}
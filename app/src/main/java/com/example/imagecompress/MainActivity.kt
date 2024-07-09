package com.example.imagecompress

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var button: Button

    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.image_view)
        button = findViewById(R.id.btn_select_image)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading image...")

        button.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                pickImage.launch(Intent(MediaStore.ACTION_PICK_IMAGES))
            }else{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                pickImage.launch(intent)
            }

        }
    }


    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == RESULT_OK && it.data != null){
            val uri = it.data!!.data
            imageView.setImageURI(uri)
            uploadImage(uri!!)
        }
    }

    private fun uploadImage(uri:Uri){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Upload image")
        progressDialog.setCancelable(false)
        progressDialog.show()
        FirebaseStorage.getInstance().getReference("compress-image")
            .child("${System.currentTimeMillis()}.jpeg")
            .putBytes(compressImage(uri))
            .addOnSuccessListener {
                Toast.makeText(this,
                    "Image upload.",
                    Toast.LENGTH_LONG
                ).show()
                progressDialog.cancel()
            }.addOnFailureListener {
                Toast.makeText(this,
                    "Error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
                progressDialog.cancel()
            }
    }

    private fun compressImage(uri: Uri):ByteArray{
        val stream = ByteArrayOutputStream()
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            bitmap.compress(Bitmap.CompressFormat.JPEG,60,stream)
            return stream.toByteArray()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return stream.toByteArray()
    }

}
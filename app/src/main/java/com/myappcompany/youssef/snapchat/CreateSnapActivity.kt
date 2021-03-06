package com.myappcompany.youssef.snapchat

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*


class CreateSnapActivity : AppCompatActivity() {

    var createSnapImageView: ImageView? = null
    var captionEditText: EditText? = null

    val imageName = UUID.randomUUID().toString()+".jpg"

    fun selectImage(view: View){
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            getPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val selectedImage = data!!.data

        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){
            try{
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImage)
                createSnapImageView?.setImageBitmap(bitmap)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun getPhoto() {
        val intent = Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    fun nextClicked(view: View){
        // Get the data from an ImageView as bytes
        createSnapImageView?.setDrawingCacheEnabled(true)
        createSnapImageView?.buildDrawingCache()
        val bitmap = (createSnapImageView?.getDrawable() as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        //Store process
        val uploadTask: UploadTask = FirebaseStorage.getInstance().reference.child("images").child(imageName).putBytes(data)
        uploadTask.addOnFailureListener(OnFailureListener {
            Toast.makeText(this,"Uploading failed.",Toast.LENGTH_SHORT).show()
        }).addOnSuccessListener( OnSuccessListener<UploadTask.TaskSnapshot>() {taskSnapshot ->
            var imageURL: String? = ""
            if (taskSnapshot.metadata != null) {
                if (taskSnapshot.metadata!!.reference != null) {
                    var result: Task<Uri> = taskSnapshot.storage.downloadUrl;
                    result.addOnSuccessListener(OnSuccessListener<Uri>() { url ->
                        imageURL = url.toString()
                        Toast.makeText(this,"Uploading success.",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this,ChooseUserActivity::class.java)
                        intent.putExtra("imageURL",imageURL)
                        intent.putExtra("imageName",imageName)
                        intent.putExtra("message",captionEditText?.text.toString())
                        startActivity(intent)
                    })
                }
            }

        })


        /*val uploadTask = FirebaseStorage.getInstance().getReference().child("images").child(imageName).putBytes(data)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this,"Uploading failed.",Toast.LENGTH_SHORT).show()
        }).addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> {taskSnapsot ->
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            //var result: Task<Uri> = taskSnapsot.storage.downloadUrl
            Toast.makeText(this,"Uploading success.",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,ChooseUserActivity::class.java)
            intent.putExtra("imageURL",taskSnapsot.storage.downloadUrl.toString())
            intent.putExtra("imageName",imageName)
            intent.putExtra("message",captionEditText?.text.toString())
            startActivity(intent)
        })*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_snap)

        createSnapImageView = findViewById(R.id.createSnapImageView)
        captionEditText = findViewById(R.id.captionEditText)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto()
            }
        }
    }
}

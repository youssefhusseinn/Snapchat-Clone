package com.myappcompany.youssef.snapchat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.net.HttpURLConnection
import java.net.URL

class ViewSnapActivity : AppCompatActivity() {

    var textView: TextView? = null
    var imageView: ImageView? = null
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snap)

        textView = findViewById(R.id.textView)
        imageView = findViewById(R.id.imageView)

        textView?.text = intent.getStringExtra("message")
        val task = ImageDownloader()
        try {
            val myImage = task.execute(intent.getStringExtra("imageURL")).get()
            imageView?.setImageBitmap(myImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    inner class ImageDownloader : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            try {
                val url = URL(urls[0])
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val `in` = connection.inputStream
                return BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(mAuth.currentUser!!.uid)
                .child("snaps")
                .child(intent.getStringExtra("snapKey"))
                .removeValue()
        FirebaseStorage
                .getInstance()
                .getReference()
                .child("images")
                .child(intent.getStringExtra("imageName"))
                .delete()

    }
}

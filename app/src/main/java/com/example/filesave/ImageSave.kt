package com.example.filesave

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream


@Composable
fun ImageSave() {
    val context = LocalContext.current
    var filename by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var readText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = readText)
        TextField(value = filename, onValueChange = {
            filename = it
        }, label = {
            Text(text = "Enter file name")
        })

        TextField(value = content, onValueChange = {
            content = it
        }, label = {
            Text(text = "Enter content")
        })

        Button(onClick = {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pic)

            if (bitmap != null) {
                readText = saveImage(context, filename, bitmap)
                Toast.makeText(context, "File saved successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error: Bitmap could not be decoded", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Save Image")
        }
    }
}

private fun saveImage(context: Context, filename: String, bitmap: Bitmap): String {
    val fullFilename = if (filename.endsWith(".png")) filename else "$filename.png"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fullFilename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = resolver.insert(collection, contentValues)

        itemUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)


        }

        return itemUri.toString()
    } else {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fullFilename)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }




        return file.absolutePath
    }
}


package com.example.filesave

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.core.content.FileProvider
import com.example.filesave.ui.theme.FileSaveTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FileSaveTheme {
                ImageVideoTextSave()
            }
        }
    }
}


@Composable
fun SaveTextFileUi() {
    val context = LocalContext.current
    var filename by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var readText by remember {
        mutableStateOf("")
    }

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
            readText = saveTextFile(context, filename, content)
            Toast.makeText(context, "File saved successfully!", Toast.LENGTH_LONG).show()
        }) {
            Text(text = "Save Text")
        }
    }


}


private fun saveTextFile(context: Context, filename: String, content: String): String {
    val fullFilename = if (filename.endsWith(".txt")) filename else "$filename.txt"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fullFilename)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = resolver.insert(collection, contentValues)

        itemUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            openFile(context, uri, "text/plain")
        }

        return itemUri.toString()
    } else {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fullFilename)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        openFile(context, uri, "text/plain")

        return file.absolutePath
    }
}

private fun openFile(context: Context, uri: Uri, mimetype: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimetype)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }


}

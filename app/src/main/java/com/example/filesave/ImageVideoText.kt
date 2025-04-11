package com.example.filesave

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.FileOutputStream


@Composable
fun ImageVideoTextSave() {
    val context = LocalContext.current
    var filename by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var readText by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
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

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            readText = saveTextFile(context, filename, content)
            Toast.makeText(context, "File saved successfully!", Toast.LENGTH_LONG).show()
        }) {
            Text(text = "Save Text")
        }
        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.pic),
            contentDescription = "",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pic)
            imageFile(context, filename, bitmap)
            Toast.makeText(context, "File saved successfully!", Toast.LENGTH_LONG).show()
        }) {
            Text(text = "Image Saved ")
        }
        Spacer(modifier = Modifier.height(20.dp))
        val EXAMPLE_VIDEO_URI =
            "https://youtube.com/shorts/KCju8tywZpY?si=lKS0Hzme_4smDziL"
        val context = LocalContext.current

        val exoPlayer = remember {
            ExoPlayer.Builder(context).build()
        }


        LaunchedEffect(EXAMPLE_VIDEO_URI) {
            val mediaItem = MediaItem.fromUri(EXAMPLE_VIDEO_URI)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            val fileName = "big_buck_bunny"
            downloadVideoFromUrl(context, videoUrl, fileName)
        }) {
            Text(text = "Download Video from URL")
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

        }

        return itemUri.toString()
    } else {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fullFilename)
        file.writeText(content)

        return file.absolutePath
    }
}

private fun imageFile(context: Context, filename: String, bitmap: Bitmap): String {
    val fullFilename = if (filename.endsWith(".png")) filename else "$filename.png"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fullFilename)
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = resolver.insert(collection, contentValues)

        itemUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

        }

        return itemUri.toString()
    } else {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fullFilename)




        return file.absolutePath
    }
}

fun downloadVideoFromUrl(context: Context, videoUrl: String, fileName: String) {
    val request = DownloadManager.Request(Uri.parse(videoUrl)).apply {
        setTitle(fileName)
        setDescription("Downloading video...")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.mp4")
        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
}

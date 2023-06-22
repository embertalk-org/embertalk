package edu.kit.tm.ps.embertalk.ui.qr_code

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.ui.components.SubmittableTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun QrCodeView(
    qrCodeViewModel: QrCodeViewModel,
    pubKey: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val keys = pubKey.chunked(750)
    val contents = keys.mapIndexed { index, key ->
        "ember://%s/%s".format(index, key)
    }
    val currentPage = rememberSaveable { mutableStateOf(0) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Card(
                Modifier.padding(5.dp)
            ) {
                Image(
                    bitmap = encodeAsBitmap(contents[currentPage.value], 2000, 2000).asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code)
                )
            }
            Row {
                IconButton(
                    enabled = currentPage.value > 0,
                    onClick = { currentPage.value = (currentPage.value - 1).coerceAtLeast(0) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
                Spacer(modifier = modifier.weight(1f))
                Text("%s of %s".format(currentPage.value, contents.size - 1))
                Spacer(modifier = modifier.weight(1f))
                IconButton(
                    enabled = currentPage.value < contents.size - 1,
                    onClick = { currentPage.value = (currentPage.value + 1).coerceAtMost(contents.size - 1) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
            if (qrCodeViewModel.isMyKey(pubKey)) {
                val keyServerScope = rememberCoroutineScope()
                SubmittableTextField(
                    label = { Text(stringResource(R.string.name_for_your_key)) },
                    imageVector = Icons.Filled.Upload,
                    onSubmit = {
                        keyServerScope.launch(Dispatchers.IO) {
                            val msg = when (val result = qrCodeViewModel.putKey(it)) {
                                201 -> "Uploaded Key successfully!"
                                else -> "Failed to upload key, status: $result"
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.weight(9f))
            FloatingActionButton(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.End),
                onClick = {
                    val type = "text/plain"
                    val subject = "EmberTalk Public Key"
                    val shareWith = "ShareWith"

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = type
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                    intent.putExtra(Intent.EXTRA_TEXT, "embertalk://$pubKey")

                    ContextCompat.startActivity(
                        context,
                        Intent.createChooser(intent, shareWith),
                        null
                    )
                }
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = stringResource(id = R.string.share_contact))
            }
        }
    }
}

fun encodeAsBitmap(content: String, width: Int, height: Int): Bitmap {
    val result: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, null)
    val w = result.width
    val h = result.height
    val pixels = IntArray(w * h)
    for (y in 0 until h) {
        val offset = y * w
        for (x in 0 until w) {
            pixels[offset + x] = if (result[x, y]) Color.BLACK else Color.WHITE
        }
    }
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, w, h)
    return bitmap
}
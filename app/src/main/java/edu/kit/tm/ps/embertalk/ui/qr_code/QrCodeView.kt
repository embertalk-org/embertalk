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
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import edu.kit.tm.ps.embertalk.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


@Composable
fun QrCodeView(
    qrCodeViewModel: QrCodeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val qrCodeUiState by qrCodeViewModel.uiState.collectAsState()

    val keys = runBlocking { qrCodeUiState.contact.pubKey.chunked(750) }
    val contents = listOf("ember://id/${qrCodeUiState.contact.userId}") + keys.mapIndexed { index, key ->
        "ember://%s/%s".format(index, key)
    }
    val currentPage = rememberSaveable { mutableIntStateOf(0) }
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
                    bitmap = encodeAsBitmap(contents[currentPage.intValue], 2000, 2000).asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code)
                )
            }
            Row {
                IconButton(
                    enabled = currentPage.intValue > 0,
                    onClick = { currentPage.intValue = (currentPage.intValue - 1).coerceAtLeast(0) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
                Spacer(modifier = modifier.weight(1f))
                Text("%s of %s".format(currentPage.intValue, contents.size - 1))
                Spacer(modifier = modifier.weight(1f))
                IconButton(
                    enabled = currentPage.intValue < contents.size - 1,
                    onClick = { currentPage.intValue = (currentPage.intValue + 1).coerceAtMost(contents.size - 1) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
            if (qrCodeViewModel.isMe()) {
                val contactId = qrCodeUiState.contact.userId
                val keyServerScope = rememberCoroutineScope()
                val focusManager = LocalFocusManager.current
                ElevatedButton(onClick = {
                    keyServerScope.launch(Dispatchers.IO) {
                        val msg = when (val result = qrCodeViewModel.putKey(contactId)) {
                            201 -> {
                                focusManager.clearFocus()
                                "Uploaded Key successfully!"
                            }
                            else -> "Failed to upload key, status: $result"
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Upload Key")
                }
            }
            Spacer(modifier = Modifier.weight(9f))
            FloatingActionButton(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.End),
                onClick = {
                    runBlocking {
                        val type = "text/plain"
                        val subject = "EmberTalk Public Key"
                        val shareWith = "ShareWith"

                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = type
                        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                        intent.putExtra(Intent.EXTRA_TEXT, "embertalk://${qrCodeUiState.contact.userId}")

                        context.startActivity(
                            Intent.createChooser(intent, shareWith),
                            null
                        )
                    }
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
    val bitmap = createBitmap(w, h)
    bitmap.setPixels(pixels, 0, width, 0, 0, w, h)
    return bitmap
}
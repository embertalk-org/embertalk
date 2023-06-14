package edu.kit.tm.ps.embertalk.ui.qr_code

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import edu.kit.tm.ps.embertalk.R

@Composable
fun QrCodeView(
    pubKey: String,
    modifier: Modifier = Modifier
) {
    val qrCodeContent = "embertalk://%s".format(pubKey)
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Card(
                Modifier.padding(25.dp)
            ) {
                Image(
                    bitmap = encodeAsBitmap(qrCodeContent, 1000, 1000).asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code)
                )
            }
            Card(
                Modifier.padding(25.dp).fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.public_key),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(10.dp)
                )
                Text(
                    text = pubKey,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

fun encodeAsBitmap(content: String, width: Int, height: Int): Bitmap {
    val result: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
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
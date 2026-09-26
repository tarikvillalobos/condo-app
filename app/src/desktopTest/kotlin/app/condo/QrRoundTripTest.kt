package app.condo

import app.condo.data.encodeQr
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import kotlin.test.*

class QrRoundTripTest {
    @Test fun generatedMatrixDecodesToExactPayload() {
        val payload = "condo-demo:v1:aurora:pickup:p1:token-123456"
        val matrix = encodeQr(payload)
        val scale = 6
        val side = (matrix.size + 8) * scale
        val pixels = IntArray(side * side) { index ->
            val x = index % side / scale - 4
            val y = index / side / scale - 4
            if (x in matrix.indices && y in matrix.indices && matrix[y][x]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        val source = RGBLuminanceSource(side, side, pixels)
        val decoded = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)))
        assertEquals(payload, decoded.text)
        assertEquals(BarcodeFormat.QR_CODE, decoded.barcodeFormat)
    }
}

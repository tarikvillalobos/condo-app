package app.condo.data

import qrcode.raw.QRCodeProcessor
import qrcode.raw.ErrorCorrectionLevel

/** ISO QR encoding with a mandatory four-module quiet zone added by the renderer. */
fun encodeQr(payload: String): List<List<Boolean>> =
    QRCodeProcessor(payload, errorCorrectionLevel = ErrorCorrectionLevel.MEDIUM)
        .encode().map { row -> row.map { it.dark } }

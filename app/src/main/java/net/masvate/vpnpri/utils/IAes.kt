package net.masvate.vpnpri.utils

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.regex.Pattern
import javax.crypto.Cipher

private const val KEY_RSA_ALGORITHM = "RSA"
private const val KEY_RSA_ALGORITHM_PADDING = "RSA/ECB/PKCS1Padding"
private const val KEY_MAX_ENCRYPT_BLOCK = 117


fun String.toRsaEncrypt(publicKey: String): String {
    val keyByteArray = publicKey.toByteArray().fromBase64()
    val keyFactory = KeyFactory.getInstance(KEY_RSA_ALGORITHM)
    val keySpec = X509EncodedKeySpec(keyByteArray)
    val pubKey = keyFactory.generatePublic(keySpec) as PublicKey
    val cipher = Cipher.getInstance(KEY_RSA_ALGORITHM_PADDING)
    cipher.init(Cipher.ENCRYPT_MODE, pubKey)

    val contentByteArray = toByteArray()
    val outputStream = ByteArrayOutputStream()
    var temp: ByteArray?
    var offset = 0
    while (contentByteArray.size - offset > 0) {
        if (contentByteArray.size - offset >= KEY_MAX_ENCRYPT_BLOCK) {
            temp = cipher.doFinal(contentByteArray, offset, KEY_MAX_ENCRYPT_BLOCK)
            offset += KEY_MAX_ENCRYPT_BLOCK
        } else {
            temp = cipher.doFinal(contentByteArray, offset, contentByteArray.size - offset)
            offset = contentByteArray.size
        }
        outputStream.write(temp)
    }
    outputStream.close()
    return outputStream.toByteArray().toBase64().decodeToString()
}

fun ByteArray.toBase64(flags: Int = Base64.NO_WRAP) =
    Base64.encode(this, flags)

fun ByteArray.fromBase64(flags: Int = Base64.NO_WRAP) = Base64.decode(this, flags)

fun String.isBase64(): Boolean {
    val pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    return Pattern.matches(pattern, this);
}


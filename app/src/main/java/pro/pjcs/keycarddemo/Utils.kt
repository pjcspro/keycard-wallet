package pro.pjcs.keycarddemo


fun toHex(bytes: ByteArray): String {

    var res = ""
    for (b in bytes) {
        val st = String.format("%02X", b)
        res += st
    }

    return res

}

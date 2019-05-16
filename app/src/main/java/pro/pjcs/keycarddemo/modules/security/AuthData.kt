package pro.pjcs.keycarddemo.modules.security

import im.status.keycard.applet.Pairing
import java.util.*

class AuthData {

    private var version                 = 1
            var cardID:         String  = ""
            var pairingObject:  Pairing? = null

    private val partsSeparator = "|"

    constructor(cardID: String = UUID.randomUUID().toString(), pairingObject: Pairing) {
        this.cardID         = cardID
        this.pairingObject  = pairingObject
    }

    constructor(serialized: String) {

        val (version, cardID, pairingObject) = serialized.split(partsSeparator)

        this.version        = version.toInt()
        this.cardID         = cardID
        this.pairingObject = Pairing(pairingObject) //load from base64

    }

    override fun toString(): String {
        return listOf(version, cardID, pairingObject?.toBase64()).joinToString(partsSeparator)
    }
}

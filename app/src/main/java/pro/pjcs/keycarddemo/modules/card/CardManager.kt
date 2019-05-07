package pro.pjcs.keycarddemo.modules.card

import android.app.Activity
import android.nfc.NfcAdapter
import im.status.keycard.android.NFCCardManager
import im.status.keycard.applet.KeycardCommandSet
import im.status.keycard.io.CardChannel
import im.status.keycard.io.CardListener
import pro.pjcs.keycarddemo.MyLog


/**
 * Listens for card connections and disconnections.
 * Creates a #CardSession once a card is connected
 */
class CardManager(private var activity : Activity) {

    private val TAG = "CardManager"
    private val nfcAdapter: NfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    private val cardManager: NFCCardManager = NFCCardManager()

    private var cardSession: CardSession? = null

    init {

        cardManager.setCardListener(object : CardListener {
            override fun onConnected(cardChannel: CardChannel) {
                onCardConnected(cardChannel)
            }

            override fun onDisconnected() {
                onCardDisconnected()
            }
        })
        cardManager.start()

    }


    /**
     * Starts listening for NFC connections
     */
    fun enable(){

        nfcAdapter.enableReaderMode(activity, this.cardManager, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)

    }

    /**
     * Stops listening for NFC connections
     */
    fun disable(){

        nfcAdapter.disableReaderMode(activity);
        onCardDisconnected()

    }

    private fun onCardConnected(cardChannel: CardChannel){

        MyLog.i(TAG, "onCardConnected")
        // Applet-specific code
        val cmdSet = KeycardCommandSet(cardChannel)

        cardSession = CardSession(cmdSet)

    }

    private fun onCardDisconnected(){

        MyLog.i(TAG, "onCardDisconnected")

        //clear from memory
        cardSession = null
    }

}
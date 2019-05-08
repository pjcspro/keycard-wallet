package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_card_listener.*
import pro.pjcs.keycarddemo.MyLog
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.card.CardManager
import pro.pjcs.keycarddemo.modules.card.CardSession
import pro.pjcs.keycarddemo.modules.card.ICardListener
import pro.pjcs.keycarddemo.toHex

class CardListenerActivity : Activity(), ICardListener {

    companion object {

        const val BUNDLE_ACTION = "BUNDLE_ACTION"
        const val BUNDLE_DATA  = "BUNDLE_DATA"
        private val TAG = "CardListenerActivity"
    }

    private lateinit var cardManager: CardManager

    enum class ACTION { SEND, SIGN }
    private var action : ACTION? = null
    private var action_data : ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card_listener);

        transact_button.setOnClickListener {
            startActivity(Intent(this, TransactActivity::class.java))
        }

        cardManager = CardManager(this)
        cardManager.cardListener = this //TODO: replace with LiveData

        action = intent.getSerializableExtra(BUNDLE_ACTION) as? ACTION
        if( action == null ){
            throw Exception("CardListenerActivity requires an action to be passed on its Intent")
        }

        action_data = intent.getByteArrayExtra(BUNDLE_DATA) as? ByteArray
        if( action_data == null ){
            throw Exception("CardListenerActivity requires an action_data to be passed on its Intent")
        }

    }

    override fun onResume() {
        super.onResume()

        //start listening for NFC connections
        cardManager.enable()
    }

    override fun onPause() {
        super.onPause()

        //stop listening for NFC connections
        cardManager.disable()
    }


    override fun cardDidConnect(cardManager: CardManager, cardSession: CardSession) {
        when(action){
            ACTION.SIGN -> action_data?.let { sign(cardSession, it) }
        }
    }

    override fun cardDidDisconnect(cardManager: CardManager) {

    }

    private fun sign(cardSession: CardSession, hash : ByteArray){

        cardSession.just {
            val signature = cardSession.sign(hash)

            MyLog.i(TAG, "Signed hash: " + toHex(hash))
            MyLog.i(TAG, "Recovery ID: " + signature.recId)
            MyLog.i(TAG, "R: " + toHex(signature.r))
            MyLog.i(TAG, "S: " + toHex(signature.s))
        }

    }

}
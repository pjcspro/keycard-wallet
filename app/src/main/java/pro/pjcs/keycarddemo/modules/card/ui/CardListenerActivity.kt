package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_card_listener.*
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.blockchain.RequestForTransaction
import pro.pjcs.keycarddemo.modules.card.CardManager
import pro.pjcs.keycarddemo.modules.card.CardSession
import pro.pjcs.keycarddemo.modules.card.ICardListener




class CardListenerActivity : Activity(), ICardListener {


    private lateinit var cardManager: CardManager

    enum class ACTION { SEND, SIGN }
    private var action : ACTION? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card_listener);

        cardManager = CardManager(this)
        cardManager.cardListener = this //TODO: replace with LiveData

        action = intent.getSerializableExtra(BUNDLE_ACTION) as? ACTION
        if( action == null ){
            throw Exception("CardListenerActivity requires an action to be passed on its Intent")
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

        helper_text.text = getString(R.string.card_connected_1)

        when(action){
            ACTION.SIGN -> (intent.getSerializableExtra(BUNDLE_DATA) as RequestForTransaction)?.let { it.testSign(cardSession, this) }
        }
    }

    override fun cardDidDisconnect(cardManager: CardManager) {

        helper_text.text = getString(R.string.waiting_for_card)

    }

    companion object {

        const val BUNDLE_ACTION = "BUNDLE_ACTION"
        const val BUNDLE_DATA  = "BUNDLE_DATA"
        private val TAG = "CardListenerActivity"
    }

}
package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.os.Bundle
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.card.CardManager

class CardListenerActivity : Activity() {

    private lateinit var cardManager: CardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card_listener);

        cardManager = CardManager(this)

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

}
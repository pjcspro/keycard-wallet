package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.activity_card_listener.*
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.blockchain.IRequestTransaction
import pro.pjcs.keycarddemo.modules.blockchain.RequestForTransaction
import pro.pjcs.keycarddemo.modules.card.CardManager
import pro.pjcs.keycarddemo.modules.card.CardSession
import pro.pjcs.keycarddemo.modules.card.ICardListener


class CardListenerActivityI : Activity(), ICardListener, IRequestTransaction {

    private lateinit var cardManager: CardManager

    enum class ACTION { SEND }
    private var action : ACTION? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card_listener);

        cardManager = CardManager(this)
        cardManager.cardListener = this //TODO: replace with LiveData and/or events

        action = intent.getSerializableExtra(BUNDLE_ACTION) as? ACTION
        if( action == null ){
            throw Exception("CardListenerActivityI requires an action to be passed on its Intent")
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

    private fun setHelperText( @StringRes text: Int){
        helper_text.text = getString(text)
    }

    //// LISTENERS

    override fun cardDidConnect(cardManager: CardManager, cardSession: CardSession) {

        setHelperText( R.string.card_connected_1 )

        when(action){
            ACTION.SEND -> (intent.getSerializableExtra(BUNDLE_DATA) as RequestForTransaction)?.let { it.send(cardSession, this) }
        }
    }

    override fun cardDidDisconnect(cardManager: CardManager) {

        helper_text.text = getString(R.string.waiting_for_card)

    }

    companion object {

        const val BUNDLE_ACTION = "BUNDLE_ACTION"
        const val BUNDLE_DATA  = "BUNDLE_DATA"
        private val TAG = "CardListenerActivityI"
    }

    override fun didSubmitTransaction(txHash: String) {
        didSend()

        runOnUiThread {
            finish()
            val intent = Intent(this, TransactionSuccessActivity::class.java)
            intent.putExtra(TransactionSuccessActivity.BUNDLE_TRANSACTION_HASH, txHash)
            startActivity(intent)
        }

    }

    override fun didFailedTransaction(error: String) {
        didFailed(error)
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun didFailWithException(error: Exception) {
        error.printStackTrace()
    }


    override fun willPrepareTransaction() {
        willSend()
    }

    override fun willPair() {
        setHelperText(R.string.card_status_pairing)
    }

    override fun didPair() {
        setHelperText(R.string.card_status_pairing)
    }

    override fun willAuthenticate() {
        setHelperText(R.string.card_status_authenticating)
    }

    override fun didAuthenticate() {
        setHelperText(R.string.card_status_authenticated)
    }

    fun willSend(){
        setHelperText(R.string.card_status_transacting)
    }

    fun didSend(){
        setHelperText(R.string.card_status_transacted)
    }

    fun didFailed(error: String){
        helper_text.text = "ERROR: $error"
    }

}
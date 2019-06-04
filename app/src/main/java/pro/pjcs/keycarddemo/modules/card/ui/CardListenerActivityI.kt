package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import im.status.keycard.io.WrongPINException
import kotlinx.android.synthetic.main.activity_card_listener.*
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.blockchain.IRequestTransaction
import pro.pjcs.keycarddemo.modules.blockchain.RequestForTransaction
import pro.pjcs.keycarddemo.modules.card.CardManager
import pro.pjcs.keycarddemo.modules.card.CardSession
import pro.pjcs.keycarddemo.modules.card.ICardListener


class CardListenerActivityI : Activity(), ICardListener, IRequestTransaction, PinLockListener {

    private lateinit var cardManager: CardManager

    enum class ACTION { SEND }
    private var action : ACTION? = null

    private var readyToAct = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card_listener);

        cardManager = CardManager(this)
        cardManager.cardListener = this //TODO: replace with LiveData and/or events

        action = intent.getSerializableExtra(BUNDLE_ACTION) as? ACTION
        if( action == null ){
            throw Exception("CardListenerActivityI requires an action to be passed on its Intent")
        }

        input_pin.visibility = View.VISIBLE
        input_pin.setPinLockListener(this)
        input_pin.pinLength = 6

        input_pin.attachIndicatorDots(pin_dots)
        pin_dots.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION

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

    private fun setHelperText(text: Int){
        helper_text.text = getString(text)
    }

    //// LISTENERS

    override fun cardDidConnect(cardManager: CardManager, cardSession: CardSession) {

        if(!readyToAct){
            return
        }

        setHelperText( R.string.card_connected_1 )

        when(action){
            ACTION.SEND -> (intent.getSerializableExtra(BUNDLE_DATA) as? RequestForTransaction)?.let { it.send(cardSession, this) }
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

        runOnUiThread {
            didFailed(error)
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun didFailWithException(error: Exception) {
        error.printStackTrace()

        if( error is WrongPINException ){
            cardManager.setAuthenticationPin(null)
            setHelperText(R.string.card_status_error_pin)
        }
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

    ///PIN
    override fun onComplete(pin: String?) {

        readyToAct = true
        Handler().postDelayed({

            pin_container.visibility = View.GONE
            listening_container.visibility = View.VISIBLE
            pin?.let { cardManager.setAuthenticationPin(it) }

        }, 600)

    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) { }
    override fun onEmpty() { }


}

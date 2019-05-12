package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_transact.*
import org.web3j.utils.Convert
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.blockchain.RequestForTransaction
import java.math.BigDecimal


class TransactActivity : Activity() {

    private val TAG = "TransactActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transact)

        action_send.setOnClickListener{
            send(input_to_address.text.toString(), input_amount.text.toString())
        }

    }

    private fun send(to_address: String, amount : String){

        val finneyValue = amount.toDoubleOrNull()
        if(finneyValue == null){
            Toast.makeText(applicationContext, "Invalid amount", Toast.LENGTH_LONG).show()
            return
        }

        val weiValue = Convert.toWei(BigDecimal.valueOf(finneyValue), Convert.Unit.ETHER).toBigIntegerExact()
        val request = RequestForTransaction(to_address, weiValue)

        val intent = Intent(this, CardListenerActivityI::class.java)
        intent.putExtra(CardListenerActivityI.BUNDLE_ACTION, CardListenerActivityI.ACTION.SEND)
        intent.putExtra(CardListenerActivityI.BUNDLE_DATA, request)
        startActivity(intent)

    }
}
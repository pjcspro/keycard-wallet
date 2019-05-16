package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_transact.*
import org.web3j.utils.Convert
import pro.pjcs.keycarddemo.R
import pro.pjcs.keycarddemo.modules.blockchain.RequestForTransaction


class TransactActivity : Activity() {

    private val TAG = "TransactActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transact)

        action_send.setOnClickListener{
            send(input_to_address.text.toString(), input_amount.text.toString(), input_gas_limit.text.toString(), input_gas_price.text.toString())
        }


    }

    private fun send(to_address: String, amount: String, gasLimitStr: String, gasPriceStr: String){

        val ethValue = amount.toBigDecimalOrNull()
        if(ethValue == null){
            Toast.makeText(applicationContext, "Invalid amount", Toast.LENGTH_LONG).show()
            return
        }

        val gasPrice = gasPriceStr.toBigDecimalOrNull()
        if(gasPrice == null){
            Toast.makeText(applicationContext, "Invalid gas price", Toast.LENGTH_LONG).show()
            return
        }

        val weiValue = Convert.toWei(ethValue, Convert.Unit.ETHER).toBigIntegerExact()
        val weiGasPrice = Convert.toWei(gasPrice, Convert.Unit.GWEI).toBigIntegerExact()
        val request = RequestForTransaction(to_address, weiValue, weiGasPrice, gasLimitStr.toBigIntegerOrNull())

        val intent = Intent(this, CardListenerActivityI::class.java)
        intent.putExtra(CardListenerActivityI.BUNDLE_ACTION, CardListenerActivityI.ACTION.SEND)
        intent.putExtra(CardListenerActivityI.BUNDLE_DATA, request)
        startActivity(intent)

    }
}
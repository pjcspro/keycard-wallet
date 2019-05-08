package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_transact.*
import pro.pjcs.keycarddemo.R

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

        val hash = "testing $to_address $amount"

        val intent = Intent(this, CardListenerActivity::class.java)
        intent.putExtra(CardListenerActivity.BUNDLE_ACTION, CardListenerActivity.ACTION.SIGN)
        intent.putExtra(CardListenerActivity.BUNDLE_DATA, "thiscouldbeahashintheorysoitisok".toByteArray()) //TODO: get hash from transaction
        startActivity(intent)

    }
}
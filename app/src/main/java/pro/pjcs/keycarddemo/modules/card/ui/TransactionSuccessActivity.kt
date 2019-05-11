package pro.pjcs.keycarddemo.modules.card.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_transaction_success.*
import pro.pjcs.keycarddemo.R

class TransactionSuccessActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_transaction_success)

        view_transaction.setOnClickListener {

            intent.getStringExtra(BUNDLE_TRANSACTION_HASH)?.let {
                val transactionHash = it

                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse("https://ropsten.etherscan.io/tx/$transactionHash")
                startActivity(openURL) }


        }

    }


    companion object {
        val BUNDLE_TRANSACTION_HASH = "BUNDLE_TRANSACTION_HASH"
    }
}
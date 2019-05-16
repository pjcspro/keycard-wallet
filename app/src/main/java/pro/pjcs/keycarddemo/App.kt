package pro.pjcs.keycarddemo

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.http.HttpService

class App : Application() {

    companion object {
        lateinit var web3j: JsonRpc2_0Web3j
        lateinit var preferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()

        var infuraKey = getString(R.string.infura_key)
        web3j = JsonRpc2_0Web3j(HttpService("https://ropsten.infura.io/v3/$infuraKey"))

        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

}
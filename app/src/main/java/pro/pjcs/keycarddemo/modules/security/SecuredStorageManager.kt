package pro.pjcs.keycarddemo.modules.security

import android.text.TextUtils
import pro.pjcs.keycarddemo.App


class SecuredStorageManager(private val encryptionManager: EncryptionManager) {

    private val AUTH_DATA = "AUTH_DATA"
    private val PAIRING_KEY = "PAIRING_KEY"


    val authData: AuthData?
        get() {
            App.preferences.getString(AUTH_DATA, null)?.let { string ->
                return AuthData(encryptionManager.decrypt(string))
            }
            return null
        }

    fun saveAuthData(authData: AuthData) {
        App.preferences.edit().putString(AUTH_DATA, encryptionManager.encrypt(authData.toString())).apply()
    }

    fun noAuthData(): Boolean {
        val words = App.preferences.getString(AUTH_DATA, null)
        return words.isNullOrEmpty()
    }

    val savedPairingKey: String?
        get() {
            val string = App.preferences.getString(PAIRING_KEY, null)
            return if (TextUtils.isEmpty(string)) {
                null
            } else {
                encryptionManager.decrypt(string)
            }
        }

    fun savePairingKey(pin: String) {
        App.preferences.edit().putString(PAIRING_KEY, encryptionManager.encrypt(pin)).apply()
    }

    fun pairingKeyIsEmpty(): Boolean {
        val string = App.preferences.getString(PAIRING_KEY, null)
        return string.isNullOrEmpty()
    }

}

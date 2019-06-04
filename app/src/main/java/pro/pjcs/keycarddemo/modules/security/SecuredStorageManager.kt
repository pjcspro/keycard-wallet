package pro.pjcs.keycarddemo.modules.security

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

    fun clearAuthData(){
        App.preferences.edit().remove(AUTH_DATA)
    }

    fun noAuthData(): Boolean {
        val words = App.preferences.getString(AUTH_DATA, null)
        return words.isNullOrEmpty()
    }

}

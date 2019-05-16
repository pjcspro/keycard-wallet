package pro.pjcs.keycarddemo.modules.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import pro.pjcs.keycarddemo.R
import javax.crypto.Cipher

class EncryptionManager  {

    private val keyStoreWrapper = KeyStoreWrapper()

    @Synchronized
    fun encrypt(data: String): String {

        var masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)

        if (masterKey == null) {
            masterKey = keyStoreWrapper.createAndroidKeyStoreSymmetricKey(MASTER_KEY)
        }
        return CipherWrapper().encrypt(data, masterKey)
    }

    @Synchronized
    fun decrypt(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
                ?: throw KeyPermanentlyInvalidatedException()
        return CipherWrapper().decrypt(data, masterKey)
    }


    fun getCryptoObject(): FingerprintManagerCompat.CryptoObject {
        var masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)

        if (masterKey == null) {
            masterKey = keyStoreWrapper.createAndroidKeyStoreSymmetricKey(MASTER_KEY)
        }

        val cipher = CipherWrapper().cipher
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)

        return FingerprintManagerCompat.CryptoObject(cipher)
    }

    companion object {
        const val MASTER_KEY = "MASTER_KEY"

        /**
         * IMPORTANT NOTE: Should handle this on activity result, otherwise clicking on the back button
         * closese this screen and goes back to the normal activity
         */
        fun showAuthenticationScreen(activity: Activity, requestCode: Int) {
            val mKeyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val intent: Intent? = mKeyguardManager.createConfirmDeviceCredentialIntent(
                    activity.getString(R.string.OSPin_Confirm_Title),
                    activity.getString(R.string.OSPin_Confirm_Desciption)
            )
            activity.startActivityForResult(intent, requestCode)
        }

        fun showAuthenticationScreen(fragment: Fragment, requestCode: Int) {
            val mKeyguardManager = fragment.activity?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val intent: Intent? = mKeyguardManager.createConfirmDeviceCredentialIntent(
                    fragment.getString(R.string.OSPin_Confirm_Title),
                    fragment.getString(R.string.OSPin_Confirm_Desciption)
            )
            fragment.startActivityForResult(intent, requestCode)
        }

        fun isDeviceLockEnabled(ctx: Context): Boolean {
            val keyguardManager = ctx.getSystemService(Activity.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardSecure
        }

    }

}

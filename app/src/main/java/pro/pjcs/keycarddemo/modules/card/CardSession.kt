package pro.pjcs.keycarddemo.modules.card

import android.util.Log
import im.status.keycard.applet.ApplicationInfo
import im.status.keycard.applet.BIP32KeyPair
import im.status.keycard.applet.KeycardCommandSet
import pro.pjcs.keycarddemo.toHex





class CardSession(private var cmdSet : KeycardCommandSet) {

    private val TAG = "CardSession"
    private var info: ApplicationInfo

    init {

        // Selecting the applet on the card
        info = ApplicationInfo(cmdSet.select().checkOK().data)

        initializeIfNeed()

        debug()

        pair()

        authenticateWithPin()

        unpair()

        debugPublicKey()

    }

    fun isInitialized(): Boolean { return info.isInitializedCard }
    fun hasSecureCapability(): Boolean { return info.hasSecureChannelCapability() }
    fun publicKey(): BIP32KeyPair? {
        return BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().data)
    }

    private fun pair(){

        if (info.hasSecureChannelCapability()) {
            // In real projects, the pairing key should be saved and used for all new sessions.
            cmdSet.autoPair("KeycardTest")
            val pairing = cmdSet.pairing

            // Never log the pairing key in a real application!
            Log.i(TAG, "Pairing with card is done.")
            Log.i(TAG, "Pairing index: " + pairing.pairingIndex)
            Log.i(TAG, "Pairing key: " + toHex(pairing.pairingKey))

            // Opening a Secure Channel is needed for all other applet commands
            cmdSet.autoOpenSecureChannel()

            Log.i(TAG, "Secure channel opened. Getting applet status.")
        }

    }

    private fun unpair(){
        Log.i(TAG, "unpair");

        if (hasSecureCapability()) {
            // Cleanup, in a real application you would not unpair and instead keep the pairing key for successive interactions.
            // We also remove all other pairings so that we do not fill all slots with failing runs. Again in real application
            // this would be a very bad idea to do.
            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

        }

    }

    private fun debug(){

        Log.i(TAG, "Instance UID: " + toHex(info.getInstanceUID()));
        Log.i(TAG, "Secure channel public key: " + toHex(info.getSecureChannelPubKey()));
        Log.i(TAG, "Application version: " + info.getAppVersionString());
        Log.i(TAG, "Free pairing slots: " + info.getFreePairingSlots());
        if (info.hasMasterKey()) {
            Log.i(TAG, "Key UID: " + toHex(info.getKeyUID()));
        } else {
            Log.i(TAG, "The card has no master key");
        }
        Log.i(TAG,  String.format("Capabilities: %02X", info.getCapabilities()));
        Log.i(TAG, "Has Secure Channel: " + info.hasSecureChannelCapability());
        Log.i(TAG, "Has Key Management: " + info.hasKeyManagementCapability());
        Log.i(TAG, "Has Credentials Management: " + info.hasCredentialsManagementCapability());
        Log.i(TAG, "Has NDEF capability: " + info.hasNDEFCapability());

    }

    private fun debugPublicKey(){

        //Log.i(TAG, "Wallet public key: " + toHex(publicKey.getPublicKey()));
        //Log.i(TAG, "Wallet address: " + toHex(publicKey.toEthereumAddress()));

    }

    private fun initializeIfNeed(){

        // If the card is not initialized, the INIT apdu must be sent. The actual PIN, PUK and pairing password values
        // can be either generated or chosen by the user. Using fixed values is highly discouraged.
        if (!isInitialized()) {
            Log.i(TAG, "Initializing card with test secrets");
            cmdSet.init("000000", "123456789012", "KeycardTest").checkOK();
            info = ApplicationInfo(cmdSet.select().checkOK().data);
        }

    }

    private fun authenticateWithPin(){

        if (info.hasCredentialsManagementCapability()) {
            // PIN authentication allows execution of privileged commands
            cmdSet.verifyPIN("000000").checkAuthOK();

            Log.i(TAG, "Pin Verified.");
        }

    }


}
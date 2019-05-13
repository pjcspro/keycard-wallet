package pro.pjcs.keycarddemo.modules.card
import im.status.keycard.applet.*
import pro.pjcs.keycarddemo.MyLog
import pro.pjcs.keycarddemo.toHex
import kotlin.concurrent.thread


/**
 * Handles the high level interaction with the card
 *
 * TODO: initializaton process if card is not init/
 * TODO: Ask credentials to the user, not fixed
 * TODO: Make clear which calls require authentication
 * TODO: Allow to switch between 12 and 24 words
 * TODO: More complete key derivation
 *
 */

interface ICardSessionListener {
    fun willPair()
    fun didPair()
    fun willAuthenticate()
    fun didAuthenticate()
    fun didFailWithException(error: Exception)
}

class CardSession(val cmdSet : KeycardCommandSet, val listener : ICardSessionListener?) {

    private val TAG = "CardSession"
    private var info: ApplicationInfo

    init {

        // Selecting the applet on the card
        info = ApplicationInfo(cmdSet.select().checkOK().data)


        if( !isCardInitialized() ){

            MyLog.w(TAG, "Card is not initialized")
            //TODO: start initialization process

            /*
            initializeIfNeed()

            debug()

            pair()

            debugRetryCount()

            authenticateWithPin()

            if( !hasMasterKey() ){

                val mnemonic = generateMnemonic()
                MyLog.i(TAG, "Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase())

                injectMnemonicKey(mnemonic)
            }

            debugPublicKey()

            MyLog.i(TAG, "Current key path: "+getCurrentKeyPath()) //Current key path: m => Master Key

            deriveKey()

            MyLog.i(TAG, "Current key path: "+getCurrentKeyPath())

            unpair()
            */

        }else{

            MyLog.i(TAG, "Card is initialized")
            debug()

        }

    }


    /******************* INITIALIZATION ******************/

    fun isCardInitialized(): Boolean { return info.isInitializedCard }
    fun hasSecureCapability(): Boolean { return info.hasSecureChannelCapability() }


    private fun initializeIfNeed(){

        // If the card is not initialized, the INIT apdu must be sent. The actual PIN, PUK and pairing password values
        // can be either generated or chosen by the user. Using fixed values is highly discouraged.
        if (!isCardInitialized()) {
            MyLog.i(TAG, "Initializing card with test secrets");
            cmdSet.init("000000", "123456789012", "KeycardTest").checkOK();
            info = ApplicationInfo(cmdSet.select().checkOK().data);
        }

    }

    /******************* PAIRING & SECURE CHANNEL ******************/

    private fun authenticateWithPin(){

        if (info.hasCredentialsManagementCapability()) {

            listener?.willAuthenticate()

            // PIN authentication allows execution of privileged commands
            cmdSet.verifyPIN("000000").checkAuthOK();

            MyLog.i(TAG, "Pin Verified.");

            listener?.didAuthenticate()
        }

    }

    private fun pair(){

        if (info.hasSecureChannelCapability()) {

            listener?.willPair()

            // In real projects, the pairing key should be saved and used for all new sessions.
            cmdSet.autoPair("KeycardTest") //TODO: Request password to the user
            val pairing = cmdSet.pairing

            // Never log the pairing key in a real application!
            MyLog.i(TAG, "Pairing with card is done.")
            MyLog.i(TAG, "Pairing index: " + pairing.pairingIndex)
            MyLog.i(TAG, "Pairing key: " + toHex(pairing.pairingKey))           //TODO: Save pairing key locally

            // Opening a Secure Channel is needed for all other applet commands
            cmdSet.autoOpenSecureChannel()

            MyLog.i(TAG, "Secure channel opened")

            listener?.didPair()
        }

    }

    private fun unpair(){

        if (hasSecureCapability()) {
            // Cleanup, in a real application you would not unpair and instead keep the pairing key for successive interactions.
            // We also remove all other pairings so that we do not fill all slots with failing runs. Again in real application
            // this would be a very bad idea to do.
            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

        }

        MyLog.i(TAG, "unpaired");

    }




    /******************* DEBUG ******************/

    private fun debug(){

        MyLog.i(TAG, "Instance UID: " + toHex(info.getInstanceUID()));
        MyLog.i(TAG, "Secure channel public key: " + toHex(info.getSecureChannelPubKey()));
        MyLog.i(TAG, "Application version: " + info.getAppVersionString());
        MyLog.w(TAG, "Free pairing slots: " + info.getFreePairingSlots());
        if (info.hasMasterKey()) {
            MyLog.i(TAG, "Key UID: " + toHex(info.getKeyUID()));
        } else {
            MyLog.i(TAG, "The card has no master key");
        }
        MyLog.i(TAG,  String.format("Capabilities: %02X", info.getCapabilities()));
        MyLog.i(TAG, "Has Secure Channel: " + info.hasSecureChannelCapability());
        MyLog.i(TAG, "Has Key Management: " + info.hasKeyManagementCapability());
        MyLog.i(TAG, "Has Credentials Management: " + info.hasCredentialsManagementCapability());
        MyLog.i(TAG, "Has NDEF capability: " + info.hasNDEFCapability());

    }

    private fun debugRetryCount(){

        val status = ApplicationStatus(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_APPLICATION).checkOK().data)

        MyLog.i(TAG, "PIN retry counter: " + status.pinRetryCount)
        MyLog.i(TAG, "PUK retry counter: " + status.pukRetryCount)
        MyLog.i(TAG, "Has master key: " + status.hasMasterKey())

    }

    private fun debugPublicKey(){

        if( hasMasterKey() ){

            getPublicKey()?.let {

                MyLog.i(TAG, "Wallet public key: " + toHex(it.publicKey));
                MyLog.i(TAG, "Wallet address: " + toHex(it.toEthereumAddress()));

            }

        }

    }


    /******************* PRIVATE KEYS ******************/

    fun getPublicKey(): BIP32KeyPair? {
        return BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().data)
    }

    fun hasMasterKey(): Boolean {
        val status = ApplicationStatus(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_APPLICATION).checkOK().data)
        return status.hasMasterKey();
    }

    fun getCurrentKeyPath(): KeyPath {
        return KeyPath(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().data)
    }

    private fun generateMnemonic(): Mnemonic {

        if (info.hasKeyManagementCapability()) {

            // A mnemonic can be generated before PIN authentication. Generating a mnemonic does not create keys on the card. a subsequent loadKey step must be performed after PIN authentication.
            val mnemonic = Mnemonic(cmdSet.generateMnemonic(KeycardCommandSet.GENERATE_MNEMONIC_12_WORDS).checkOK().data)
            mnemonic.fetchBIP39EnglishWordlist()

            return mnemonic;

        }else{
            throw Exception("Card does not allow to generate Mnemonic")
        }

    }

    private fun generateInternalKey(){

        if (!hasMasterKey() && info.hasKeyManagementCapability())
        {
            cmdSet.generateKey()
        }

    }

    private fun injectMnemonicKey(mnemonic : Mnemonic){
        if (!hasMasterKey() && info.hasKeyManagementCapability())
        {
            cmdSet.loadKey(mnemonic.toBIP32KeyPair());
        }

    }


    private fun deriveKey(){ //TODO: parameterize this

        if (getCurrentKeyPath().toString() != "m/44'/0'/0'/0/0")
        {
            // Key derivation is needed to select the desired key. The derived key remains current until a new derive
            // command is sent (it is not lost on power loss).
            cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK()
            MyLog.w(TAG, "Derived m/44'/0'/0'/0/0")
        }

    }


    /******************* SIGNING KEY ******************/

    fun readyToSign(): Boolean {
        return isCardInitialized() && hasMasterKey() && !getCurrentKeyPath().equals("m");
    }



    fun sign(hash : ByteArray): ByteArray {

        if( !readyToSign() ){
            throw Exception("Card not ready to sign yet. Please initialize it, create a master key and derive it.")
        }

        //val signature = RecoverableSignature(hash, cmdSet.sign(hash).checkOK().data)
        val signature = cmdSet.sign(hash).checkOK().data ?: throw Exception("Error while signing")
        return signature

    }

    fun deriveAndSign(path : String, hash : ByteArray): RecoverableSignature {

        //path = "m/44'/0'/0'/0/0"
        val resp = cmdSet.signWithPath(hash, path, false)
        val signature = RecoverableSignature(hash, resp.checkOK().data)
        return signature

    }

    fun just( action : ()->Unit ){

        thread {

            try {
                //TODO: create a datatype that know the requirements for an action (pair needed? authentication needed? etc)
                pair()
                authenticateWithPin()
                action()
            }catch (e : Exception){
                e.printStackTrace()
                listener?.didFailWithException(e)
            }finally {
                try { unpair() } catch (e : Exception){ e.printStackTrace() }
            }

        }

    }
}
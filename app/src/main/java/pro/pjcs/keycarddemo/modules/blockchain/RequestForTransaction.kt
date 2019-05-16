package pro.pjcs.keycarddemo.modules.blockchain

import im.status.keycard.applet.RecoverableSignature
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.Hash
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.Sign
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tx.Transfer
import org.web3j.utils.Numeric
import pro.pjcs.keycarddemo.App
import pro.pjcs.keycarddemo.MyLog
import pro.pjcs.keycarddemo.modules.card.CardSession
import pro.pjcs.keycarddemo.toHex
import java.io.Serializable
import java.math.BigInteger
import java.util.*


/**
 * TODO: Replace interface listeners with LiveData and/or events
 */

interface IRequestTransaction {
    fun willPrepareTransaction()
    fun didSubmitTransaction(txHash : String)
    fun didFailedTransaction(error : String)
}

/**
 * Note: for ethereum pairingKey must be in weiValue
 */
class RequestForTransaction(private val toAddress: String, private val value : BigInteger, private var gasPrice: BigInteger? = null, private var gasLimit: BigInteger? = null) : Serializable {



    fun send(cardSession: CardSession, listenerI: IRequestTransaction){


        cardSession.just {

            listenerI.willPrepareTransaction()
            val web3j = App.web3j
            val fromAddress = cardSession.getPublicKey()?.toEthereumAddress()?.let { "0x"+ toHex(it) };
            MyLog.w(TAG, "fromAddress: $fromAddress")

            // Create transaction
            gasPrice = gasPrice ?: web3j.ethGasPrice().send().gasPrice
            gasLimit = gasLimit ?: Transfer.GAS_LIMIT

            val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST).send().transactionCount

            val rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, value)

            val txBytes = TransactionEncoder.encode(rawTransaction)
            val signature = signMessage(cardSession, txBytes)


            val encode = TransactionEncoder::class.java.getDeclaredMethod(
                "encode",
                RawTransaction::class.java,
                Sign.SignatureData::class.java
            )
            encode.isAccessible = true

            // Send transaction
            val signedMessage = encode.invoke(null, rawTransaction, signature) as ByteArray
            val hexValue = "0x" + Hex.toHexString(signedMessage)
            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

            if (ethSendTransaction.hasError()) {

                println("Transaction Error: " + ethSendTransaction.error.message)
                listenerI.didFailedTransaction(ethSendTransaction.error.message)

            }

            MyLog.w(TAG, "Transaction hash: "+ethSendTransaction.transactionHash)
            listenerI.didSubmitTransaction(ethSendTransaction.transactionHash)

        }

    }


    @Throws(Exception::class)
    private fun signMessage(cardSession: CardSession, message: ByteArray): Sign.SignatureData {
        val messageHash = Hash.sha3(message)

        val respData = cardSession.sign(messageHash)
        val rawSig = extractSignature(respData)

        val rLen = rawSig[3].toInt()
        val sOff = 6 + rLen
        val sLen = rawSig.size - rLen - 6

        var r = BigInteger(Arrays.copyOfRange(rawSig, 4, 4 + rLen))
        var s = BigInteger(Arrays.copyOfRange(rawSig, sOff, sOff + sLen))


        val signature = RecoverableSignature(messageHash, respData)


        /* OLD VERSION
        val ecdsaSignature = Class.forName("org.web3j.crypto.Sign\$ECDSASignature")
        val ecdsaSignatureConstructor =
            ecdsaSignature.getDeclaredConstructor(BigInteger::class.java, BigInteger::class.java)
        ecdsaSignatureConstructor.isAccessible = true
        var sig = ecdsaSignatureConstructor.newInstance(r, s)
        val m = ecdsaSignature.getMethod("toCanonicalised")
        m.isAccessible = true
        sig = m.invoke(sig)

        val recoverFromSignature = Sign::class.java.getDeclaredMethod(
            "recoverFromSignature",
            Int::class.javaPrimitiveType,
            ecdsaSignature,
            ByteArray::class.java
        )
        recoverFromSignature.isAccessible = true

        val pubData = extractPublicKeyFromSignature(respData)
        val publicKey = BigInteger(Arrays.copyOfRange(pubData, 1, pubData.size))

        var recId = -1
        for (i in 0..3) {
            val k = recoverFromSignature.invoke(null, i, sig, messageHash) as BigInteger
            if (k != null && k!!.equals(publicKey)) {
                recId = i
                break
            }
        }
        if (recId == -1) {
            throw RuntimeException("Could not construct a recoverable key. This should never happen.")
        }

        val rF = signature.r
        val sF = signature.s

        */

        val headerByte = signature.recId + 27

        // 1 header + 32 bytes for R + 32 bytes for S
        val v = headerByte.toByte()
        val rB = Numeric.toBytesPadded(r, 32)
        val sB = Numeric.toBytesPadded(s, 32)

        return Sign.SignatureData(v, rB, sB)
    }

    private fun extractSignature(sig: ByteArray): ByteArray {
        val off = sig[4] + 5
        return Arrays.copyOfRange(sig, off, off + sig[off + 1].toInt() + 2)
    }

    private fun extractPublicKeyFromSignature(sig: ByteArray): ByteArray {
        return Arrays.copyOfRange(sig, 5, 5 + sig[4])
    }

    companion object {
        val TAG = "RequestForTransaction"
    }


}


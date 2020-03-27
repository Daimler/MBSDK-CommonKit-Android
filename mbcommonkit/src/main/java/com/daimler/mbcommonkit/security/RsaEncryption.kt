package com.daimler.mbcommonkit.security

import android.content.Context
import android.security.KeyPairGeneratorSpec
import com.daimler.mbcommonkit.security.memory.MemoryCacheEncryptionAlgorithm
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

internal class RsaEncryption(
    private val context: Context,
    private val keyStore: KeyStore,
    cacheEncryptedValues: Boolean
) : MemoryCacheEncryptionAlgorithm(cacheEncryptedValues) {

    private val settings = RsaSettings(context)

    companion object {
        private const val AES_MODE = "AES/ECB/PKCS7Padding"

        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"

        private const val PROVIDER_ANDROID_SSL = "AndroidOpenSSL"

        private const val PROVIDER_BC = "BC"

        private const val KEY_LENGTH_IN_BYTES = 256

        private const val PREFIX_SUBJECT_PRINCIPAL = "CN="

        private const val UTF_8 = "UTF-8"

        /**
         * Same constant as {@link KeyProperties#KEY_ALGORITHM_RSA}. This is required for devices running
         * OS version < Android M
         */
        private const val KEY_ALGORITHM_RSA = "RSA"

        /**
         * Same constant as {@link KeyProperties#KEY_ALGORITHM_AES}. This is required for devices running
         * OS version < Android M
         */
        private const val KEY_ALGORITHM_AES = "AES"

        private const val KEY_PERIOD_IN_YEARS = 15

        private const val FAILED_TO_REMOVE_KEY = "Failed to generate Key"

        private const val FAILED_TO_ENCRYPT_WITH_ALIAS = "Failed to encrypt with alias"

        private const val ENCRYPT_RSA_FAILED = "Encrypt key with $KEY_ALGORITHM_RSA failed."

        private const val DECRYPT_RSA_FAILED = "Decrypt key with $KEY_ALGORITHM_RSA failed."
    }

    //region EncryptionAlgorithm
    override fun generateKey(alias: String) {
        if (keyStore.containsAlias(alias).not()) {
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, KEY_PERIOD_IN_YEARS)

            val generatorSpec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(X500Principal("$PREFIX_SUBJECT_PRINCIPAL$alias"))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.time)
                .setEndDate(end.time)
                .build()
            try {
                KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, keyStore.type).apply {
                    initialize(generatorSpec)
                    generateKeyPair()
                }
                createAesKey(alias)
            } catch (nsa: NoSuchAlgorithmException) {
                throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_REMOVE_KEY $alias.", nsa)
            } catch (nsp: NoSuchProviderException) {
                throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_REMOVE_KEY $alias.", nsp)
            } catch (iape: InvalidAlgorithmParameterException) {
                throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_REMOVE_KEY $alias.", iape)
            }
        }
    }

    override fun getKey(alias: String): Key {
        return SecretKeySpec(rsaDecrypt(settings.getForAlias(alias), alias), KEY_ALGORITHM_AES)
    }

    override fun removeKey(alias: String) {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                settings.removeForAlias(alias)
            }
        } catch (kse: KeyStoreException) {
            throw EncryptionAlgorithm.AlgorithmException("Failed to remove Key $alias.", kse)
        }
    }

    override fun doEncryption(alias: String, plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_MODE, PROVIDER_BC).apply {
                init(Cipher.ENCRYPT_MODE, getKey(alias))
            }
            android.util.Base64.encodeToString(cipher.doFinal(plainText.toByteArray(Charset.defaultCharset())), android.util.Base64.NO_WRAP)
        } catch (nsae: NoSuchAlgorithmException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", nsae)
        } catch (nspre: NoSuchProviderException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", nspre)
        } catch (nspae: NoSuchPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", nspae)
        } catch (ike: InvalidKeyException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", ike)
        } catch (bpe: BadPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", bpe)
        } catch (ibse: IllegalBlockSizeException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", ibse)
        }
    }

    override fun doDecryption(alias: String, encryptedText: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_MODE, PROVIDER_BC).apply {
                init(Cipher.DECRYPT_MODE, getKey(alias))
            }
            String(cipher.doFinal(android.util.Base64.decode(encryptedText.toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP)), Charset.defaultCharset())
        } catch (nsae: NoSuchAlgorithmException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", nsae)
        } catch (nspre: NoSuchProviderException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", nspre)
        } catch (nspae: NoSuchPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", nspae)
        } catch (bpe: BadPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", bpe)
        } catch (ibse: IllegalBlockSizeException) {
            throw EncryptionAlgorithm.AlgorithmException("$FAILED_TO_ENCRYPT_WITH_ALIAS $alias.", ibse)
        }
    }
    //endregion

    private fun rsaEncrypt(secret: ByteArray, alias: String): String {
        return try {
            val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
            val cipher = Cipher.getInstance(RSA_MODE, PROVIDER_ANDROID_SSL).apply {
                init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)
            }
            android.util.Base64.encodeToString(cipher.doFinal(secret), android.util.Base64.NO_WRAP)
        } catch (uee: UnrecoverableEntryException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, uee)
        } catch (ke: KeyStoreException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, ke)
        } catch (nspe: NoSuchProviderException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, nspe)
        } catch (nspae: NoSuchPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, nspae)
        } catch (ike: InvalidKeyException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, ike)
        } catch (bpe: BadPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, bpe)
        } catch (ibse: IllegalBlockSizeException) {
            throw EncryptionAlgorithm.AlgorithmException(ENCRYPT_RSA_FAILED, ibse)
        }
    }

    fun rsaDecrypt(base64encrypted: String, alias: String): ByteArray {
        return try {
            val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
            val cipher = Cipher.getInstance(RSA_MODE, PROVIDER_ANDROID_SSL).apply {
                init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
            }
            cipher.doFinal(android.util.Base64.decode(base64encrypted.toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP))
        } catch (nsae: NoSuchAlgorithmException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, nsae)
        } catch (uee: UnrecoverableEntryException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, uee)
        } catch (kse: KeyStoreException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, kse)
        } catch (nspre: NoSuchProviderException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, nspre)
        } catch (nspae: NoSuchPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, nspae)
        } catch (ike: InvalidKeyException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, ike)
        } catch (bpe: BadPaddingException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, bpe)
        } catch (ibe: IllegalBlockSizeException) {
            throw EncryptionAlgorithm.AlgorithmException(DECRYPT_RSA_FAILED, ibe)
        }
    }

    private fun createAesKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES).apply {
            init(KEY_LENGTH_IN_BYTES)
        }
        val secretKey = keyGenerator.generateKey()
        settings.putForAlias(alias, rsaEncrypt(secretKey.encoded, alias))
    }
}
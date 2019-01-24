import org.apache.commons.net.ftp.FTPSClient
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Prints the server certificates for an FTPS server (port 21) and writes the first certificate to a keystore.
 *
 * Usage: DownloadFtpsServerCertificateKt ftps.example.com
 *
 * …or override port explicitly: DownloadFtpsServerCertificateKt ftps.example.com 1234
 */
fun main(args: Array<String>) {
    val host = args[0]
    val port = if (args.size >= 2) args[1].toInt() else 21

    val trustManager = object : X509TrustManager {

        override fun getAcceptedIssuers(): Array<X509Certificate>? = null

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            val certificates = chain.asList()

            certificates.forEach {
                println(it)
            }

            writeCertificateToKeyStoreFile(certificates.first())
        }

        private fun writeCertificateToKeyStoreFile(certificate: X509Certificate, alias: String = "alias") {
            val keyStore = createEmptyKeyStore()
            keyStore.setCertificateEntry(alias, certificate)
            writeKeyStoreToFile(keyStore)
        }

    }

    val sslContext = createSSLContext(trustManager)
    val client = FTPSClient(sslContext)
    client.connect(host, port)
}

private fun createEmptyKeyStore(password: String = ""): KeyStore =
    KeyStore.getInstance(KeyStore.getDefaultType())
        .apply { load(null, password.toCharArray()) }

private fun writeKeyStoreToFile(
    keyStore: KeyStore,
    file: File = File("keystore.keystore"),
    password: String = ""
) {
    FileOutputStream(file).use { out ->
        keyStore.store(out, password.toCharArray())
    }
}

private fun createSSLContext(trustManager: X509TrustManager): SSLContext =
    SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustManager), null)
    }

package com.zioanacleto.feedtracker.features.auth.services

import com.zioanacleto.feedtracker.config.SmtpConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

interface EmailSender {
    suspend fun sendVerification(to: String, code: String, link: String)
    suspend fun sendPasswordReset(to: String, code: String, link: String, ssoOnly: Boolean)
}

class LoggingEmailSender : EmailSender {
    private val logger = KotlinLogging.logger {}

    override suspend fun sendVerification(to: String, code: String, link: String) {
        logger.info { "Verification email for $to code=$code link=$link" }
    }

    override suspend fun sendPasswordReset(to: String, code: String, link: String, ssoOnly: Boolean) {
        logger.info { "Password reset email for $to ssoOnly=$ssoOnly code=$code link=$link" }
    }
}

class SmtpEmailSender(private val config: SmtpConfig) : EmailSender {
    override suspend fun sendVerification(to: String, code: String, link: String) = send(
        to = to,
        subject = "Il tuo codice FeedTracker",
        body = """
            Ciao,

            Usa questo codice per continuare la registrazione su FeedTracker: $code

            Puoi anche aprire questo link: $link

            Il codice scade a breve. Se non hai richiesto tu questo accesso, ignora questa email.
        """.trimIndent(),
    )

    override suspend fun sendPasswordReset(to: String, code: String, link: String, ssoOnly: Boolean) {
        val body = if (ssoOnly) {
            """
            Ciao,

            Questo account accede con Google o Apple e non ha ancora una password.

            Se vuoi impostarne una, usa questo codice in FeedTracker: $code

            Puoi anche aprire questo link: $link

            Se non hai richiesto tu questo messaggio, ignora questa email.
            """.trimIndent()
        } else {
            """
            Ciao,

            Usa questo codice per reimpostare la password di FeedTracker: $code

            Puoi anche aprire questo link: $link

            Il codice scade a breve. Se non hai richiesto tu il reset, ignora questa email.
            """.trimIndent()
        }
        send(to = to, subject = "Reimposta la password di FeedTracker", body = body)
    }

    private suspend fun send(to: String, subject: String, body: String) = withContext(Dispatchers.IO) {
        val properties = Properties().apply {
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            put("mail.smtp.auth", config.username.isNotBlank().toString())
            put("mail.smtp.starttls.enable", config.startTls.toString())
        }
        val session = if (config.username.isBlank()) {
            Session.getInstance(properties)
        } else {
            Session.getInstance(
                properties,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication =
                        PasswordAuthentication(config.username, config.password)
                },
            )
        }
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            this.subject = subject
            setText(body)
        }
        Transport.send(message)
    }
}

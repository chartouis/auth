package yzarr.auth.service;

import java.net.URI;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    @Async("mailTaskExecutor")
    public void sendEmailVerificationMessage(String token, String to, String path) {
        try {
            String verificationLink = buildVerificationLink(token, path);

            String html = buildEmailHtml(verificationLink);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("noreply@yzarr.com");
            helper.setTo(to);
            helper.setSubject("Email verification");
            helper.setText(html, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildVerificationLink(String token, String path) {
        return UriComponentsBuilder
                .fromUri(URI.create("http://localhost:9001"))
                .path(path)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String buildEmailHtml(String link) {
        return """
                <html>
                    <body>
                        <p>Email verification request</p>
                        <p>
                            <a href="%s">Click here to continue</a>
                        </p>
                        <p>If you did not request this, ignore this email.</p>
                    </body>
                </html>
                """.formatted(link);
    }
}

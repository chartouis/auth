package yzarr.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
    private final JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMessage(String text, String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply");
        message.setTo(to);
        message.setSubject("AUTH");
        message.setText(text);
        javaMailSender.send(message);
    }

    public void sendEmailVerificationMessage(String token, String to) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // HARDCODED
            String verificationLink = "http://localhost:9001/auth/verify/email?token=" + token;

            String html = """
                    <html>
                    <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,sans-serif;">
                        <div style="max-width:600px;margin:40px auto;background:#ffffff;
                                    border-radius:8px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                            <h2 style="margin-top:0;color:#222;">Email verification</h2>

                            <p style="color:#555;font-size:14px;line-height:1.5;">
                                Click the button below to verify your email address.
                            </p>

                            <div style="text-align:center;margin:24px 0;">
                                <a href="%s"
                                   style="background:#2563eb;color:#ffffff;
                                          padding:12px 20px;text-decoration:none;
                                          border-radius:6px;font-weight:bold;
                                          display:inline-block;">
                                    Verify email
                                </a>
                            </div>

                            <p style="color:#999;font-size:12px;">
                                If you did not request this, ignore this email.
                            </p>
                        </div>
                    </body>
                    </html>
                    """.formatted(verificationLink);

            helper.setFrom("noreply");
            helper.setTo(to);
            helper.setSubject("AUTH");
            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

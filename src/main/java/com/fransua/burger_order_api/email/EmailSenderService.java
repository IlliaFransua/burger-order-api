package com.fransua.burger_order_api.email;

import com.fransua.burger_order_api.email.EmailMessage.MessageStatus;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

  private final JavaMailSender emailSender;
  private final String fromAddress;
  private final EmailMessageRepository emailMessageRepository;

  public EmailSenderService(
      JavaMailSender emailSender,
      @Value("${spring.mail.username}") String fromAddress,
      EmailMessageRepository emailMessageRepository) {
    this.emailSender = emailSender;
    this.fromAddress = fromAddress;
    this.emailMessageRepository = emailMessageRepository;
  }

  public void sendEmail(String to, String subject, String content) {
    EmailMessage savedMessage = saveEmailMessage(to, subject, content);
    sendEmail(savedMessage);
  }

  public void sendEmail(EmailMessage savedMessage) {
    try {
      SimpleMailMessage mail = new SimpleMailMessage();
      mail.setFrom(fromAddress);
      mail.setTo(savedMessage.getTo());
      mail.setSubject(savedMessage.getSubject());
      mail.setText(savedMessage.getContent());

      emailSender.send(mail);

      savedMessage.setStatus(MessageStatus.SENT);

    } catch (Exception e) {
      savedMessage.setStatus(MessageStatus.ERROR);
      savedMessage.setErrorMessage(
          "[%s]: [%s]".formatted(e.getClass().getSimpleName(), e.getMessage()));
      savedMessage.incrementAttempts();
      savedMessage.setLastAttemptTime(Instant.now());

    } finally {
      emailMessageRepository.save(savedMessage);
    }
  }

  private EmailMessage saveEmailMessage(String to, String subject, String content) {
    EmailMessage emailMessage = new EmailMessage();

    emailMessage.setTo(to);
    emailMessage.setSubject(subject);
    emailMessage.setContent(content);
    emailMessage.setStatus(MessageStatus.NEW);

    return emailMessageRepository.save(emailMessage);
  }
}

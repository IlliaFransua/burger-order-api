package com.fransua.burger_order_api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fransua.burger_order_api.email.EmailMessage;
import com.fransua.burger_order_api.email.EmailMessage.MessageStatus;
import com.fransua.burger_order_api.email.EmailMessageRepository;
import com.fransua.burger_order_api.email.EmailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
public class EmailSenderServiceTest {

  @Autowired private EmailSenderService emailSenderService;

  @Autowired private EmailMessageRepository emailMessageRepository;

  @MockBean private JavaMailSender javaMailSender;

  @BeforeEach
  void clearDb() {
    emailMessageRepository.deleteAll();
  }

  @Test
  void testSuccessEmailSendin() {
    String to = "success@success.success";

    emailSenderService.sendEmail(to, "Subject", "Content");

    EmailMessage savedMessage = emailMessageRepository.findAll().iterator().next();

    assertThat(savedMessage.getStatus()).isEqualTo(MessageStatus.SENT);
    assertThat(savedMessage.getErrorMessage()).isNull();
    verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  void testFailedEmailSending() {
    doThrow(new RuntimeException("SMTP Connection refused"))
        .when(javaMailSender)
        .send(any(SimpleMailMessage.class));

    String to = "fail@fail.fail";

    emailSenderService.sendEmail(to, "Subject", "Content");

    EmailMessage saved = emailMessageRepository.findAll().iterator().next();

    assertThat(saved.getStatus()).isEqualTo(MessageStatus.ERROR);
    assertThat(saved.getErrorMessage()).isNotNull().contains("SMTP Connection refused");
    assertThat(saved.getAttemptsCount()).isEqualTo(1);
  }
}

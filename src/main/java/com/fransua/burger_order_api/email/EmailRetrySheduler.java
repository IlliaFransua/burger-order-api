package com.fransua.burger_order_api.email;

import com.fransua.burger_order_api.email.EmailMessage.MessageStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EmailRetrySheduler {

  private final EmailMessageRepository emailMessageRepository;
  private final EmailSenderService emailSenderService;

  @Scheduled(fixedDelay = 30_000) // 5 min
  public void retryFailedEmails() {
    log.info("Starting background retry send for failed emails...");

    List<EmailMessage> failedEmails =
        emailMessageRepository.findByStatusAndAttemptsCountLessThan(MessageStatus.ERROR, 5);

    if (failedEmails.isEmpty()) {
      log.info("No failed emails found for retry.");
      return;
    }

    log.info("Found {} email to retry. Starting re-sending...", failedEmails.size());

    for (EmailMessage failedEmail : failedEmails) {
      log.info(
          "Retrying email: #{}, attempts: {}", failedEmail.getId(), failedEmail.getAttemptsCount());

      emailSenderService.sendEmail(failedEmail);
    }
  }
}

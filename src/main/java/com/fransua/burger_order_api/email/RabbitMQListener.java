package com.fransua.burger_order_api.email;

import com.fransua.burger_order_api.order.dto.request.OrderCreatedEmailNotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RabbitMQListener {

  private final EmailSenderService emailSenderService;

  @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE_NAME)
  public void listen(OrderCreatedEmailNotificationRequest request) {
    emailSenderService.sendEmail(request.to(), request.subject(), request.content());
  }
}

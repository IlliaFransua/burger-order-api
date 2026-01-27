package com.fransua.burger_order_api.email;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailMessageRepository extends ElasticsearchRepository<EmailMessage, String> {
  List<EmailMessage> findByStatusAndAttemptsCountLessThan(
      EmailMessage.MessageStatus status, int maxAttempts);
}

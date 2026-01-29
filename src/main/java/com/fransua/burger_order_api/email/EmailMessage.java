package com.fransua.burger_order_api.email;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(indexName = "emails")
public class EmailMessage {

  @Id @EqualsAndHashCode.Include private String id;

  @Field(type = FieldType.Keyword)
  private String to;

  @Field(type = FieldType.Text)
  private String subject;

  @Field(type = FieldType.Text)
  private String content;

  @Field(type = FieldType.Keyword)
  private MessageStatus status;

  @Field(type = FieldType.Text)
  private String errorMessage;

  @Field(type = FieldType.Integer)
  private int attemptsCount;

  @Field(type = FieldType.Date)
  private Instant lastAttemptTime;

  public enum MessageStatus {
    NEW,
    SENT,
    ERROR,
    RETRYING
  }

  public void incrementAttempts() {
    this.attemptsCount++;
  }
}

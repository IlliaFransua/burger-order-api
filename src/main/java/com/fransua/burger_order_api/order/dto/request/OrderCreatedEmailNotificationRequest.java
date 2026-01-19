package com.fransua.burger_order_api.order.dto.request;

public record OrderCreatedEmailNotificationRequest(String to, String subject, String content) {}

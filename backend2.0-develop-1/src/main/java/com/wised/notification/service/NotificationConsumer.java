package com.wised.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @KafkaListener(topics = "FOLLOW", groupId = "notification-group")
    public void consumeFollowNotification(String message) {
        // Process follow notification
        System.out.println("Received Follow Notification: " + message);
        // Add logic to handle the follow notification, e.g., save to database
    }
}

package com.example.neura_search.kafka;

import com.example.neura_search.service.DirectoryProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DirectoryProcessingConsumer {
    private final DirectoryProcessingService directoryProcessingService;

    public DirectoryProcessingConsumer(DirectoryProcessingService directoryProcessingService) {
        this.directoryProcessingService = directoryProcessingService;
    }

    @KafkaListener(topics = "directory-processing-topic", groupId = "neura-search-group")
    public void consume(@Payload String message) {
        try {
            String[] parts = message.split(",", 2);
            if (parts.length == 2) {
                UUID jobId = UUID.fromString(parts[0]);
                String directoryPath = parts[1];
                directoryProcessingService.processDirectory(jobId, directoryPath);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}

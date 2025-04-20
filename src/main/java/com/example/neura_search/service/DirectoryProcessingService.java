package com.example.neura_search.service;

import com.example.neura_search.model.ProcessingJob;
import com.example.neura_search.model.ProcessingStatus;
import com.example.neura_search.parser.DocumentParser;
import com.example.neura_search.parser.DocumentParserFactory;
import com.example.neura_search.repository.ProcessingJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DirectoryProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryProcessingService.class);
    private final ProcessingJobRepository processingJobRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DocumentParserFactory parserFactory;
    private final EmbeddingService embeddingService;
    private final VectorDbService vectorDbService;

    public DirectoryProcessingService(
            ProcessingJobRepository processingJobRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            DocumentParserFactory parserFactory,
            EmbeddingService embeddingService,
            VectorDbService vectorDbService) {
        this.processingJobRepository = processingJobRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.parserFactory = parserFactory;
        this.embeddingService = embeddingService;
        this.vectorDbService = vectorDbService;
    }

    @Transactional
    public UUID scheduleDirectoryProcessing(String directoryPath) {
        // Validate directory exists
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        // Create processing job
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = new ProcessingJob(jobId, directoryPath, ProcessingStatus.QUEUED);
        processingJobRepository.save(job);

        // Send to Kafka for async processing
        kafkaTemplate.send("directory-processing-topic", jobId.toString(), directoryPath);

        return jobId;
    }

    @Transactional(readOnly = true)
    public ProcessingStatus getProcessingStatus(UUID processId) {
        return processingJobRepository.findById(processId)
                .map(ProcessingJob::getStatus)
                .orElseThrow(() -> new IllegalArgumentException("Processing job not found"));
    }

    @Transactional
    public void processDirectory(UUID jobId, String directoryPath) {
        try {
            ProcessingJob job = processingJobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found"));

            job.setStatus(ProcessingStatus.PROCESSING);
            processingJobRepository.save(job);

            File directory = new File(directoryPath);
            processFilesRecursively(directory);

            job.setStatus(ProcessingStatus.COMPLETED);
            processingJobRepository.save(job);

        } catch (Exception e) {
            logger.error("Error processing directory", e);
            ProcessingJob job = processingJobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(ProcessingStatus.FAILED);
                job.setErrorMessage(e.getMessage());
                processingJobRepository.save(job);
            }
        }
    }

    private void processFilesRecursively(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processFilesRecursively(file);
            } else {
                try {
                    DocumentParser parser = parserFactory.getParser(file);
                    if (parser != null) {
                        String content = parser.parseDocument(file);

                        // Generate embeddings and store in vector DB
                        float[] embedding = embeddingService.generateEmbedding(content);
                        Long vectorId = vectorDbService.storeEmbedding(embedding, file.getAbsolutePath());

                        // Store document metadata
                        vectorDbService.storeDocumentMetadata(file.getAbsolutePath(), content,
                                getFileType(file), vectorId);
                    }
                } catch (Exception e) {
                    logger.error("Error processing file: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private String getFileType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) return "PDF";
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) return "EXCEL";
        return "UNKNOWN";
    }
}

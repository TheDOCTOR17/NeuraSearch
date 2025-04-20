package com.example.neura_search.service;

import com.example.neura_search.dto.SearchResultDTO;
import com.example.neura_search.model.DocumentEmbedding;
import com.example.neura_search.repository.DocumentEmbeddingRepository;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final MilvusServiceClient milvusClient;
    private final EmbeddingService embeddingService;
    private final DocumentEmbeddingRepository documentRepository;
    private static final String COLLECTION_NAME = "documents";

    public SearchService(
            MilvusServiceClient milvusClient,
            EmbeddingService embeddingService,
            DocumentEmbeddingRepository documentRepository) {
        this.milvusClient = milvusClient;
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResultDTO> search(String query, int limit, float minimumScore) {
        try {
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return new ArrayList<>();
            }

            // Generate embeddings for the query
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            if (queryEmbedding == null || queryEmbedding.length == 0) {
                logger.warn("Failed to generate embedding for query: {}", query);
                return fallbackSearch(query, limit);
            }

            // Search in Milvus
            List<List<Float>> vectors = new ArrayList<>();
            List<Float> queryVector = new ArrayList<>(queryEmbedding.length);
            for (float v : queryEmbedding) {
                queryVector.add(v);
            }
            vectors.add(queryVector);

            // For Milvus 2.5.7 compatibility
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withOutFields(List.of("id", "path"))
                    .withTopK(limit)
                    .withVectors(vectors)
                    .withVectorFieldName("embedding")
                    .withParams("{\"metric_type\": \"COSINE\", \"nprobe\": 10}")
                    .build();

            R<SearchResults> response = milvusClient.search(searchParam);

            if (!response.getStatus().equals(R.Status.Success.getCode())) {
                logger.warn("Vector search failed with status: {}. Message: {}",
                        response.getStatus(), response.getMessage());
                return fallbackSearch(query, limit);
            }

            SearchResults rawResults = response.getData();
            if (rawResults == null) {
                logger.info("No vector search results found for query: {}", query);
                return fallbackSearch(query, limit);
            }

            List<Long> documentIds = new ArrayList<>();
            Map<Long, Float> scoreMap = new HashMap<>();

            SearchResultsWrapper resultsWrapper = new SearchResultsWrapper(rawResults.getResults());
            int numQueries = resultsWrapper.getRowRecords().size();

            if (numQueries == 0) {
                logger.info("No search results found for query: {}", query);
                return fallbackSearch(query, limit);
            }

            for (int i = 0; i < numQueries; i++) {
                List<SearchResultsWrapper.IDScore> idScoreList = resultsWrapper.getIDScore(i);
                for (SearchResultsWrapper.IDScore idScore : idScoreList) {
                    long docId = idScore.getLongID();  // use getLongID() for Int64 PK
                    float score = idScore.getScore();
                    documentIds.add(docId);
                    scoreMap.put(docId, score);
                }
            }


            if (documentIds.isEmpty()) {
                logger.info("No vector IDs found in search results for query: {}", query);
                return fallbackSearch(query, limit);
            }

            // Retrieve documents by vectorIds - using standard JPA methods
            List<DocumentEmbedding> allDocuments = documentRepository.findAll();
            List<DocumentEmbedding> documents = allDocuments.stream()
                    .filter(doc -> documentIds.contains(doc.getVectorId()))
                    .collect(Collectors.toList());

            if (documents.isEmpty()) {
                logger.warn("No documents found for vector IDs: {}", documentIds);
                return fallbackSearch(query, limit);
            }

            // Map to search results with scores
            List<SearchResultDTO> searchResults = new ArrayList<>();
            for (DocumentEmbedding doc : documents) {
                Float score = scoreMap.get(doc.getVectorId());
                if (score != null && score >= minimumScore) {
                    searchResults.add(new SearchResultDTO(
                            doc.getFilePath(),
                            doc.getContent(),
                            score,
                            doc.getFileType()
                    ));
                }
            }

            // Sort by score (highest first)
            searchResults.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            logger.info("Vector search completed successfully for query: {}, found {} results",
                    query, searchResults.size());
            return searchResults;

        } catch (Exception e) {
            logger.error("Error during vector search: {}", e.getMessage(), e);
            return fallbackSearch(query, limit);
        }
    }

    private List<SearchResultDTO> fallbackSearch(String query, int limit) {
        logger.info("Using fallback search for query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Simple keyword-based fallback search when vector search fails
            String lowerCaseQuery = query.toLowerCase();

            List<DocumentEmbedding> documents = documentRepository.findAll();

            List<SearchResultDTO> results = documents.stream()
                    .filter(doc -> {
                        String content = doc.getContent() != null ? doc.getContent().toLowerCase() : "";
                        return content.contains(lowerCaseQuery);
                    })
                    .map(doc -> {
                        // Calculate a basic relevance score based on occurrences
                        String content = doc.getContent() != null ? doc.getContent().toLowerCase() : "";
                        int occurrences = countOccurrences(content, lowerCaseQuery);
                        float score = Math.min(0.7f + (occurrences * 0.05f), 0.95f);

                        return new SearchResultDTO(
                                doc.getFilePath(),
                                doc.getContent(),
                                score,
                                doc.getFileType()
                        );
                    })
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            logger.info("Fallback search completed for query: {}, found {} results", query, results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error during fallback search: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private int countOccurrences(String text, String searchTerm) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(searchTerm, index)) != -1) {
            count++;
            index += searchTerm.length();
        }
        return count;
    }
}
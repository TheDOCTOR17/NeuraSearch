package com.example.neura_search.service;

import com.example.neura_search.model.DocumentEmbedding;
import com.example.neura_search.repository.DocumentEmbeddingRepository;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VectorDbService {

    private final MilvusServiceClient milvusClient;
    private final DocumentEmbeddingRepository documentRepository;
    private static final String COLLECTION_NAME = "documents";
    private static final int VECTOR_DIMENSION = 1536;
    private final AtomicLong idCounter = new AtomicLong(1);

    public VectorDbService(MilvusServiceClient milvusClient, DocumentEmbeddingRepository documentRepository) {
        this.milvusClient = milvusClient;
        this.documentRepository = documentRepository;
        initializeCollection();
    }

    private void initializeCollection() {
        try {
            HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .build();

            R<Boolean> response = milvusClient.hasCollection(hasCollectionParam);
            boolean collectionExists = response.getData();

            if (!collectionExists) {
                FieldType idField = FieldType.newBuilder()
                        .withName("id")
                        .withDataType(DataType.Int64)
                        .withPrimaryKey(true)
                        .withAutoID(false)
                        .build();

                FieldType pathField = FieldType.newBuilder()
                        .withName("path")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(1024)
                        .build();

                FieldType vectorField = FieldType.newBuilder()
                        .withName("embedding")
                        .withDataType(DataType.FloatVector)
                        .withDimension(VECTOR_DIMENSION)
                        .build();

                CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                        .withCollectionName(COLLECTION_NAME)
                        .withDescription("Document embeddings for semantic search")
                        .addFieldType(idField)
                        .addFieldType(pathField)
                        .addFieldType(vectorField)
                        .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                        .build();

                milvusClient.createCollection(createCollectionReq);
            }
        } catch (Exception e) {
            System.err.println("Error initializing Milvus collection: " + e.getMessage());
        }
    }

    @Transactional
    public Long storeEmbedding(float[] embedding, String filePath) {
        try {
            long vectorId = idCounter.getAndIncrement();

            List<InsertParam.Field> fields = new ArrayList<>();

            // ID field
            List<Long> ids = new ArrayList<>();
            ids.add(vectorId);
            fields.add(new InsertParam.Field("id", ids));

            // Path field
            List<String> paths = new ArrayList<>();
            paths.add(filePath);
            fields.add(new InsertParam.Field("path", paths));

            // Embedding field
            List<List<Float>> vectors = new ArrayList<>();
            List<Float> vector = new ArrayList<>();
            for (float v : embedding) {
                vector.add(v);
            }
            vectors.add(vector);
            fields.add(new InsertParam.Field("embedding", vectors));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(COLLECTION_NAME)
                    .withFields(fields)
                    .build();

            R<MutationResult> response = milvusClient.insert(insertParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                System.err.println("Error inserting vector: " + response.getMessage());
            }

            return vectorId;
        } catch (Exception e) {
            System.err.println("Exception in vector store: " + e.getMessage());
            return idCounter.getAndIncrement();
        }
    }

    @Transactional
    public void storeDocumentMetadata(String filePath, String content, String fileType, Long vectorId) {
        DocumentEmbedding document = new DocumentEmbedding();
        document.setFilePath(filePath);

        String contentPreview = content.length() > 5000 ? content.substring(0, 5000) + "..." : content;
        document.setContent(contentPreview);

        document.setFileType(fileType);
        document.setVectorId(vectorId);

        documentRepository.save(document);
    }
}
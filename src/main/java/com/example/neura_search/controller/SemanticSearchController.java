package com.example.neura_search.controller;

import com.example.neura_search.dto.DirectoryRequestDTO;
import com.example.neura_search.dto.SearchRequestDTO;
import com.example.neura_search.dto.SearchResultDTO;
import com.example.neura_search.service.DirectoryProcessingService;
import com.example.neura_search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/semantic")
public class SemanticSearchController {
    private final DirectoryProcessingService directoryProcessingService;
    private final SearchService searchService;

    public SemanticSearchController(DirectoryProcessingService directoryProcessingService,
                                    SearchService searchService) {
        this.directoryProcessingService = directoryProcessingService;
        this.searchService = searchService;
    }

    @PostMapping("/process-directory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> processDirectory(@RequestBody DirectoryRequestDTO request) {
        try {
            UUID processId = directoryProcessingService.scheduleDirectoryProcessing(request.getDirectoryPath());
            return ResponseEntity.ok("Directory processing scheduled with ID: " + processId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error scheduling directory processing: " + e.getMessage());
        }
    }

    @GetMapping("/process-status/{processId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getProcessingStatus(@PathVariable UUID processId) {
        return ResponseEntity.ok(directoryProcessingService.getProcessingStatus(processId));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<SearchResultDTO>> search(@RequestBody SearchRequestDTO request) {
        try {
            List<SearchResultDTO> results = searchService.search(
                    request.getQuery(),
                    request.getLimit(),
                    request.getMinimumScore()
            );
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}

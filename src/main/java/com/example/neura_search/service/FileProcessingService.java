package com.example.neura_search.service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


@Service
public class FileProcessingService {
    private final Tika tika = new Tika();

    public String processFile(MultipartFile file) throws Exception {
        String fileType = file.getContentType();
        String extractedContent;

        if (fileType != null && fileType.contains("pdf")) {
            extractedContent = extractTextFromPDF(file);
        } else if (fileType != null && fileType.contains("excel")) {
            extractedContent = extractTextFromExcel(file);
        } else {
            return "Unsupported file format. Only PDF and Excel files are allowed.";
        }

        saveToFile(extractedContent);
        return "File processed successfully! Extracted content saved.";
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException, TikaException {
        return tika.parseToString(file.getInputStream());
    }

    private String extractTextFromExcel(MultipartFile file) throws IOException {
        StringBuilder extractedText = new StringBuilder();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        extractedText.append(cell.toString()).append("\t");
                    }
                    extractedText.append("\n");
                }
            }
        }
        return extractedText.toString();
    }

    private void saveToFile(String content) throws IOException {
        String OUTPUT_FILE = "extracted_data.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) {
            writer.write(content);
            writer.newLine();
        }
    }


}

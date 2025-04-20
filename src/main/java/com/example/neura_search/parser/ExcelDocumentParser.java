package com.example.neura_search.parser;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.StringJoiner;

@Component
public class ExcelDocumentParser implements DocumentParser{
    @Override
    public String parseDocument(File file) throws Exception {
        StringBuilder content = new StringBuilder();

        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                content.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                Iterator<Row> rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    StringJoiner cellJoiner = new StringJoiner(" | ");

                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        cellJoiner.add(cellValue);
                    }

                    content.append(cellJoiner.toString()).append("\n");
                }
                content.append("\n");
            }
        }

        return content.toString();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    @Override
    public boolean supports(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".xlsx") || name.endsWith(".xls");
    }
}

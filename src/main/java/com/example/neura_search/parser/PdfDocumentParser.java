package com.example.neura_search.parser;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

@Component
public class PdfDocumentParser implements DocumentParser{
    @Override
    public String parseDocument(File file) throws Exception {
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata, new ParseContext());
            return handler.toString();
        }
    }

    @Override
    public boolean supports(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf");
    }
}

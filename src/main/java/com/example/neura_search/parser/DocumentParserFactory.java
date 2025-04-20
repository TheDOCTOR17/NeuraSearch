package com.example.neura_search.parser;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DocumentParserFactory {
    private final List<DocumentParser> parsers;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public DocumentParser getParser(File file) {
        return parsers.stream()
                .filter(parser -> parser.supports(file))
                .findFirst()
                .orElse(null);
    }
}

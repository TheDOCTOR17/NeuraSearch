package com.example.neura_search.parser;

import java.io.File;

public interface DocumentParser {
    String parseDocument(File file) throws Exception;
    boolean supports(File file);
}

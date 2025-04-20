package com.example.neura_search.dto;

public class SearchRequestDTO {
    private String query;
    private int limit = 10;
    private float minimumScore = 0.7f;

    public SearchRequestDTO() {
    }

    public SearchRequestDTO(String query, int limit, float minimumScore) {
        this.query = query;
        this.limit = limit;
        this.minimumScore = minimumScore;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public float getMinimumScore() {
        return minimumScore;
    }

    public void setMinimumScore(float minimumScore) {
        this.minimumScore = minimumScore;
    }
}

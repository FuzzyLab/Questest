package com.fuzzylabs.questest;

/**
 * Created by sharm on 01-08-2016.
 */
public class ResponseData {
    private String query;
    private Entry[] entries;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Entry[] getEntries() {
        return entries;
    }

    public void setEntries(Entry[] entries) {
        this.entries = entries;
    }
}

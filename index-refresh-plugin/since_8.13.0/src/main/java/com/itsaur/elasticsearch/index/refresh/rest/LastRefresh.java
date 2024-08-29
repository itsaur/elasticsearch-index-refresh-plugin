package com.itsaur.elasticsearch.index.refresh.rest;

import org.elasticsearch.rest.RestRequest;

public record LastRefresh(Long value) {
    public static final String REST_PROPERTY = "last_refresh";

    public static LastRefresh parseOrDefault(RestRequest request) {
        if (!request.hasParam(REST_PROPERTY)) {
            return null;
        }

        long value = request.paramAsLong(REST_PROPERTY, Long.MIN_VALUE);
        if (value == Long.MIN_VALUE) {
            return null;
        }

        if (value < 0) {
            throw new RuntimeException(REST_PROPERTY + " cannot be negative");
        }

        return new LastRefresh(value);
    }
}

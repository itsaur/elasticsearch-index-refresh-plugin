package com.itsaur.elasticsearch.index.refresh.rest;

import org.elasticsearch.rest.RestRequest;

public record Wait(boolean value) {
    public static final String REST_PROPERTY = "wait";

    public static Wait parseOrDefault(RestRequest request) {
        return new Wait(request.paramAsBoolean(REST_PROPERTY, true));
    }
}

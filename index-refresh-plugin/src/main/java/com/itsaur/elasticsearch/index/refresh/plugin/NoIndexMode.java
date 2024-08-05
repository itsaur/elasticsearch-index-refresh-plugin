package com.itsaur.elasticsearch.index.refresh.plugin;

import org.elasticsearch.rest.RestRequest;

import java.util.Arrays;

/**
 * <p>Indicates how a request to /{index}/wait_refresh should handle the case where the index does not exist.</p>
 * There are 3 cases:
 * <ol>
 *     <li>FAIL, returns an error (default)</li>
 *     <li>Exit, returns 0</li>
 *     <li>WAIT, waits until the index is created</li>
 * </ol>
 */
public enum NoIndexMode {
    Fail,
    Exit,
    Wait;

    public static final String REST_PROPERTY = "no_index";

    public static NoIndexMode parseOrDefault(RestRequest request) {
        return parseOrDefault(request.param(REST_PROPERTY));
    }

    public static NoIndexMode parseOrDefault(String mode) {
        if (mode == null || mode.isEmpty()) {
            return Fail;
        }

        return Arrays
                .stream(values())
                .filter(noIndexMode -> mode.equalsIgnoreCase(noIndexMode.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown mode [" + mode + "] for '" + REST_PROPERTY + "'"));
    }
}

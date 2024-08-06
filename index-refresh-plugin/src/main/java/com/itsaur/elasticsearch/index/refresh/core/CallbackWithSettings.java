package com.itsaur.elasticsearch.index.refresh.core;

import java.util.Objects;

public record CallbackWithSettings(IndexRefreshCallback callback, IndexRefreshCallback.Settings settings) {
    public CallbackWithSettings {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(settings);
    }
}

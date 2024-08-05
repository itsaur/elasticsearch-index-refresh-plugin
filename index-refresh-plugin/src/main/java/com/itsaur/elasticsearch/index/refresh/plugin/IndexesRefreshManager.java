package com.itsaur.elasticsearch.index.refresh.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IndexesRefreshManager {

    private final Map<String, IndexRefreshListener> indexRefreshListeners;

    public IndexesRefreshManager() {
        this.indexRefreshListeners = new HashMap<>();
    }

    /**
     * Registers a new {@link IndexRefreshListener} for the given index.
     * @param index The index to register the {@link IndexRefreshListener}.
     * @return The created {@link IndexRefreshListener}
     */
    public IndexRefreshListener registerListener(String index) {
        IndexRefreshListener indexRefreshListener = indexRefreshListeners.get(index);
        if (indexRefreshListener == null) {
            indexRefreshListener = new IndexRefreshListener(index);
            indexRefreshListeners.put(index, indexRefreshListener);
        }

        return indexRefreshListener;
    }

    /**
     * Adds a new {@link IndexRefreshedCallback} to the given index by using the given {@link IndexRefreshedCallback.Settings}.
     * @param index The index to add the callback.
     * @param settings The settings used for the callback
     * @param callback The callback that will be executed each time the index is updated.
     * @return An auto-generated id that can be used to remove the callback
     */
    public String addCallback(String index, IndexRefreshedCallback.Settings settings, IndexRefreshedCallback callback) {
        return getListener(index).addCallback(new IndexRefreshedCallback.CallbackWithSettings(callback, settings));
    }

    /**
     * Adds a new {@link IndexRefreshedCallback} to the given index by using the given {@link IndexRefreshedCallback#DEFAULT_SETTINGS}.
     * @param index The index to add the callback.
     * @param callback The callback that will be executed each time the index is updated.
     * @return An auto-generated id that can be used to remove the callback
     */
    public String addCallback(String index, IndexRefreshedCallback callback) {
        return addCallback(index, IndexRefreshedCallback.DEFAULT_SETTINGS.build(), callback);
    }

    public void removeCallback(String index, String callbackId) {
        getListener(index).removeCallback(callbackId);
    }

    public boolean hasListener(String index) {
        return indexRefreshListeners.containsKey(index);
    }

    /**
     * Returns how many times the given index has been refreshed so far.
     * @param index The index to get how many times it has been refreshed.
     * @return The number of the refreshes.
     */
    public long refreshes(String index) {
        return getListener(index).refreshCount();
    }

    private IndexRefreshListener getListener(String index) {
        return Objects.requireNonNull(
                indexRefreshListeners.get(index),
                "No refresh listener found for index [" + index + "]"
        );
    }
}

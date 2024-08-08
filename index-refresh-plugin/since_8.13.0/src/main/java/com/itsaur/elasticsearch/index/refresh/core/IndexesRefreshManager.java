package com.itsaur.elasticsearch.index.refresh.core;

import org.apache.lucene.search.ReferenceManager.RefreshListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the callback actions that will be executed on indices refreshes.
 */
public class IndexesRefreshManager {

    private final Map<String, IndexRefreshListener> indexRefreshListeners;

    public IndexesRefreshManager() {
        this.indexRefreshListeners = new HashMap<>();
    }

    /**
     * Registers a new {@link RefreshListener} for the given index or returns the one that is already registered.
     * @param index The index to register the {@link RefreshListener}.
     * @return The created or existing {@link RefreshListener}.
     */
    public RefreshListener registerListener(String index) {
        IndexRefreshListener indexRefreshListener = indexRefreshListeners.get(index);
        if (indexRefreshListener == null) {
            indexRefreshListener = new IndexRefreshListener(index);
            indexRefreshListeners.put(index, indexRefreshListener);
        }

        return indexRefreshListener;
    }

    /**
     * Adds a new {@link IndexRefreshCallback} to the given index by using the given {@link IndexRefreshCallback.Settings}.
     * @param index The index to add the callback.
     * @param settings The settings used for the callback
     * @param callback The callback that will be executed each time the index is updated.
     * @return An auto-generated id that can be used to remove the callback
     * @throws RuntimeException in case no {@link IndexRefreshListener} was defined for the given index which means that
     *         the index is either system/hidden or does not exist.
     */
    public String addCallback(String index, IndexRefreshCallback.Settings settings, IndexRefreshCallback callback) {
        return getListener(index).addCallback(callback, settings);
    }

    /**
     * Adds a new {@link IndexRefreshCallback} to the given index by using the given {@link IndexRefreshCallback#DEFAULT_SETTINGS}.
     * @param index The index to add the callback.
     * @param callback The callback that will be executed each time the index is updated.
     * @return An auto-generated id that can be used to remove the callback
     * @throws RuntimeException in case no {@link IndexRefreshListener} was defined for the given index which means that
     *         the index is either system/hidden or does not exist.
     */
    public String addCallback(String index, IndexRefreshCallback callback) {
        return addCallback(index, IndexRefreshCallback.DEFAULT_SETTINGS.build(), callback);
    }

    /**
     * Adds a new {@link IndexRefreshCallback} to the given index by using the given {@link IndexRefreshCallback.Settings}.
     * Unlike {@link #addCallback(String, IndexRefreshCallback.Settings, IndexRefreshCallback)} this method will not
     * throw an error in case the {@link IndexRefreshListener} is not already registered (index not created) but will
     * create it.
     *
     * @param index The index to add the callback.
     * @param settings The settings used for the callback
     * @param callback The callback that will be executed each time the index is updated.
     * @return An auto-generated id that can be used to remove the callback.
     */
    public String addCallbackWithNoCheck(String index, IndexRefreshCallback.Settings settings, IndexRefreshCallback callback) {
        registerListener(index);
        return addCallback(index, settings, callback);
    }

    /**
     * Adds a new {@link IndexRefreshCallback} to the given index by using the given {@link IndexRefreshCallback#DEFAULT_SETTINGS}.
     * Unlike {@link #addCallback(String, IndexRefreshCallback)} this method will not throw an error in case the
     * {@link IndexRefreshListener} is not already registered (index not created) but will create it.
     *
     * @param index The index to add the callback.
     * @param callback The callback that will be executed each time the index is updated.
     * @return An auto-generated id that can be used to remove the callback
     */
    public String addCallbackWithNoCheck(String index, IndexRefreshCallback callback) {
        return addCallbackWithNoCheck(index, IndexRefreshCallback.DEFAULT_SETTINGS.build(), callback);
    }

    /**
     * Removes a registered callback from the given index by its id
     * @param index The index to remove the callback from
     * @param callbackId The id of the callback
     * @throws RuntimeException in case no {@link IndexRefreshListener} was defined for the given index which means that
     *         the index is either system/hidden or does not exist.
     */
    public void removeCallback(String index, String callbackId) {
        getListener(index).removeCallback(callbackId);
    }


    /**
     * Checks if there is an {@link IndexRefreshListener} registered for the given index. It will return
     * {@code false} if the index does not exist or it is system/internal.
     * @param index The index to check if it has a registered {@link IndexRefreshListener}.
     * @return true/false depending on whether an {@link IndexRefreshListener} is registered for the index.
     */
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

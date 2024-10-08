package com.itsaur.elasticsearch.index.refresh.core;

import org.apache.lucene.search.ReferenceManager.RefreshListener;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 *     Implements the {@link RefreshListener} and holds a list of {@link IndexRefreshCallback}s that will execute every
 *     time the associated index was refreshed. The callbacks are getting a unique id that can be used to remove them
 * </p>
 */
class IndexRefreshListener implements RefreshListener {

    private final String index;
    private final AtomicLong refreshCount = new AtomicLong(0);
    private final Map<String, CallbackWithSettings> callbacks = new HashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    IndexRefreshListener(String index) {
        this.index = index;
    }

    @Override
    public void beforeRefresh() {

    }

    @Override
    public void afterRefresh(boolean didRefresh) {
        // executing the callbacks to a different thread to not disrupt elasticsearch.
        executorService.execute(() -> {
            handleRefresh(refreshCount.incrementAndGet());
            System.out.println("Total refresh callbacks remained: " + callbacks.size());
        });
        System.out.println("Refresh Scheduled for index '" + index + "'");
    }

    /**
     * Returns the times that the index has been refreshed so far.
     * @return The refresh times number
     */
    long refreshCount() {
        return refreshCount.get();
    }

    /**
     * Adds a new callback to this listener
     * @param callback The callback to execute each time the associated index is refreshed.
     * @return A unique id associated with this callback that can be used to remove it.
     */
    synchronized String addCallback(IndexRefreshCallback callback, IndexRefreshCallback.Settings settings) {
        String id = UUID.randomUUID().toString();
        callbacks.put(id, new CallbackWithSettings(callback, settings));
        return id;
    }

    /**
     * Removes a callback by its unique id.
     * @param id The id of the callback
     */
    synchronized void removeCallback(String id) {
        callbacks.remove(id);
    }

    /**
     * The actual execution of the callbacks. If a callback is ephemeral, it will be removed once executed.
     */
    private synchronized void handleRefresh(long refreshCount) {
        List<String> toRemove = new ArrayList<>();

        callbacks.forEach((id, callback) -> {
            callback.callback().refreshed(refreshCount);
            if (callback.settings().ephemeral()) {
                toRemove.add(id);
            }
        });

        toRemove.forEach(callbacks::remove);
    }
}

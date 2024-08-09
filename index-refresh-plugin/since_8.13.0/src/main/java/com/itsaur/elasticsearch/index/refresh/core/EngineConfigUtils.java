package com.itsaur.elasticsearch.index.refresh.core;

import org.apache.lucene.search.ReferenceManager.RefreshListener;
import org.elasticsearch.index.engine.EngineConfig;
import org.elasticsearch.plugins.EnginePlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class that provides an easy way to override the default {@link EngineConfig} used by {@link EnginePlugin}s
 */
public class EngineConfigUtils {

    /**
     * Creates and returns a clone of the given initial {@link EngineConfig} by adding a new {@link RefreshListener} to it.
     * @param initial The initial {@link EngineConfig} which will be overridden.
     * @param refreshListener The extra {@link RefreshListener} to add to the initial {@link EngineConfig}.
     * @return The new (overridden) {@link EngineConfig}.
     */
    public static EngineConfig addRefreshListener(final EngineConfig initial, final RefreshListener refreshListener) {
        List<RefreshListener> externalRefreshListener = new ArrayList<>(initial.getExternalRefreshListener());
        externalRefreshListener.add(refreshListener);
        return overrideExternalRefreshListeners(initial, List.copyOf(externalRefreshListener));
    }

    private static EngineConfig overrideExternalRefreshListeners(final EngineConfig initial, final List<RefreshListener> externalRefreshListeners) {
        return new EngineConfig(
                initial.getShardId(),
                initial.getThreadPool(),
                initial.getIndexSettings(),
                initial.getWarmer(),
                initial.getStore(),
                initial.getMergePolicy(),
                initial.getAnalyzer(),
                initial.getSimilarity(),
                initial.getCodecService(),
                initial.getEventListener(),
                initial.getQueryCache(),
                initial.getQueryCachingPolicy(),
                initial.getTranslogConfig(),
                initial.getFlushMergesAfter(),
                externalRefreshListeners,
                initial.getInternalRefreshListener(),
                initial.getIndexSort(),
                initial.getCircuitBreakerService(),
                initial.getGlobalCheckpointSupplier(),
                initial.retentionLeasesSupplier(),
                initial.getPrimaryTermSupplier(),
                initial.getSnapshotCommitSupplier(),
                initial.getLeafSorter(),
                initial.getRelativeTimeInNanosSupplier(),
                initial.getIndexCommitListener(),
                initial.isPromotableToPrimary()
        );
    }
}

package com.itsaur.elasticsearch.index.refresh.plugin;

import com.itsaur.elasticsearch.index.refresh.core.EngineConfigUtils;
import com.itsaur.elasticsearch.index.refresh.core.IndexesRefreshManager;
import com.itsaur.elasticsearch.index.refresh.rest.IndexRefreshRestAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.features.NodeFeature;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.engine.EngineConfig;
import org.elasticsearch.index.engine.EngineFactory;
import org.elasticsearch.index.engine.InternalEngineFactory;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.EnginePlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class IndexRefreshPlugin extends Plugin implements EnginePlugin, ActionPlugin {

    private final IndexesRefreshManager refreshManager = new IndexesRefreshManager();

    @Override
    public Collection<RestHandler> getRestHandlers(
            final Settings settings,
            final NamedWriteableRegistry namedWriteableRegistry,
            final RestController restController,
            final ClusterSettings clusterSettings,
            final IndexScopedSettings indexScopedSettings,
            final SettingsFilter settingsFilter,
            final IndexNameExpressionResolver indexNameExpressionResolver,
            final Supplier<DiscoveryNodes> nodesInCluster,
            final Predicate<NodeFeature> clusterSupportsFeature
    ) {
        // Passing the IndexesRefreshManager to the IndexRefreshesRestAction so it can add callbacks and get notified
        // when indexes are refreshed.
        return List.of(new IndexRefreshRestAction(refreshManager));
    }

    @Override
    public Optional<EngineFactory> getEngineFactory(IndexSettings indexSettings) {
        if (indexSettings.getIndexMetadata().isSystem() || indexSettings.getIndexMetadata().isHidden()) {
            return Optional.empty();
        }

        return Optional.of(config -> {
            // Here is where we add our extra RefreshListener when new indexes are created.
            // We override the default EngineConfig by adding a new RefreshListener which is created
            // by the IndexesRefreshManager for the index which is about to be created by elasticsearch.
            EngineConfig newConfig = EngineConfigUtils
                    .addRefreshListener(config, refreshManager.registerListener(indexSettings.getIndex().getName()));

            return new InternalEngineFactory().newReadWriteEngine(newConfig);
        });
    }
}

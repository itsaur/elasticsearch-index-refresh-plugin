package com.itsaur.elasticsearch.index.refresh.plugin;

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
        return List.of(new IndexRefreshesRestAction(refreshManager));
    }

    @Override
    public Optional<EngineFactory> getEngineFactory(IndexSettings indexSettings) {
        if (indexSettings.getIndexMetadata().isSystem() || indexSettings.getIndexMetadata().isHidden()) {
            System.out.println("Ignoring index '" + indexSettings.getIndex().getName() + "'");
            return Optional.empty();
        }

        return Optional.of(config -> {
            IndexRefreshListener indexRefreshListener = refreshManager.registerListener(indexSettings.getIndex().getName());
            // Here is where the actual association of the IndexRefreshListener and the associated index is done.
            EngineConfig newConfig = ConfigUtils.addRefreshListener(config, indexRefreshListener);
            return new InternalEngineFactory().newReadWriteEngine(newConfig);
        });
    }
}

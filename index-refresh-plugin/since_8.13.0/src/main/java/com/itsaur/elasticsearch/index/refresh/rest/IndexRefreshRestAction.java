package com.itsaur.elasticsearch.index.refresh.rest;

import com.itsaur.elasticsearch.index.refresh.core.IndexRefreshCallback;
import com.itsaur.elasticsearch.index.refresh.core.IndexRefreshCallback.Settings;
import com.itsaur.elasticsearch.index.refresh.core.IndexesRefreshManager;
import org.elasticsearch.client.internal.node.NodeClient;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.RestRequest.Method;

import java.util.List;
import java.util.Objects;

public class IndexRefreshRestAction extends BaseRestHandler {

    private static final Settings EPHEMERAL_CALLBACK_SETTING = IndexRefreshCallback.DEFAULT_SETTINGS.withEphemeral(true).build();

    private final IndexesRefreshManager indexesRefreshManager;

    public IndexRefreshRestAction(final IndexesRefreshManager indexesRefreshManager) {
        this.indexesRefreshManager = Objects.requireNonNull(indexesRefreshManager);
    }

    @Override
    public List<Route> routes() {
        return List.of(
                new Route(Method.GET, "/{index}/wait_refresh"),
                new Route(Method.POST, "/{index}/wait_refresh")
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        String index = request.param("index");

        Wait wait = Wait.parseOrDefault(request);
        NoIndexMode noIndexMode = NoIndexMode.parseOrDefault(request);
        LastRefresh lastRefresh = LastRefresh.parseOrDefault(request);

        boolean hasListenerForIndex = indexesRefreshManager.hasListener(index);

        if (!hasListenerForIndex) {
            return switch (noIndexMode) {
                case Fail -> throw new RuntimeException("no listener for index [" + index + "]");
                case Exit -> channel -> sendResponse(channel, 0);
                case Wait -> channel -> indexesRefreshManager
                        .addCallbackWithNoCheck(index, EPHEMERAL_CALLBACK_SETTING, refreshes -> sendResponse(channel, refreshes));
            };
        }

        if (lastRefresh != null && !Objects.equals(lastRefresh.value(), indexesRefreshManager.refreshes(index))) {
            return channel -> sendResponse(channel, indexesRefreshManager.refreshes(index));
        }

        if (wait.value()) {
            return channel -> indexesRefreshManager
                    .addCallback(index, EPHEMERAL_CALLBACK_SETTING, refreshes -> sendResponse(channel, refreshes));
        }

        return channel -> sendResponse(channel, indexesRefreshManager.refreshes(index));
    }

    @Override
    public String getName() {
        return "index_refreshed";
    }

    private void sendResponse(RestChannel channel, long refreshes) {
        channel.sendResponse(new RestResponse(RestStatus.OK, String.valueOf(refreshes)));
    }
}

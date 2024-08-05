package com.itsaur.elasticsearch.index.refresh.plugin;

import org.elasticsearch.client.internal.node.NodeClient;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.RestRequest.Method;

import java.util.List;
import java.util.Objects;

public class IndexRefreshesRestAction extends BaseRestHandler {

    private static final IndexRefreshedCallback.Settings REFRESH_CALLBACK_SETTING =
            IndexRefreshedCallback.DEFAULT_SETTINGS.withEphemeral(true).build();

    private final IndexesRefreshManager indexesRefreshManager;

    public IndexRefreshesRestAction(final IndexesRefreshManager indexesRefreshManager) {
        this.indexesRefreshManager = Objects.requireNonNull(indexesRefreshManager);
    }

    @Override
    public List<Route> routes() {
        return List.of(
                new Route(Method.GET, "/{index}/refreshes"),
                new Route(Method.GET, "/{index}/wait_refresh")
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        String index = request.param("index");

        boolean isWaitRequest = request.path().endsWith("/wait_refresh");
        NoIndexMode noIndexMode = NoIndexMode.parseOrDefault(request);
        boolean hasListenerForIndex = indexesRefreshManager.hasListener(index);

        RestChannelConsumer consumer = null;
        if (!hasListenerForIndex) {
            switch (noIndexMode) {
                case Fail -> consumer = channel -> { throw new RuntimeException("no listener for index [" + index + "]"); };
                case Exit -> consumer = channel -> sendResponse(channel, 0);
                case Wait -> {
                    if (!isWaitRequest) {
                        throw new RuntimeException("'wait' is not supported for /{index}/refreshes ");
                    }
                    throw new RuntimeException("not yet supported");
                }
            }
        } else {
            if (isWaitRequest) {
                consumer = channel -> indexesRefreshManager
                        .addCallback(index, REFRESH_CALLBACK_SETTING, refreshes -> sendResponse(channel, refreshes));
            } else {
                consumer = channel -> sendResponse(channel, indexesRefreshManager.refreshes(index));
            }
        }

        return consumer;
    }

    @Override
    public String getName() {
        return "index_refreshed";
    }

    private void sendResponse(RestChannel channel, long refreshes) {
        channel.sendResponse(new RestResponse(RestStatus.OK, String.valueOf(refreshes)));
    }
}

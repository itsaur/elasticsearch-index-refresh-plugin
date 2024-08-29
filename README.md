# Index-Refresh plugin for Elasticsearch

This plugin extends elasticsearch functionality by adding a way to get informed when an index is refreshed.

Knowing when an index is refreshed is useful because that is the time that previously indexed/updated data are 
available for search.

## Table of Contents
- [Versioning](#versioning)  
- [Installation](#installation)  
- [Http Endpoints](#http-endpoints)  
  - [{index}/wait_refresh](#indexwaitrefresh)

## Versioning

The exact version of the plugin that you have to installed is defined by the version of the plugin + the version
of your elasticsearch instance.

For example if the plugin's version is **1.0** and elasticsearch version is **8.13.0** then the exact plugin's
version is _index-refresh-plugin-1.0_8.13.0_.

## Installation
To install the plugin run the following command:
```
/path/to/elasticsearch/bin/elasticsearch-plugin install https://github.com/itsaur/elasticsearch-index-refresh-plugin/releases/download/1.0/index-refresh-plugin-1.0_8.13.0.zip
```

## Http Endpoints

The plugin add the following http endpoints to elasticsearch:

### `/{index}/wait_refresh`

Responds with the amount of refreshes of the requested `index` (since the latest elasticsearch restart). 
You can either wait for a new refresh or get back the current amount of refreshes. 

##### Http Methods
- GET
- POST

##### Query Parameters

###### wait 
Defines weather you want an immediate response with the current amount  of refresh or wait for a new refresh.

| Values     | Default |
|------------|---------|
| true/false | true    |

###### no_index 
Defines how the request should be handled in case the `index` does not exist (for example it was not yet created
but expected to be).

|          Value | Description                                    |
|---------------:|------------------------------------------------|
|           fail | will return an error (default)                 |
|           exit | will return '0' without error                  |
|           wait | will wait and return once the index is created |

###### last_refresh
Gives the option to provide the last known number of refreshes in the request. If provided, it will be checked against
the current number of refreshes and if the values do not match it will return the current value immediately no matter if
`wait` was requested. 

This is useful if `wait` is requested and the requester is out-of-sync.

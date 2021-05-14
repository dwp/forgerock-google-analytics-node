
# Google Analytics Node

This ForgeRock custom node uses the Google Analytics Measurement Protocol API 
to send events to Google.

Read the [Measurement Protocol documentation](https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters) for further information.

Check these links to [install](./docs/install.md) 
the node or [build](./docs/build.md) it yourself 

## Configuration

The Google Analytics URL must be provided - the URL must contain
the version 1 and the 'tid' account value, for example: 

```
https://www.google-analytics.com/collect?v=1&tid=UA-123456789-1
```
There are three ways to set the URL:

1. Set it with a script in the authentication tree, e.g.
`sharedState.put('googleAnalytics_url','http://...')`
2. Add the URL as a custom parameter in the node (enter `url` as the key)
3. Set an environment variable: `export GOOGLE_ANALYTICS_URL="https://..."`

Note: these are checked in the order above, so setting `sharedState` will
override any other method

To disable the call to Google Analytics, set the URL to `disable`

## Event parameters

These parameters can be entered using the ForgeRock tree editor.

### t - Hit Type

`pageview` - one of:
* pageview
* screenview
* event
* transaction
* item
* social
* exception
* timing

### dp - Document path

`/tidv-login-failed` - A useful parameter as Analytics can group by this field and use it for visualizations.

Enter plain text or a reference to a sharedState variable, ie if you enter `{{myDocumentPath}}`
as the value, the value in `sharedState.myDocumentPath` will be sent to GA.

### ec - Event category

`Category` - max 150 bytes

Enter plain text or a reference to a sharedState variable in `{{brackets}}`.

### ea - Event action

`Action` - max 500 bytes

Enter plain text or a reference to a sharedState variable in `{{brackets}}`.

### el - Event label

`Label` - max 500 bytes

Enter plain text or a reference to a sharedState variable in `{{brackets}}`.

### ev - Event value

`890` - an integer

Enter plain text or a reference to a sharedState variable in `{{brackets}}`.

### Custom parameters

You can add any valid parameter, for example:

`ul = cy` ... sets the user language to Welsh.

`dr = http://example.com/links` ... sets the Document Referrer value

`qt = 63000` ... sets time delta to 62 seconds

See the [Measurement Protocol documentation](https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters) for all values.

The value can be plain text or a reference to a sharedState variable in `{{brackets}}`.


## Common parameters

Most parameters can be preset in JavaScript code by adding a variable to `sharedState` and 
the variable name should be prefixed with `googleAnalytics_`, for example

```
sharedState.put( "googleAnalytics_cid", deviceCookie );
sharedState.put( "googleAnalytics_uid", userGUID );
sharedState.put( "googleAnalytics_cd5", "pip" );
```

#### Exceptions

Some parameters cannot be preset in this manner:

`t, dp, ec, ea, el, ev` - these must be entered on the analytics node using the ForgeRock UI

`v, tid` - these must be included in the `url` 

### cid - Client ID

`35009a79-1a05-49d7-b876-2b884d0f825b`

Anonymously identifies a particular user, device, or browser instance.
Ideally this should be a persistent cookie stored on the **device**

For the telephony channel this can be a hash of the user phone number

If _googleAnalytics_cid_ is not set in _sharedState_, a random cid will be generated for each usage of a tree

### uid - User ID

`as8eknlllhe6254dhsjafghd`

a known identifier for a user - hashed so as not to be PII

### ip - IP Override

`101.94.1.37` - a Shanghai address

By default this is extracted from the request headers, but for testing you can override this 
with sharedState.put('googleAnalytics_ip','x.x.x.x') or a custom parameter.

Analytics uses this to determine the approximate geographic origin of the request.

If geoid is provided then this is ignored

For telephony this will not be a usable value as the request will
be from an API

TODO: how to handle this situation!

### geoid - Geographical Override

`1006964` - the code for Norwich

The geoid can be set with:

`sharedState.put( "googleAnalytics_geoid", '1006964' );`

[See here for further information about the geoid](./docs/geoid.md)

### ua - User Agent

`Mozilla/5.0 (iPhone; CPU iPhone OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Mobile/14D27 Safari/602.1` - an iphone

Google Analytics uses this to determine the type of device in use.

If not set in `sharedState` or a custom parameter, the user-agent header from the request will be used.

### Sending a timestamp to Google Analytics as a custom dimension

You may wish to send a timestamp to GA as part of an event. This can be done as follows:

1. In a script: `sharedState.put("googleAnalytics_cd1", '{{now}}');`
2. By adding a key/value via the ForgeRock UI with `googleAnalytics_cd1` and `{{now}}`

By default this will be send as a UNIX timestamp, e.g. `1609847671574`.

There is also the option to send this in simple date format, i.e. `yyyymmddhhmmss`. To send the timestamp in simple format,
set the following in `sharedState`: `sharedState.put("googleAnalytics_timestampFormat","simple");`

### General Analytics custom dimensions for DTH

PII data (e.g. name, DoB, postcode, phone number) must not be sent to Google Analytics.

Decision scripts can set the event action and label like this:

``` 
sharedState.put( "kbvOutcome", ok ? "pass" : "fail" );
sharedState.put( "kbvLabel", "kbvSortCode|incorrect" );
```

These parameters can then be dereferenced in the node configuration:

Set the Event action value to `{{kbvOutcome}}`.

Set the Event label value to `{{kbvLabel}}`.

### Debugging

By default only errors will be logged. To output the data sent to GA, add the following to `sharedState`: 
`sharedState.put("googleAnalytics_debugLevel", 'all');`. Log files for GA can be found in your ForgeRock AM directory, 
e.g. `/openam/openam/debug/GoogleAnalyticsNode`. 

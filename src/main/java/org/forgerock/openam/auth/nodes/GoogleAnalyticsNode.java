package org.forgerock.openam.auth.nodes;

import com.google.inject.assistedinject.Assisted;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class,
        configClass = GoogleAnalyticsNode.Config.class)

public class GoogleAnalyticsNode extends SingleOutcomeNode {

    public enum HitType {  // must be public or will be inaccessible at run time
        pageview,
        event,
        screenview,
        transaction,
        item,
        social,
        exception,
        timing
    }

    public interface Config {

        @Attribute(order = 1)
        default HitType hitType() {
            return HitType.pageview;
        }

        @Attribute(order = 2)
        default String pageUrl() {
            return "/";
        }

        @Attribute(order = 3, validators = {OptionalValueValidator.class})
        default String eventCategory() {
            return "";
        }

        @Attribute(order = 4, validators = {OptionalValueValidator.class})
        default String eventAction() {
            return "";
        }

        @Attribute(order = 5, validators = {OptionalValueValidator.class})
        default String eventLabel() {
            return "";
        }

        @Attribute(order = 6, validators = {OptionalValueValidator.class})
        default String eventValue() {
            return "";
        }

        @Attribute(order = 7)
        Map<String, String> customParameters();

    }

    static final String PREFIX = "googleAnalytics_";
    static Debug debug = Debug.getInstance("GoogleAnalyticsNode");

    private final GoogleAnalyticsNode.Config config;

    /**
     * @param config Node configuration.
     */
    @Inject
    public GoogleAnalyticsNode(@Assisted GoogleAnalyticsNode.Config config) {
        this.config = config;
    }


    @Override
    public Action process(TreeContext context) {
        sendAnalytics(context);
        return goToNext().build();
    }

    long now() {
        return Instant.now().toEpochMilli();
    }

    /**
     * ga parameters which cannot be added using custom parameters or shared state overrides
     */

    static final Set<String> EXCLUDED = new HashSet<>(Arrays.asList(
            "v", "tid", "cid", "uid", "uip", "guid", "ua", "url", "t", "dp", "ec", "ea", "el", "ev",
            "timestampFormat", "debugLevel"
    ));

    /**
     * get a map of all values in sharedState which start with the "googleAnalytics_" prefix
     */

    Map<String, String> getSharedStateValues(TreeContext context) {
        Map<String, String> out = new HashMap<>();
        Set<String> keys = context.sharedState.keys();
        keys.forEach(key -> {
            if (key.startsWith(PREFIX)) {
                String param = key.substring(PREFIX.length());
                if (EXCLUDED.contains(param)) { // handled separately
                    return;
                }
                if (config.customParameters().containsKey(param)) { // ignore if we have a custom parameter
                    return;
                }
                String value = context.sharedState.get(key).asString();
                if (value.equals("{{now}}")) { // can be simple date time or timestamp
                    String timestampFormat = getTimestampFormat(context);
                    value = getTimestamp(timestampFormat);
                }
                out.put(param, value);
            }
        });
        return out;
    }

    String getTimestamp(String timestampFormat) {
        String value;
        if (timestampFormat.equals("simple")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            java.util.Date date = new java.util.Date(now());
            value = formatter.format(date);
        } else {
            value = String.valueOf(now());
        }
        return value;
    }

    /**
     * value can be a reference to a value in sharedState, ie "{{mySpecialValue}}"
     */

    String checkSharedStateValue(TreeContext context, String value) {
        if (value != null) {
            if (value.equals("{{now}}")) { // can be simple date time or timestamp
                String timestampFormat = getTimestampFormat(context);
                return getTimestamp(timestampFormat);
            } else if (value.startsWith("{{") && value.endsWith("}}")) {
                String key = value.substring(2, value.length() - 2);
                JsonValue check = context.sharedState.get(key);
                if (check != null) {
                    return check.asString();
                }
                return null;
            }
        }
        return value;
    }

    /**
     * get a named parameter from sharedState, or from the set of custom parameters
     */

    private String getPrefixedParam(TreeContext context, String name) {
        String value = context.sharedState.get(PREFIX + name).asString();
        if (value == null || value.length() == 0) {
            value = config.customParameters().get(name);
        }
        return value;
    }

    /**
     * url, example
     * "http://www.google-analytics.com/collect?v=1&tid=UA-123456789-1"
     */

    private String getUrl(TreeContext context) {
        String url = getPrefixedParam(context, "url");
        if (url == null || url.length() == 0) {
            url = System.getenv("GOOGLE_ANALYTICS_URL");
        }
        return url;
    }

    /**
     * set timestamp format. Default to now(), but can be overridden to yyyymmddhhmmss if desired
     */

    private String getTimestampFormat(TreeContext context) {
        String timestampFormat = getPrefixedParam(context, "timestampFormat");
        if (timestampFormat == null || timestampFormat.length() == 0) {
            timestampFormat = "timestamp";
        }
        return timestampFormat;
    }

    /**
     * debug output to log file, useful in development, must be turned off in production
     */

    private String getDebugLevel(TreeContext context) {
        String debugLevel = getPrefixedParam(context, "debugLevel");
        if (debugLevel == null || debugLevel.length() == 0) {
            debugLevel = "off";
        }
        return debugLevel;
    }

    /**
     * uid - user id, example
     * as8eknlllhe6254dhsjafghd
     */

    private String getUserId(TreeContext context) {
        return context.sharedState.get(PREFIX + "uid").asString();
    }

    /**
     * cid - actually the device id, example
     * "35009a79-1a05-49d7-b876-2b884d0f825b"
     */

    String getClientId(TreeContext context) throws Exception {  // cid - device cookie

        String cid = getPrefixedParam(context, "cid");
        if (cid != null && cid.length() > 0) {
            return cid;
        }

        String deviceCookieName = getPrefixedParam(context, "deviceCookieName");
        if (deviceCookieName != null && deviceCookieName.length() > 0) {
            // is there a cookie
            String cookie = context.request.cookies.get(deviceCookieName);
            if (cookie != null && cookie.length() > 0) {
                return cookie;
            }
            // allocate a cookie
            cid = UUID.randomUUID().toString();
            context.sharedState.put(PREFIX + "cid", cid);
            // save on device
            javax.servlet.http.HttpServletResponse response = context.request.servletResponse;
            if (response != null) {
                Cookie C = new Cookie(deviceCookieName, cid);
                C.setMaxAge(60 * 60 * 24 * 365 * 2); // 2 years
                C.setPath("/");
                response.addCookie(C);
            }
        }

        return cid;
    }

    /**
     * ua - example
     * "Mozilla/5.0 (iPhone; CPU iPhone OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Mobile/14D27 Safari/602.1";
     */

    private String getUserAgent(TreeContext context) {  // ua
        String ua = getPrefixedParam(context, "ua");
        if (ua == null || ua.length() == 0) {
            return context.request.headers.get("user-agent").toString();
        }
        return ua;
    }

    private String getGeoId(TreeContext context) {  // geoid
        return getPrefixedParam(context, "geoid");
    }

    /**
     * uip - Google Analytics uses the ip address to determine the user's location
     */

    private String getUserIP(TreeContext context) {
        String uip = getPrefixedParam(context, "uip");
        if (uip == null || uip.length() == 0) {
            uip = context.request.clientIp; // ie 37.215.113.148
            if (uip.equals("0:0:0:0:0:0:0:1") || uip.equals("127.0.0.1")) { // ignore localhost!
                uip = null;
            }
        }
        return uip;
    }

    public String encode(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, "UTF-8");
    }

    void append(StringBuilder path, String param, String value) throws UnsupportedEncodingException {
        if (value != null && value.length() > 0) {
            path.append("&").append(param).append("=").append(encode(value));
        }
    }

    // construct the GA url and send to google (on separate thread)

    private void sendAnalytics(TreeContext context) {
        try {
            String urlStart = getUrl(context);

            if (urlStart == null || urlStart.length() == 0 || urlStart.equals("disable")) {
                return;
            }
            Map<String, String> customValues = getSharedStateValues(context);

            String cid = getClientId(context);
            String uid = getUserId(context);
            String ua = getUserAgent(context);
            String uip = getUserIP(context);
            String geoid = getGeoId(context);

            StringBuilder path = new StringBuilder(urlStart);
            append(path, "uid", uid);
            append(path, "cid", cid);
            append(path, "geoid", geoid);
            append(path, "t", config.hitType().toString());
            append(path, "dp", checkSharedStateValue(context, config.pageUrl()));
            append(path, "ec", checkSharedStateValue(context, config.eventCategory()));
            append(path, "ea", checkSharedStateValue(context, config.eventAction()));
            append(path, "el", checkSharedStateValue(context, config.eventLabel()));
            append(path, "ev", checkSharedStateValue(context, config.eventValue()));
            append(path, "uip", uip);

            // add custom parameters
            for (Map.Entry<String, String> entry : config.customParameters().entrySet()) {
                String key = entry.getKey();
                if (EXCLUDED.contains(key)) {  // these are dealt with above
                    continue;
                }
                customValues.put(key, checkSharedStateValue(context, entry.getValue()));
            }

            // add global overrides from sharedState, ie cd1, ds, and custom parameter values
            Object[] keys = customValues.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                append(path, key.toString(), customValues.get(key.toString()));
            }

            append(path, "ua", ua);  // last parameter, user agent

            String realm = context.sharedState.get("realm").asString();

            sendToGoogle(path.toString(), realm);

            String debugLevel = getDebugLevel(context);
            if (debugLevel.equals("all")) {
                debug.error("GoogleAnalytics: " + path.toString());
            }

        } catch (Exception e) {
            debug.error("EXCEPTION " + e + "\r\n" + Arrays.toString(e.getStackTrace()));
        }

    }

    boolean sendToGoogle(String fullUrl, String realm) {
        new GetURLThread(fullUrl, realm);
        return true;
    }

}

package org.forgerock.openam.auth.nodes;

import com.sun.identity.shared.debug.Debug;
import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.monitoring.api.instrument.DistributionSummary;
import org.forgerock.monitoring.api.instrument.MeterRegistry;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * make the GET request to Google Analytics
 * using a separate thread so as not to block the main thread
 */

public class GetURLThread extends Thread {

    String fullUrl;
    String realm;
    static Debug debug = Debug.getInstance("GoogleAnalyticsNode");

    public GetURLThread(String fullUrl, String realm) {
        this.fullUrl = fullUrl;
        this.realm = realm;
        this.start();
    }

    public void run() {
        try {
            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                // failure, log it - most likely an invalid url
                record(realm + "_google_analytics_error");
                debug.error("GoogleAnalytics: ERROR RESPONSE " + response + " " + fullUrl);
            } else {
                record(realm + "_google_analytics_success");
            }
            conn.disconnect();
        } catch (Exception e) {
            record(realm + "_google_analytics_failed");
            debug.error("GoogleAnalytics: EXCEPTION " + e + "\r\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public void record(String metric_name) {
        MeterRegistry meterRegistry = InjectorHolder.getInstance(MeterRegistry.class);
        DistributionSummary d = DistributionSummary.builder(metric_name)
                .register(meterRegistry);
        d.record();
    }
}

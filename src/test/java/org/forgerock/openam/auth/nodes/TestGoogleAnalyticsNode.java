package org.forgerock.openam.auth.nodes;

import com.google.common.collect.ArrayListMultimap;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.forgerock.openam.auth.nodes.GoogleAnalyticsNode.HitType;
import org.mockito.*;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

public class TestGoogleAnalyticsNode {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    GoogleAnalyticsNode getNode(GoogleAnalyticsNode.Config config) {
        GoogleAnalyticsNode spied = Mockito.spy(new GoogleAnalyticsNode(config));
        Mockito.doReturn(1599651065290L).when(spied).now(); // mock now() to return a known value
        return spied;
    }

    GoogleAnalyticsNode.Config getConfig(HitType type, String pageUrl, String eventCategory, String eventAction, String eventLabel, String[] custom) {
        return new GoogleAnalyticsNode.Config() {

            @Override
            public HitType hitType() {
                return type;
            }

            @Override
            public String pageUrl() {
                return pageUrl;
            }

            @Override
            public String eventCategory() {
                return eventCategory;
            }

            @Override
            public String eventAction() {
                return eventAction;
            }

            @Override
            public String eventLabel() {
                return eventLabel;
            }

            @Override
            public Map<String, String> customParameters() {
                Map<String, String> params = new HashMap<>();
                params.put("url", "http://example.com?v=1");
                if (custom != null) {
                    for (int n = 0; n < custom.length; n += 2) {
                        params.put(custom[n], custom[n + 1]);
                    }
                }
                return params;
            }
        };
    }

    TreeContext getTreeContext(String cid, String[] sharedValues) {
        HashMap<String, String> shared = new HashMap<>();
        shared.put("realm", "test");
        if (cid != null)
            shared.put("googleAnalytics_cid", cid);
        if (sharedValues != null) {
            for (int n = 0; n < sharedValues.length; n += 2) {
                shared.put(sharedValues[n], sharedValues[n + 1]);
            }
        }

        JsonValue sharedState = new JsonValue(shared);

        ArrayListMultimap<String, String> headers = ArrayListMultimap.create();
        headers.put("user-agent", "Safari");

        HashMap<String, String> cookies = new HashMap<>();
        //cookies.put("TRACK","TRACKING-COOKIE");

        ExternalRequestContext request = new ExternalRequestContext.Builder()
                .clientIp("109.42.33.21")
                .headers(headers)
                .cookies(cookies)
                .build();

        List<? extends Callback> callbacks = new ArrayList<>();

        return new TreeContext(sharedState, request, callbacks);
    }

    @Test
    public void testPageView() throws Exception {
        GoogleAnalyticsNode node = getNode(
                getConfig(HitType.pageview, "/login-success", null, null, null, null)
        );
        TreeContext context = getTreeContext("1234", null);

        Mockito.doReturn(true).when(node).sendToGoogle(any(), any());
        node.process(context);

        Mockito.verify(node).sendToGoogle(
                "http://example.com?v=1" +
                        "&cid=1234" +
                        "&t=pageview" +
                        "&dp=%2Flogin-success" +
                        "&uip=109.42.33.21" +
                        "&ua=%5BSafari%5D",
                "test"
        );
    }

    @Test
    public void testEvent() throws Exception {
        GoogleAnalyticsNode node = getNode(
                getConfig(HitType.event, "", "matching", "fail", "cliDob|notMatched", null)
        );
        TreeContext context = getTreeContext("8088-9999", null);

        Mockito.doReturn(true).when(node).sendToGoogle(any(), any());
        node.process(context);

        Mockito.verify(node).sendToGoogle(
                "http://example.com?v=1" +
                        "&cid=8088-9999" +
                        "&t=event" +
                        "&ec=matching" +
                        "&ea=fail" +
                        "&el=cliDob%7CnotMatched" +
                        "&uip=109.42.33.21" +
                        "&ua=%5BSafari%5D",
                "test"
        );
    }

    @Test
    public void testEventWithSharedState() throws Exception {
        String[] sharedValues = {
                "googleAnalytics_cid", "8088-3726-7585",
                "googleAnalytics_cd1", "{{now}}",
                "googleAnalytics_cd2", "8088-3726-7585",
                "googleAnalytics_cd3", "CLI_HASH",
                "googleAnalytics_cd4", "NINO_HASH",
                "googleAnalytics_cd5", "pip",
                "googleAnalytics_cs", "qa",
                "googleAnalytics_ds", "tidv",
                "nextEventCategory", "kbv",
                "kbvMatchAction", "pass",
                "kbvMatchLabel", "kbvSortCode|correct",
        };
        GoogleAnalyticsNode node = getNode(
                getConfig(
                        HitType.event,
                        "/success",
                        "{{nextEventCategory}}",
                        "{{kbvMatchAction}}",
                        "{{kbvMatchLabel}}",
                        null
                )
        );
        TreeContext context = getTreeContext(null, sharedValues);

        Mockito.doReturn(true).when(node).sendToGoogle(any(), any());
        node.process(context);

        Mockito.verify(node).sendToGoogle(
                "http://example.com?v=1" +
                        "&cid=8088-3726-7585" +
                        "&t=event" +
                        "&dp=%2Fsuccess" +
                        "&ec=kbv" +
                        "&ea=pass" +
                        "&el=kbvSortCode%7Ccorrect" +
                        "&uip=109.42.33.21" +
                        "&cd1=1599651065290" +  // mock now value
                        "&cd2=8088-3726-7585" +
                        "&cd3=CLI_HASH" +
                        "&cd4=NINO_HASH" +
                        "&cd5=pip" +
                        "&cs=qa" +
                        "&ds=tidv" +
                        "&ua=%5BSafari%5D",
                "test"
        );
    }

    @Test
    public void testEventWithSharedStateAndCustomOverride() throws Exception {
        String[] sharedValues = {
                "googleAnalytics_cid", "8088-3726-7585",
                "googleAnalytics_cd1", "{{now}}",
                "googleAnalytics_cd2", "8088-3726-7585",
                "googleAnalytics_cd3", "CLI_HASH",
                "googleAnalytics_cd4", "NINO_HASH",
                "googleAnalytics_cd5", "pip",
                "googleAnalytics_cs", "qa",
                "googleAnalytics_ds", "tidv",
                "kbvMatchAction", "pass",
                "kbvMatchLabel", "kbvSortCode|correct",
                "overrideCLI", "123456789",
        };
        String[] custom = {
                "cd3", "{{overrideCLI}}",
                "cd4", "",  // should not be present in output
                "cd6", "NEW_VALUE",
                "debugLevel", "",  // should not be present in output
        };
        GoogleAnalyticsNode node = getNode(
                getConfig(
                        HitType.event,
                        "{{thereIsNoPage}}",  // undefined in sharedState - should not be in output
                        "kbv",
                        "{{kbvMatchAction}}",
                        "{{kbvMatchLabel}}",
                        custom
                )
        );
        TreeContext context = getTreeContext(null, sharedValues);

        Mockito.doReturn(true).when(node).sendToGoogle(any(), any());
        node.process(context);

        Mockito.verify(node).sendToGoogle(
                "http://example.com?v=1" +
                        "&cid=8088-3726-7585" +
                        "&t=event" +
                        "&ec=kbv" +
                        "&ea=pass" +
                        "&el=kbvSortCode%7Ccorrect" +
                        "&uip=109.42.33.21" +
                        "&cd1=1599651065290" +  // mock now value
                        "&cd2=8088-3726-7585" +
                        "&cd3=123456789" +
                        "&cd5=pip" +
                        "&cd6=NEW_VALUE" +
                        "&cs=qa" +
                        "&ds=tidv" +
                        "&ua=%5BSafari%5D",
                "test"
        );
    }

    @Test
    public void testWithSimpleDateFormat() throws Exception {
        String[] sharedValues = {
                "googleAnalytics_cd1", "{{now}}",
                "googleAnalytics_timestampFormat", "simple",
                "googleAnalytics_debugLevel", "off" // should not be in output
        };
        String[] custom = {
                "cd33", "{{now}}"
        };
        GoogleAnalyticsNode node = getNode(
                getConfig(HitType.pageview, "/time-check", null, null, null, custom)
        );
        Mockito.doReturn(1609323320000L).when(node).now(); // 2020 12 30 10 15 20
        TreeContext context = getTreeContext(null, sharedValues);

        Mockito.doReturn(true).when(node).sendToGoogle(any(), any());
        node.process(context);

        Mockito.verify(node).sendToGoogle(
                "http://example.com?v=1" +
                        "&t=pageview" +
                        "&dp=%2Ftime-check" +
                        "&uip=109.42.33.21" +
                        "&cd1=20201230101520" +
                        "&cd33=20201230101520" +
                        "&ua=%5BSafari%5D",
                "test"
        );
    }

    @Test
    public void testSimpleDateFormat() {
        LinkedHashMap<String, Object> sharedState = new LinkedHashMap<>();
        sharedState.put("googleAnalytics_timestampFormat", "simple");
        GoogleAnalyticsNode analytics = getNode(
                getConfig(HitType.pageview, "/login-success", null, null, null, null)
        );
        testSimpleDate(analytics,1646514674000L, "20220305211114"); // 2022-03-05T21:11:14
        testSimpleDate(analytics,1582976759000L, "20200229114559"); // 2020-02-29T11:45:59
        testSimpleDate(analytics,1587202233000L, "20200418093033"); // 2020-04-18T09:30:33
        testSimpleDate(analytics,-464106567000L, "19550418093033"); // 1955-04-18T09:30:33
        testSimpleDate(analytics,1595635199000L, "20200724235959"); // 2020-07-24T23:59:59
        testSimpleDate(analytics,1616892900000L, "20210328005500"); // 2021-03-28T00:55:00
        testSimpleDate(analytics,1616893500000L, "20210328010500"); // 2021-03-28T01:05:00 UK DST started
    }

    void testSimpleDate(GoogleAnalyticsNode analytics, long when, String expect) {
        Mockito.doReturn(when).when(analytics).now();
        String actual1 = analytics.getTimestamp("simple");
        Assert.assertEquals("Date value as expected", expect, actual1);
    }
}

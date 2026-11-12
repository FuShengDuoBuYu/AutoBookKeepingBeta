package com.beta.autobookkeeping.activity.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AiBillOriginTest {
    @Test
    public void buildsExchangeUrlFromHttpsOrigin() {
        AiBillOrigin origin = requireOrigin(" https://Bills.example.test:443/ ");
        assertEquals("https://bills.example.test/api/session/exchange", origin.exchangeUrl());
    }

    @Test
    public void requestsNativeTicketOnTheConfiguredOrigin() {
        AiBillOrigin origin = requireOrigin("https://bills.example.test:8443");
        assertEquals("https://bills.example.test:8443/api/session/ticket", origin.ticketUrl());
    }

    @Test
    public void preservesConfiguredNonDefaultPort() {
        AiBillOrigin origin = requireOrigin("https://bills.example.test:8443");
        assertEquals("https://bills.example.test:8443/api/session/exchange", origin.exchangeUrl());
        assertTrue(origin.allows("https://bills.example.test:8443/history"));
        assertFalse(origin.allows("https://bills.example.test/history"));
    }

    @Test
    public void rejectsInvalidOriginConfigurations() {
        String[] invalid = {
                null,
                "",
                "http://bills.example.test",
                "https:/bills.example.test",
                "https://",
                "https://user@bills.example.test",
                "https://bills.example.test:65536",
                "https://bills.example.test:0",
                "https://bills.example.test/other",
                "https://bills.example.test/?query",
                "https://bills.example.test/#fragment"
        };
        for (String value : invalid) {
            assertNull(value, AiBillOrigin.fromConfig(value));
        }
    }

    @Test
    public void allowsNavigationOnTheConfiguredOrigin() {
        AiBillOrigin origin = requireOrigin("https://bills.example.test");
        assertTrue(origin.allows("https://bills.example.test/"));
        assertTrue(origin.allows("https://BILLS.example.test:443/history?month=11#result"));
    }

    @Test
    public void blocksOtherOriginsAndLocalSchemes() {
        AiBillOrigin origin = requireOrigin("https://bills.example.test");
        String[] blocked = {
                "https://another.example.test/",
                "https://bills.example.test.another.example.test/",
                "https://bills.example.test:8443/",
                "https://other@bills.example.test/",
                "http://bills.example.test/",
                "//bills.example.test/",
                "file:///android_asset/page.html",
                "content://provider/document",
                "javascript:void(0)",
                "about:blank"
        };
        for (String url : blocked) {
            assertFalse(url, origin.allows(url));
        }
    }

    @Test
    public void identifiesTheLandingPageAfterSessionExchange() {
        AiBillOrigin origin = requireOrigin("https://bills.example.test");
        assertTrue(origin.isLandingPage("https://bills.example.test/"));
        assertFalse(origin.isLandingPage("https://bills.example.test/api/session/exchange"));
        assertFalse(origin.isLandingPage("https://bills.example.test/?ticket=unexpected"));
        assertFalse(origin.isLandingPage("https://another.example.test/"));
    }

    private AiBillOrigin requireOrigin(String value) {
        AiBillOrigin origin = AiBillOrigin.fromConfig(value);
        assertNotNull(value, origin);
        return origin;
    }
}

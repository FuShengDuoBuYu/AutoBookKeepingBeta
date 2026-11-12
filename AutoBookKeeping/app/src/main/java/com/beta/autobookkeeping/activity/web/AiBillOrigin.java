package com.beta.autobookkeeping.activity.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

final class AiBillOrigin {
    private static final int HTTPS_PORT = 443;

    private final String host;
    private final int port;

    private AiBillOrigin(URI origin) {
        host = origin.getHost().toLowerCase(Locale.ROOT);
        port = effectivePort(origin);
    }

    static AiBillOrigin fromConfig(String value) {
        URI url = parseHttpsUrl(value == null ? null : value.trim());
        return isOriginRoot(url) ? new AiBillOrigin(url) : null;
    }

    String exchangeUrl() {
        String authority = port == HTTPS_PORT ? host : host + ":" + port;
        return "https://" + authority + "/api/session/exchange";
    }

    boolean allows(String value) {
        return matches(parseHttpsUrl(value));
    }

    boolean isLandingPage(String value) {
        URI url = parseHttpsUrl(value);
        return matches(url) && isOriginRoot(url);
    }

    private boolean matches(URI url) {
        return url != null && host.equalsIgnoreCase(url.getHost()) && port == effectivePort(url);
    }

    private static boolean isOriginRoot(URI url) {
        return url != null
                && (url.getRawPath().isEmpty() || "/".equals(url.getRawPath()))
                && url.getRawQuery() == null
                && url.getRawFragment() == null;
    }

    private static URI parseHttpsUrl(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI url = new URI(value);
            if (!"https".equalsIgnoreCase(url.getScheme()) || url.getHost() == null
                    || url.getRawUserInfo() != null || url.getPort() == 0 || url.getPort() > 65535) {
                return null;
            }
            return url;
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    private static int effectivePort(URI url) {
        return url.getPort() == -1 ? HTTPS_PORT : url.getPort();
    }
}

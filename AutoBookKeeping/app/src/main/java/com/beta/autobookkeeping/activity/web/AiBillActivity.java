package com.beta.autobookkeeping.activity.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.beta.autobookkeeping.BuildConfig;
import com.beta.autobookkeeping.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import Util.SpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AiBillActivity extends AppCompatActivity {
    private static final String EXPIRED_SESSION_COOKIE =
            "__Host-book-agent=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=Strict";
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final OkHttpClient TICKET_CLIENT = new OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(false)
            .retryOnConnectionFailure(false)
            .build();

    private AiBillOrigin origin;
    private WebView webView;
    private ProgressBar progress;
    private AlertDialog passwordDialog;
    private Call ticketCall;
    private boolean awaitingExchange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        origin = AiBillOrigin.fromConfig(BuildConfig.AI_BILL_ORIGIN);
        HttpUrl ticketUrl = HttpUrl.parse(BuildConfig.API_BASE_URL + "/auth/ai-bill/ticket");
        if (origin == null || ticketUrl == null || !ticketUrl.isHttps()) {
            finishWithMessage(R.string.ai_bill_unconfigured);
            return;
        }
        String cachedPhone = (String) SpUtils.get(this, "phoneNum", "");
        if (cachedPhone == null || cachedPhone.trim().isEmpty()) {
            finishWithMessage(R.string.ai_bill_sign_in_first);
            return;
        }

        setContentView(R.layout.activity_ai_bill);
        webView = findViewById(R.id.web_ai_bill);
        progress = findViewById(R.id.ai_bill_progress);
        initWebView();
        promptForPassword(cachedPhone.trim(), ticketUrl);
    }

    private void promptForPassword(String phoneNum, HttpUrl ticketUrl) {
        EditText passwordInput = new EditText(this);
        passwordInput.setHint(R.string.ai_bill_password_hint);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setSaveEnabled(false);
        passwordInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);

        FrameLayout inputContainer = new FrameLayout(this);
        int horizontalPadding = Math.round(24 * getResources().getDisplayMetrics().density);
        inputContainer.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        inputContainer.addView(passwordInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.ai_bill)
                .setMessage(R.string.ai_bill_verify_message)
                .setView(inputContainer)
                .setPositiveButton(R.string.ai_bill_continue, null)
                .setNegativeButton(android.R.string.cancel, (ignored, which) -> finish())
                .create();
        passwordDialog = dialog;
        dialog.setOnCancelListener(ignored -> finish());
        dialog.setOnDismissListener(ignored -> {
            passwordInput.getText().clear();
            passwordDialog = null;
        });
        passwordInput.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                ignored -> requestTicket(phoneNum, ticketUrl, dialog, passwordInput));
    }

    private void requestTicket(String phoneNum, HttpUrl ticketUrl, AlertDialog dialog, EditText input) {
        String password = input.getText().toString();
        if (password.isEmpty()) {
            input.setError(getString(R.string.ai_bill_password_required));
            return;
        }
        input.getText().clear();
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("phoneNum", phoneNum);
            credentials.put("password", password);
        } catch (JSONException ignored) {
            dialog.setMessage(getString(R.string.ai_bill_auth_failed));
            return;
        }
        Request request = new Request.Builder()
                .url(ticketUrl)
                .post(RequestBody.create(credentials.toString(), JSON))
                .build();

        input.setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.setMessage(getString(R.string.ai_bill_verifying));
        ticketCall = TICKET_CLIENT.newCall(request);
        ticketCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException error) {
                deliverResult(call, null, R.string.ai_bill_auth_failed);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response ticketResponse = response) {
                    if (ticketResponse.code() == 401 || ticketResponse.code() == 403) {
                        deliverResult(call, null, R.string.ai_bill_auth_rejected);
                        return;
                    }
                    ResponseBody body = ticketResponse.body();
                    if (ticketResponse.isSuccessful() && body != null) {
                        Object value = new JSONObject(body.string()).opt("ticket");
                        if (value instanceof String && !((String) value).isEmpty()) {
                            deliverResult(call, (String) value, 0);
                            return;
                        }
                    }
                } catch (IOException | JSONException ignored) {
                    // Show a retryable error for an interrupted or invalid response.
                }
                deliverResult(call, null, R.string.ai_bill_auth_failed);
            }

            private void deliverResult(Call call, String ticket, @StringRes int errorMessage) {
                runOnUiThread(() -> {
                    if (call != ticketCall || isFinishing() || isDestroyed()) {
                        return;
                    }
                    ticketCall = null;
                    if (ticket == null) {
                        dialog.setMessage(getString(errorMessage));
                        input.setEnabled(true);
                        input.requestFocus();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        return;
                    }
                    dialog.dismiss();
                    exchangeTicket(ticket);
                });
            }
        });
    }

    private void exchangeTicket(String ticket) {
        awaitingExchange = true;
        progress.setVisibility(View.VISIBLE);
        String url = origin.exchangeUrl();
        byte[] formBody = ("ticket=" + Uri.encode(ticket)).getBytes(StandardCharsets.UTF_8);
        CookieManager.getInstance().setCookie(url, EXPIRED_SESSION_COOKIE, expired -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (!Boolean.TRUE.equals(expired)) {
                finishWithMessage(R.string.ai_bill_open_failed);
                return;
            }
            webView.postUrl(url, formBody);
        });
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                boolean block = request.isForMainFrame() && !origin.allows(request.getUrl().toString());
                if (block && awaitingExchange) {
                    finishWithMessage(R.string.ai_bill_open_failed);
                }
                return block;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // The navigation callback is not invoked for POST requests.
                if (request.isForMainFrame() && !origin.allows(request.getUrl().toString())) {
                    return new WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden",
                            Collections.emptyMap(), null);
                }
                return null;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!origin.allows(url)) {
                    view.stopLoading();
                    finishWithMessage(R.string.ai_bill_open_failed);
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                if (!awaitingExchange || isFinishing()) {
                    return;
                }
                if (!origin.isLandingPage(url)) {
                    finishWithMessage(R.string.ai_bill_open_failed);
                    return;
                }
                awaitingExchange = false;
                view.clearHistory();
                view.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (awaitingExchange && request.isForMainFrame()) {
                    finishWithMessage(R.string.ai_bill_open_failed);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse error) {
                if (awaitingExchange && request.isForMainFrame()) {
                    finishWithMessage(R.string.ai_bill_open_failed);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                if (awaitingExchange && origin.allows(error.getUrl())) {
                    finishWithMessage(R.string.ai_bill_open_failed);
                }
            }
        });
    }

    private void finishWithMessage(@StringRes int message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && !awaitingExchange && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (ticketCall != null) {
            ticketCall.cancel();
            ticketCall = null;
        }
        if (passwordDialog != null) {
            passwordDialog.dismiss();
        }
        if (webView != null) {
            webView.stopLoading();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}

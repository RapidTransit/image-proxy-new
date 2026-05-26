package com.pss.proxy.image;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DebugRequestTest {

    public static final String URL = "https://pleasant-smoke.sirv.com/p/Fuente-Aged/Opus-X-Lost-City/PRO-LOS-DouRob-T_S.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcmdzIjp7InciOjEwMDAsInRodW1ibmFpbCI6MTAwMCwicHJvZmlsZSI6InBzLWwifSwiaWF0IjoxNzY5MDA5MDc0LCJleHAiOjE3ODQ1NjEwNzQsImF1ZCI6Ii9wLyJ9.On9fGm1izSI3LLjSfk95lrPWmTonXFs9p42vJuUfcS4";
    public static final String UURL = "https://pleasant-smoke.sirv.com/u/CRA/CRA-Sampler-2025-Winter.jpg?tr=n-primary-m_l";
    @Test
    public void testDebugRequest() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        CloseableHttpAsyncClient build = HttpAsyncClients.createHttp2Minimal();
        var request = SimpleHttpRequest.create("get", UURL);
        request.setHeaders( new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                new BasicHeader("Accept-Language", "en-US,en;q=0.5"),
                new BasicHeader("Accept-Encoding", "gzip, deflate, br"));
        Future<SimpleHttpResponse> execute = build.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse result) {

            }

            @Override
            public void failed(Exception ex) {
                System.out.println(ex.getMessage());
            }

            @Override
            public void cancelled() {
                throw new RuntimeException("Cancelled");
            }
        });

        SimpleHttpResponse simpleHttpResponse = execute.get(10, TimeUnit.SECONDS);

    }

}

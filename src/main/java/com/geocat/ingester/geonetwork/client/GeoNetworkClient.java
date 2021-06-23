package com.geocat.ingester.geonetwork.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeoNetworkClient {

    @Autowired
    private GNLogin login;

    private GNConnection connection;

    @Value("${geonetwork.baseUrl}")
    private String baseUrl;

    @PostConstruct
    public void init() throws Exception {
        connection = new GNConnection();
        login.login(connection);
    }

    public void index(List<String> uuids) throws IOException {
        String url = baseUrl + "/srv/api/records/index?uuids=" +
                uuids.stream()
                        .map(u -> encodeParam(u))
                        .collect(Collectors.joining(","));

        doGet(connection, url, "application/json");
    }

    protected String doGet(GNConnection connection, String url, String acceptHeader) throws IOException {
        Cookie jsessionidCookie = connection.getJsessionidCookie();
        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", acceptHeader);
        if (jsessionidCookie != null) {
            httpGet.setHeader("Cookie", jsessionidCookie.getName()+"="+jsessionidCookie.getValue());
        }

        CloseableHttpResponse response = null;
        String responseBody = "";

        try {
            response = closeableHttpClient.execute(httpGet, connection.getHttpClientContext());

            checkSuccessStatusCode(response);

            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);

        } catch (ClientProtocolException e) {
            // log
        } catch (IOException e) {
            // log
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return responseBody;
    }


    protected String doPost(GNConnection connection, String url, String body, String acceptHeader, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        return sendRequest(connection, httpPost, body, acceptHeader, contentType);
    }


    protected String doPut(GNConnection connection, String url, String body, String acceptHeader, String contentType) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        return sendRequest(connection, httpPut, body, acceptHeader, contentType);
    }

    protected String doDelete(GNConnection connection, String url, String body, String acceptHeader, String contentType) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        configureHttpRequestHeaders(httpDelete, connection, acceptHeader, contentType);

        HttpClientContext clientContext = connection.getHttpClientContext();

        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();
        CloseableHttpResponse response = null;

        String responseBody = "";

        try {
            response = closeableHttpClient.execute(httpDelete, clientContext);
            checkSuccessStatusCode(response);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }

        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return responseBody;
    }

    private String sendRequest(GNConnection connection, HttpEntityEnclosingRequestBase httpMethod, String body, String acceptHeader, String contentType) throws IOException {
        configureHttpRequestHeaders(httpMethod, connection, acceptHeader, contentType);

        HttpClientContext clientContext = connection.getHttpClientContext();

        HttpEntity entity;

        String responseBody = "";

        try {
            entity = new ByteArrayEntity(body.getBytes("UTF-8"));
            httpMethod.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            // log
        }

        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();
        CloseableHttpResponse response = null;

        try {
            response = closeableHttpClient.execute(httpMethod, clientContext);
            checkSuccessStatusCode(response);

            entity = response.getEntity();

            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }

        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return responseBody;
    }


    private void configureHttpRequestHeaders(HttpRequestBase request, GNConnection connection,
                                             String acceptHeader, String contentType) {
        Cookie crsfTokenCookie = connection.getCrsfTokenCookie();

        if (!StringUtils.isEmpty(acceptHeader)) {
            request.addHeader("Accept", acceptHeader);
        }

        //httpPost.addHeader("Cookie", jsessionidCookie.getName()+"="+jsessionidCookie.getValue());
        if (crsfTokenCookie != null) {
            request.addHeader("X-XSRF-TOKEN", crsfTokenCookie.getValue());
        }

        if (!StringUtils.isEmpty(contentType)) {
            request.addHeader("Content-type", contentType);
        }

    }

    private void checkSuccessStatusCode(HttpResponse response) throws IOException {
        if (response == null || !(response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK &&
                response.getStatusLine().getStatusCode() <= 299)) {

            throw new IOException("Http response error (" + response.getStatusLine().getStatusCode() +
                    "): " + response.getStatusLine().getReasonPhrase());
        }
    }

    private String encodeParam(String paramValue) {
        try {
            return URLEncoder.encode(paramValue, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return paramValue;
        }
    }
}

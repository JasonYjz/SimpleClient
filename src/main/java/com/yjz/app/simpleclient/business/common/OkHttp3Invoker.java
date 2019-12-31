package com.yjz.app.simpleclient.business.common;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Created by jasyu on 2019/12/30.
 **/
@Component
public class OkHttp3Invoker {
    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttp3Invoker.class);

    private boolean trustAll = false;
    private File certLocation;

    protected OkHttpClient client;
    @Value("${maxIdleConnections:5}")
    private int maxIdleConnections;

    @Value("${keepAliveDuration:1800}")
    private long keepAliveDuration;

    @Value("${connectTimeoutDuration:20}")
    private long connectTimeoutDuration;

//    @Value("${proxyIP}")
//    private String proxyIP;
//
//    @Value("${proxyPort}")
//    private int proxyPort;

    @PostConstruct
    public void init() {
        //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www.abc.com", 8080)))
        //                .addInterceptor(interceptor)
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(connectTimeoutDuration, TimeUnit.SECONDS)
                .connectionPool(pool())
                .hostnameVerifier((hostname, session) -> true)
                .connectionSpecs(Arrays.asList(
                        ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS
                ))
                .sslSocketFactory(createSslSocketFactory())
//                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, proxyPort))
                .build();

//        if (!StringUtils.isEmpty(proxyIP) && !StringUtils.isEmpty(proxyPort)) {
//            client.(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, proxyPort)));
//        }

        LOGGER.info("Init OkHttpClient successfully.");
    }

    private SSLSocketFactory createSslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null, trustAll ? new TrustManager[]{createX509TrustAllManager()}
                            : creatX509TrustManager(), new SecureRandom()
            );
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error("xxxxxxxxxx");
            return null;
        }
    }

    //如果创建失败，那么直接会退到不验证服务器证书逻辑，也就是接受所有证书
    private TrustManager[] creatX509TrustManager() {
        if (certLocation != null && certLocation.isFile()) {
            try (FileInputStream inputStream = new FileInputStream(certLocation)) {
                CertificateFactory certfactory = CertificateFactory.getInstance("X.509");
                Certificate certificate = certfactory.generateCertificate(inputStream);

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("certificate", certificate);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm()
                );
                trustManagerFactory.init(keyStore);

                return trustManagerFactory.getTrustManagers();
            } catch (Exception e) {
                LOGGER.error("Can not load cert from path " + certLocation + " ,so trust all certificate", e);
                return new TrustManager[]{createX509TrustAllManager()};
            }
        } else {
            return new TrustManager[]{createX509TrustAllManager()};
        }
    }

    private X509TrustManager createX509TrustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private ConnectionPool pool() {
        return new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
    }

    public void Invoke(String url, String username, String password) {
        RequestBody body = new FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .build();
//client.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port)));
        Request request = new Request.Builder()
                            .url(HttpUrl.get(url))
                            .header(HttpHeaders.AUTHORIZATION, constructAuthValue(username, password))
                            .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                            .post(body)
                            .build();
        System.out.println(request);
        for (int i = 0; i < 3; i++) {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    LOGGER.info("All events has been pushed for NMS");
                    System.out.println(response.body().string());
                    break;
                } else {
                    LOGGER.warn("Response status {} is not successfully, retry again", response.code());
                    sleepAwait(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sleepAwait(long second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("UnExcepted Interrupt exception happen", e);
        }
    }

    private String constructAuthValue(String username, String password) {
        //base64加密
        String value = username + ":" + password;
        byte[] auth = Base64.getEncoder().encode(value.getBytes());
        value = "Basic " + new String(auth);

        System.out.println(value);
        return value;
    }
}

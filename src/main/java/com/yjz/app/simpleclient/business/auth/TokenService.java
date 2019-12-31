package com.yjz.app.simpleclient.business.auth;

import com.yjz.app.simpleclient.business.common.OkHttp3Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by jasyu on 2019/12/31.
 **/
@Service
public class TokenService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private OkHttp3Invoker okHttp3Invoker;

    public void applyToken(String hostname, String username, String password) {
        okHttp3Invoker.Invoke(hostname, username, password);
    }
}

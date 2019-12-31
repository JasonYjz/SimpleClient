package com.yjz.app.simpleclient.controller;

import com.yjz.app.simpleclient.business.auth.TokenService;
import de.felixroske.jfxsupport.FXMLController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by jasyu on 2019/12/30.
 **/
@FXMLController
public class MainStageController implements Initializable {
    @Autowired
    private TokenService tokenService;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void btnApplyTokenOnClick(final Event e) {
        tokenService.applyToken("https://10.32.222.185:9527/restda/oauth2/token", "omc", "omc");
        System.out.println("Clicked a button");
    }
}

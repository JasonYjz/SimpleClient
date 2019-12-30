package com.yjz.app.simpleclient;

import com.yjz.app.simpleclient.view.MainStageView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        launch(MainApplication.class, MainStageView.class, args);
    }

}

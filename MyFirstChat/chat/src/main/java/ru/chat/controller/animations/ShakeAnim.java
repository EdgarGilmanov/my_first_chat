package ru.chat.controller.animations;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class ShakeAnim {
    private TranslateTransition tt;

    public ShakeAnim(Node node){
        tt = new TranslateTransition(Duration.millis(80),node);
        tt.setFromX(-10f);
        tt.setByX(10f);
        tt.setCycleCount(3);
        tt.setAutoReverse(true);
    }
    public void play(){
        tt.playFromStart();
    }
}

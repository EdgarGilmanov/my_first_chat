package ru.chat.controller;

import javafx.scene.Node;
import ru.chat.Launcher;


public class BaseController implements Controller {
    private Node view;

    @Override
    public void setView (Node view){
        this.view = view;
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void show() {
        preShowing();
        Launcher.getNavigation().show(this);
        postShowing();
    }

    public void preShowing() {

    }

    public void postShowing(){

    }
}
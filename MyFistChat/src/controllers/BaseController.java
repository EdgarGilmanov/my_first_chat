package controllers;

import javafx.scene.Node;
import sample.Main;


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
        Main.getNavigation().show(this);
        postShowing();
    }

    public void preShowing() {

    }

    public void postShowing(){

    }
}
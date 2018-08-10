package com.atsisa.gox.games.trextrack.screen.screensaver;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.view.KeyframeAnimationView;
import com.atsisa.gox.framework.view.ViewGroup;

public class Scene1 {
    ViewGroup viewGroup = GameEngine.current().getViewManager().findViewById("screenSaver", "scene1");

    public static Scene1 scene1;

    private Scene1() {
    }

    public static Scene1 getInstance() {
        if (scene1 == null) {
            scene1 = new Scene1();
        }
        return scene1;
    }

    public void start() {
        viewGroup.setVisible(true);

        for(int i=1; i<=6; i++){
            GameEngine.current().getViewManager().findViewById("screenSaver","scene1"+i).setVisible(true);

                ((KeyframeAnimationView)GameEngine.current().getViewManager().findViewById("screenSaver","keyfr1"+i)).setVisible(true);
                ((KeyframeAnimationView)GameEngine.current().getViewManager().findViewById("screenSaver","keyfr1"+i)).play();

        }
        new Timeout(16000,()->{
            resetScene();
        },true);
        playScene();
    }

    void resetScene() {
        for(int i=1; i<=6; i++){
            GameEngine.current().getViewManager().findViewById("screenSaver","scene1"+i).setVisible(false);

                ((KeyframeAnimationView)GameEngine.current().getViewManager().findViewById("screenSaver","keyfr1"+i)).setVisible(false);
                ((KeyframeAnimationView)GameEngine.current().getViewManager().findViewById("screenSaver","keyfr1"+i)).stop();

        }
        viewGroup.setVisible(false);
    }

    void playScene() {

    }

}

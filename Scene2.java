package com.atsisa.gox.games.trextrack.screen.screensaver;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.animation.TweenViewAnimation;
import com.atsisa.gox.framework.animation.TweenViewAnimationData;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.utility.TimeoutCallback;
import com.atsisa.gox.framework.view.KeyframeAnimationView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.framework.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


class Scene2 {
    private static int symbolId = 0;
    public static Scene2 scene2;
    private static List<ViewGroup> viewGroups = new ArrayList<>();
    private TweenViewAnimation scene2TweenViewAnimation;
    private TweenViewAnimationData scene2TweenViewAnimationData;
    private Timeout scene2Time;
    private Timeout playNextScene;

    private Scene2() {

    }

    public static Scene2 getInstance() {
        if (scene2 == null) {
            scene2 = new Scene2();
            viewGroups.add(GameEngine.current().getViewManager().findViewById("screenSaver", "violet"));
            viewGroups.add(GameEngine.current().getViewManager().findViewById("screenSaver", "pink"));
            viewGroups.add(GameEngine.current().getViewManager().findViewById("screenSaver", "green"));
            viewGroups.add(GameEngine.current().getViewManager().findViewById("screenSaver", "yellow"));
            viewGroups.add(GameEngine.current().getViewManager().findViewById("screenSaver", "blue"));
        }
        symbolId = 0;
        return scene2;
    }

    public void start() {
        resetScene();
        playScene();
    }

    void playScene() {

        for (int i = 1; i <= 5; i++) {
            View textView = GameEngine.current().getViewManager().findViewById("screenSaver", "track4" + i);
            if (!textView.isVisible()) {
                textView.setVisible(true);
            }
        }

        viewGroups.get(symbolId).setVisible(true);
        viewGroups.get(symbolId).getChildren().forEach(view -> {
//            view.setVisible(true);
            if (view instanceof KeyframeAnimationView) {
                KeyframeAnimationView symbolInThisGroup = ((KeyframeAnimationView) view);
                symbolInThisGroup.setLoop(true);
                if (!symbolInThisGroup.isPlaying()) {
                    symbolInThisGroup.play();
                }
            }
        });

        scene2TweenViewAnimation = GameEngine.current().getAnimationFactory().createAnimation(TweenViewAnimation.class);
        scene2TweenViewAnimationData = new TweenViewAnimationData();
        scene2TweenViewAnimationData.setTimeSpan(200);
        scene2TweenViewAnimationData.setDestinationAlpha(1f);
        scene2TweenViewAnimation.setTargetView(viewGroups.get(symbolId));
        scene2TweenViewAnimation.setViewAnimationData(scene2TweenViewAnimationData);
        scene2TweenViewAnimation.play();
        scene2Time = new Timeout(4000, new Scene2TimeOut(viewGroups.get(symbolId)), true);
        if (symbolId < 4) {
            symbolId++;

            playNextScene = new Timeout(4000, new TimeoutCallback() {
                @Override
                public void onTimeout() {
                    playScene();
                }
            }, true);

        }
    }

    public static class Scene2TimeOut implements TimeoutCallback {
        ViewGroup viewGroupTime;

        public Scene2TimeOut(ViewGroup viewGroup) {
            this.viewGroupTime = viewGroup;
        }

        @Override
        public void onTimeout() {
            TweenViewAnimation moveGroup = GameEngine.current().getAnimationFactory().createAnimation(TweenViewAnimation.class);
            TweenViewAnimationData moveData = new TweenViewAnimationData();
            moveData.setDestinationAlpha(0f);
            moveData.setDestinationScaleX(1.3f);
            moveData.setDestinationScaleY(1.3f);
            moveData.setDestinationX(960f);
            moveData.setDestinationY(600f);
            moveData.setTimeSpan(500f);
            moveGroup.setViewAnimationData(moveData);
            moveGroup.setTargetView(viewGroupTime);
            if (!moveGroup.isPlaying()) {
                moveGroup.play();
            }
        }
    }

    void resetScene() {
        for (int i = 1; i <= 5; i++) {
            View textView = GameEngine.current().getViewManager().findViewById("screenSaver", "track4" + i);
            if (textView.isVisible()) {
                textView.setVisible(false);
            }
        }

        if (scene2Time != null) {
            scene2Time.clear();
        }
        if (playNextScene != null) {
            playNextScene.clear();
        }
        if (scene2TweenViewAnimation != null) {
            if (scene2TweenViewAnimation.isPlaying()) {
                scene2TweenViewAnimation.stop();
            }
        }

        for (ViewGroup group : viewGroups) {
            group.setVisible(false);
            group.setScaleX(0.8f);
            group.setScaleY(0.8f);
            group.setX(192);
            group.setY(250);
            group.setAlpha(1);
            group.getChildren().forEach(view -> {
                if (view instanceof KeyframeAnimationView) {
                    KeyframeAnimationView symbolInThisGroup = ((KeyframeAnimationView) view);
                    symbolInThisGroup.setLoop(false);
                    if (symbolInThisGroup.isPlaying()) {
                        symbolInThisGroup.stop();
                    }
                }
            });
        }
    }

}
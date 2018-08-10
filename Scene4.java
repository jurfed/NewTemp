package com.atsisa.gox.games.trextrack.screen.screensaver;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.eventbus.NextObserver;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.utility.TimeoutCallback;
import com.atsisa.gox.framework.view.*;
import com.atsisa.gox.reels.animation.ReelAnimation;
import com.atsisa.gox.reels.view.AbstractReel;
import com.atsisa.gox.reels.view.ReelGroupView;
import com.atsisa.gox.reels.view.state.ReelState;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * for playing reels with TRax symbols
 */
public class Scene4 {
    public static Scene4 scene4;
    private static AbstractReel reel0;
    private static AbstractReel reel1;
    private static AbstractReel reel2;
    private static AbstractReel reel3;
    private static AbstractReel reel4;
    private static List<AbstractReel> reels = new ArrayList<>();
    private static StopTrackKeyframeListener stopTrackKeyframeListener;
    private static boolean finishKeyFrameAnimation = false;
    private static Timeout playNextSceneTimeout;

    /**
     * count for playing key frames animation scene number - 1,2,3,4,5,6
     */
    private static int sceneNumber = 0;

    /**
     * when reel state = IDLE then playing key frame animation
     */
    private static Observable<ReelState> reelStateObservable;
    private static KeyframeAnimationView kReel0;
    private static KeyframeAnimationView kReel1;
    private static KeyframeAnimationView kReel2;
    private static KeyframeAnimationView kReel3;
    private static KeyframeAnimationView kReel4;

    private static Timeout reelTimeout0;
    private static Timeout reelTimeout1;
    private static Timeout reelTimeout2;
    private static Timeout reelTimeout3;
    private static Timeout reelTimeout4;
    private static boolean activeScene4 = false;

    /**
     * List of symbols under the track animations for ech scene
     */
    private static List<ViewGroup> symbolsForSteps = new ArrayList<>();

    /**
     * <Reel number and scene number>,<symbols for this reel and his scene>
     */
    private static Map<String, List<String>> symbols = new HashMap<>();

    /**
     * <Reel number and scene number>, <TrackAnimationID>
     */
    private static Map<String, String> trackAnimation = new HashMap<>();

    private Scene4() {
    }

    public static Scene4 getInstance() {
        if (scene4 == null) {
            scene4 = new Scene4();
            reel0 = ((ReelGroupView) GameEngine.current().getViewManager().findViewById("screenSaver", "reelGroupView")).getReel(0);
            reels.add(reel0);
            reel1 = ((ReelGroupView) GameEngine.current().getViewManager().findViewById("screenSaver", "reelGroupView")).getReel(1);
            reels.add(reel1);
            reel2 = ((ReelGroupView) GameEngine.current().getViewManager().findViewById("screenSaver", "reelGroupView")).getReel(2);
            reels.add(reel2);
            reel3 = ((ReelGroupView) GameEngine.current().getViewManager().findViewById("screenSaver", "reelGroupView")).getReel(3);
            reels.add(reel3);
            reel4 = ((ReelGroupView) GameEngine.current().getViewManager().findViewById("screenSaver", "reelGroupView")).getReel(4);
            reels.add(reel4);

            for (int i = 0; i < 6; i++) {
                symbolsForSteps.add(GameEngine.current().getViewManager().findViewById("screenSaver", "symbolForStep" + i));
            }

            symbols.put("reel00", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel10", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel20", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel30", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel40", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});

            symbols.put("reel01", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel11", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel21", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel31", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel41", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});

            symbols.put("reel02", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel12", new ArrayList() {{
                add("Green");
                add("Green");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel22", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel32", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel42", new ArrayList() {{
                add("Green");
                add("Green");
                add("Track");
                add("TrackR");
            }});

            symbols.put("reel03", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel13", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel23", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Green");
                add("Green");
            }});
            symbols.put("reel33", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel43", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});

            symbols.put("reel04", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel14", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel24", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel34", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});
            symbols.put("reel44", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});

            symbols.put("reel05", new ArrayList() {{
                add("Green");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel15", new ArrayList() {{
                add("Track");
                add("TrackR");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel25", new ArrayList() {{
                add("Green");
                add("Green");
                add("Track");
                add("TrackR");
            }});
            symbols.put("reel35", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Track");
            }});
            symbols.put("reel45", new ArrayList() {{
                add("Green");
                add("Green");
                add("Green");
                add("Green");
            }});

            //<Reel number and scene number>, <TrackAnimationID>
            //for first scene
            trackAnimation.put("reel00", "TrackAnimation00");
            trackAnimation.put("reel20", "TrackAnimation20");
            trackAnimation.put("reel40", "TrackAnimation40");

            //for second scene
            trackAnimation.put("reel01", "TrackAnimation01");
            trackAnimation.put("reel11", "TrackAnimation11");
            trackAnimation.put("reel21", "TrackAnimation21");
            trackAnimation.put("reel31", "TrackAnimation31");
            trackAnimation.put("reel41", "TrackAnimation41");

            //for third scene
            trackAnimation.put("reel02", "TrackAnimation02");
            trackAnimation.put("reel12", "TrackAnimation12");
            trackAnimation.put("reel22", "TrackAnimation22");
            trackAnimation.put("reel42", "TrackAnimation42");

            //for fourth scene
            trackAnimation.put("reel03", "TrackAnimation03");
            trackAnimation.put("reel13", "TrackAnimation13");
            trackAnimation.put("reel23", "TrackAnimation23");

            //for fifth scene
            trackAnimation.put("reel04", "TrackAnimation04");
            trackAnimation.put("reel44", "TrackAnimation44");

            //for sixth scene
            trackAnimation.put("reel05", "TrackAnimation05");
            trackAnimation.put("reel15", "TrackAnimation15");
            trackAnimation.put("reel25", "TrackAnimation25");
            trackAnimation.put("reel35", "TrackAnimation35");

            reelStateObservable = reel4.getReelStateObservable();
            reelStateObservable.subscribe(new ReelsNextObserver());
            stopTrackKeyframeListener = new StopTrackKeyframeListener();
        }
        return scene4;
    }

    public void start() {
        activeScene4 = true;
        sceneNumber = 0;
        resetSymbolsOnTheReels();
        reel0.setVisible(true);
        reel1.setVisible(true);
        reel2.setVisible(true);
        reel3.setVisible(true);
        reel4.setVisible(true);

        for (int i = 1; i <= 5; i++) {
            View view = GameEngine.current().getViewManager().findViewById("screenSaver", "track4"+i);
            if(!view.isVisible()){
                view.setVisible(true);
            }
        }

        //resetScene();
        playScene();
    }

    private static void playScene() {
        reelTimeout0 = new Timeout(100, new Reel0TimeOut(), true);
        reelTimeout1 = new Timeout(400, new Reel1TimeOut(), true);
        reelTimeout2 = new Timeout(700, new Reel2TimeOut(), true);
        reelTimeout3 = new Timeout(1000, new Reel3TimeOut(), true);
        reelTimeout4 = new Timeout(1300, new Reel4TimeOut(), true);

    }

    public static class Reel0TimeOut implements TimeoutCallback {

        @Override
        public void onTimeout() {
            reel0.spin();
            ((ReelAnimation) reel0.getReelAnimation()).setSpeedIncrease(0.6f);
            ((ReelAnimation) reel0.getReelAnimation()).speedUp();
            reel0.stopOnSymbols(symbols.get("reel0" + sceneNumber));
        }
    }

    public static class Reel1TimeOut implements TimeoutCallback {

        @Override
        public void onTimeout() {
            reel1.spin();
            ((ReelAnimation) reel1.getReelAnimation()).setSpeedIncrease(0.6f);
            ((ReelAnimation) reel1.getReelAnimation()).speedUp();
            reel1.stopOnSymbols(symbols.get("reel1" + sceneNumber));
        }
    }

    public static class Reel2TimeOut implements TimeoutCallback {

        @Override
        public void onTimeout() {
            reel2.spin();
            ((ReelAnimation) reel2.getReelAnimation()).setSpeedIncrease(0.6f);
            ((ReelAnimation) reel2.getReelAnimation()).speedUp();
            reel2.stopOnSymbols(symbols.get("reel2" + sceneNumber));
        }
    }

    public static class Reel3TimeOut implements TimeoutCallback {

        @Override
        public void onTimeout() {
            reel3.spin();
            ((ReelAnimation) reel3.getReelAnimation()).setSpeedIncrease(0.6f);
            ((ReelAnimation) reel3.getReelAnimation()).speedUp();
            reel3.stopOnSymbols(symbols.get("reel3" + sceneNumber));
        }
    }

    public static class Reel4TimeOut implements TimeoutCallback {

        @Override
        public void onTimeout() {
            reel4.spin();
            ((ReelAnimation) reel4.getReelAnimation()).setSpeedIncrease(0.6f);
            ((ReelAnimation) reel4.getReelAnimation()).speedUp();
            reel4.stopOnSymbols(symbols.get("reel4" + sceneNumber));
        }
    }

    /**
     * for set others symbols invisible
     */
    static void resetSymbolsOnTheReels() {
        reels.forEach(reel -> {
            for (int i = 0; i < 4; i++) {
                reel.setDisplayedSymbol(i, "Green");
                reel.redraw();
                finishKeyFrameAnimation = false;
            }
        });
    }

    /**
     * For playing track key frame animation
     */
    static void replaceReelSymbolsForAnimation() {
        kReel0 = GameEngine.current().getViewManager().findViewById("screenSaver", trackAnimation.get("reel0" + sceneNumber));
        kReel1 = GameEngine.current().getViewManager().findViewById("screenSaver", trackAnimation.get("reel1" + sceneNumber));
        kReel2 = GameEngine.current().getViewManager().findViewById("screenSaver", trackAnimation.get("reel2" + sceneNumber));
        kReel3 = GameEngine.current().getViewManager().findViewById("screenSaver", trackAnimation.get("reel3" + sceneNumber));
        kReel4 = GameEngine.current().getViewManager().findViewById("screenSaver", trackAnimation.get("reel4" + sceneNumber));

        playKeyFrame(kReel0);
        playKeyFrame(kReel1);
        playKeyFrame(kReel2);
        playKeyFrame(kReel3);
        playKeyFrame(kReel4);
    }

    private static void playKeyFrame(KeyframeAnimationView trackKeyFrame) {
        if (trackKeyFrame != null) {
            trackKeyFrame.gotoAndPlay(1);
            trackKeyFrame.setVisible(true);
            trackKeyFrame.addPropertyChangedListener(stopTrackKeyframeListener);
            symbolsForSteps.get(sceneNumber).getChildren().forEach(
                    symbol -> symbol.setVisible(true)
            );
        }
    }

    /**
     * Start play key frame animation when reels stopped
     */
    static class ReelsNextObserver extends NextObserver<ReelState> {

        @Override
        public void onNext(ReelState reelState) {
            if (activeScene4 && reelState.name().equals("IDLE")) {
                replaceReelSymbolsForAnimation();//play track key frame animation
                resetSymbolsOnTheReels();//set other symbols invisible
            }
        }
    }

    /**
     * For set visible symbols under the track key frames and for sets invisible track keyframes
     */
    static class StopTrackKeyframeListener implements IViewPropertyChangedListener {

        @Override
        public void propertyChanged(View view, ViewType viewType, int property) {
            KeyframeAnimationView trackKeyFrame = (KeyframeAnimationView) view;
            if (trackKeyFrame.isStopped()) {
                trackKeyFrame.removePropertyChangedListener(stopTrackKeyframeListener);
                trackKeyFrame.setVisible(false);
            }

            if (!finishKeyFrameAnimation) {//if one of the track key frame animation finish, then start next scene with timeout
                finishKeyFrameAnimation = true;
                if (sceneNumber < 5) {
                    sceneNumber++;
                    playNextSceneTimeout = new Timeout(1900, () -> {
                        symbolsForSteps.get(sceneNumber - 1).getChildren().forEach(symbol -> symbol.setVisible(false));
                        playScene();
                    }, true);
                } else {
                    new Timeout(1900, () -> {
                        resetScene();
                    }, true);

                }
            }
        }
    }

    /**
     * for reset all parameters and hide Scene4
     */
    public static void resetScene() {

        for (int i = 1; i <= 5; i++) {
            View view = GameEngine.current().getViewManager().findViewById("screenSaver", "track4"+i);
            if(view.isVisible()){
                view.setVisible(false);
            }
        }

        if (activeScene4) {
            ViewGroup symbols = symbolsForSteps.get(sceneNumber);
            if (symbols != null) {
                symbols.getChildren().forEach(
                        symbol -> {
                            if (symbol.isVisible()) {
                                symbol.setVisible(false);
                            }
                        }
                );
            }
            activeScene4 = false;
            if (playNextSceneTimeout != null && !playNextSceneTimeout.isCleaned()) {
                playNextSceneTimeout.clear();
            }
            if (reelTimeout0 != null && !reelTimeout0.isCleaned()) {
                reelTimeout0.clear();
            }
            if (reelTimeout1 != null && !reelTimeout1.isCleaned()) {
                reelTimeout1.clear();
            }
            if (reelTimeout2 != null && !reelTimeout2.isCleaned()) {
                reelTimeout2.clear();
            }
            if (reelTimeout3 != null && !reelTimeout3.isCleaned()) {
                reelTimeout3.clear();
            }
            if (reelTimeout4 != null && !reelTimeout4.isCleaned()) {
                reelTimeout4.clear();
            }
            if (kReel0 != null && kReel0.isVisible()) {
                kReel0.setVisible(false);
                if (kReel0.isPlaying()) {
                    kReel0.stop();
                }
                try {
                    kReel0.removePropertyChangedListener(stopTrackKeyframeListener);
                } catch (Exception e) {

                }
            }
            if (kReel1 != null && kReel1.isVisible()) {
                kReel1.setVisible(false);
                if (kReel1.isPlaying()) {
                    kReel1.stop();
                }
                try {
                    kReel1.removePropertyChangedListener(stopTrackKeyframeListener);
                } catch (Exception e) {

                }
            }
            if (kReel2 != null && kReel2.isVisible()) {
                kReel2.setVisible(false);
                if (kReel2.isPlaying()) {
                    kReel2.stop();
                }
                try {
                    kReel2.removePropertyChangedListener(stopTrackKeyframeListener);
                } catch (Exception e) {

                }
            }
            if (kReel3 != null && kReel3.isVisible()) {
                kReel3.setVisible(false);
                if (kReel3.isPlaying()) {
                    kReel3.stop();
                }
                try {
                    kReel3.removePropertyChangedListener(stopTrackKeyframeListener);
                } catch (Exception e) {

                }
            }
            if (kReel4 != null && kReel4.isVisible()) {
                kReel4.setVisible(false);
                if (kReel4.isPlaying()) {
                    kReel4.stop();
                }
                try {
                    kReel4.removePropertyChangedListener(stopTrackKeyframeListener);
                } catch (Exception e) {

                }
            }
            reel0.setVisible(false);
            reel1.setVisible(false);
            reel2.setVisible(false);
            reel3.setVisible(false);
            reel4.setVisible(false);
        }

    }

}

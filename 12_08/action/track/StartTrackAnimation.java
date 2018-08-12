package com.atsisa.gox.games.trextrack.action.track;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.framework.eventbus.IEventBus;
import com.atsisa.gox.framework.infrastructure.ISoundManager;
import com.atsisa.gox.framework.infrastructure.IViewManager;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.utility.TimeoutCallback;
import com.atsisa.gox.framework.view.IViewPropertyChangedListener;
import com.atsisa.gox.framework.view.KeyframeAnimationView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.framework.view.ViewType;
import com.atsisa.gox.games.trextrack.TRexTrackWildHitChecker;
import com.atsisa.gox.games.trextrack.event.ChangeTagStateEvent;
import com.atsisa.gox.games.trextrack.event.FixTrackSymbolsAfterWin;
import com.atsisa.gox.games.trextrack.event.SkipTrackLongAnimationEvent;
import com.atsisa.gox.games.trextrack.screen.model.BaseGameScreenModel;
import com.atsisa.gox.reels.AbstractReelGame;
import com.atsisa.gox.reels.IWinLineInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StartTrackAnimation extends Action<StartTrackAnimationData> {

    /**
     * counts track symbols on each reel
     */
    private static int[] count;

    /**
     * contains first cell with track symbol on ech reel: 00->Track 30->TrackR
     */
    private static Map<String, String> trackAnimation;

    /**
     * for calculate y coordinates for track animation
     */
    private static final int symbolHeight = 216;

    /**
     * contains the name of track animation and their y-coordinates: TrackAnimation10 -> 11; TrackAnimationRevers32 -> 215
     */
    private Map<String, Integer> animationId;
    KeyframeAnimationView lastKeyFrame;

    /**
     * when ended last animation then we finish() this action
     */
    LastKeyFrameListener lastKeyFrameListener;

    static int cycle = 0;

    private static final IViewManager viewManager = GameEngine.current().getViewManager();
    private static final IEventBus eventBus = GameEngine.current().getEventBus();
    private static final ISoundManager soundManager = GameEngine.current().getSoundManager();

    private boolean slowAnimation = true;
    private static final int SLOW_FRAME_RATE_DIVIDER = 15;
    private static final int FAST_FRAME_RATE_DIVIDER = 45;

    @Override
    protected void execute() {
        eventBus.post(new SkipTrackLongAnimationEvent());
        count = ((BaseGameScreenModel) viewManager.getScreenById("baseGameScreen").getModel()).getCounts();
        trackAnimation = ((BaseGameScreenModel) viewManager.getScreenById("baseGameScreen").getModel()).getTrackAnimation2();
        animationId = new LinkedHashMap<>();
        if (trackAnimation.size() == 0) {
            finish();
        } else {
//            timeout = new Timeout[trackAnimation.size()];
            if (lastKeyFrameListener == null) {
                lastKeyFrameListener = new LastKeyFrameListener();
            }
            formStopSymbols();
            if (this.actionData.getWin()) {
                slowAnimation = getSlowAnimation();
            } else {
                slowAnimation = false;
            }

            playAnimation();

        }
    }

    /**
     * for fill animationId (LinkedHashMap) with the name of track animation and their y-coordinates
     */
    private void formStopSymbols() {
        for (int i = 0; i < 5; i++) {//columns
            if (count[i] > 0) {
                for (int j = 0; j < 4; j++) {//rows
                    if (trackAnimation.get(i + "" + j) != null) {
                        String trackName;
                        if (trackAnimation.get(i + "" + j).equals("Track")) {
                            trackName = "TrackAnimation";
                        } else {
                            trackName = "TrackAnimationRevers";
                        }
                        animationId.put(trackName + count[i] + i, 11 + j * symbolHeight);
                    }
                }
            }
        }
    }

    /**
     * for playing track animation and sets track symbols invisible
     */
    private void playAnimation() {
        if(slowAnimation){
            soundManager.play("stones");
        }else{
            soundManager.play("stones_2");
        }

        int i = 1;//

        for (String key : animationId.keySet()) {
            viewManager.findViewById("baseGameScreen", key).setVisible(true);
            if (i == trackAnimation.size()) {//play last track animation
                if (lastKeyFrame != null && lastKeyFrame.hasEventListeners()) {
                    lastKeyFrame.removePropertyChangedListener(lastKeyFrameListener);
                }

                lastKeyFrame = viewManager.findViewById("baseGameScreen", key);
                lastKeyFrame.setY(animationId.get(key));
                ((KeyframeAnimationView) lastKeyFrame.setY(animationId.get(key))).gotoAndPause(1);
                lastKeyFrame.setVisible(true);
                lastKeyFrame.addPropertyChangedListener(lastKeyFrameListener);

                if (slowAnimation) {
                    lastKeyFrame.setFps(SLOW_FRAME_RATE_DIVIDER);
                    new Timeout(i * 100, new AnimationTimeOffset(i, lastKeyFrame), true);
                } else {
                    lastKeyFrame.setFps(FAST_FRAME_RATE_DIVIDER);
                    new Timeout(100, new AnimationTimeOffset(i, lastKeyFrame), true);
                }

//                lastKeyFrame.play();

                BaseGameScreenModel propertiy = ((BaseGameScreenModel) viewManager.getScreenById("baseGameScreen").getModel());
                new Timeout(200, new TimeoutCallback() {
                    @Override
                    public void onTimeout() {//sets the track symbols invisible and post command for show random symbols under the track animation
                        int symbolId = TRexTrackWildHitChecker.getWinTrackSymbol();
                        eventBus.post(new ChangeTagStateEvent(BaseGameScreenModel.getImageViews().get(symbolId)));
                        eventBus.post(new FixTrackSymbolsAfterWin());
                    }
                }, true);

            } else {//play track animation
                viewManager.findViewById("baseGameScreen", key).setY(animationId.get(key));
                ((KeyframeAnimationView) viewManager.findViewById("baseGameScreen", key)).gotoAndPause(1);
                viewManager.findViewById("baseGameScreen", key).setVisible(true);
                KeyframeAnimationView tracksFrames = (KeyframeAnimationView) viewManager.findViewById("baseGameScreen", key);
                if (slowAnimation) {
                    tracksFrames.setFps(SLOW_FRAME_RATE_DIVIDER);
                    new Timeout(i * 100, new AnimationTimeOffset(i, tracksFrames), true);
                } else {
                    tracksFrames.setFps(FAST_FRAME_RATE_DIVIDER);
                    new Timeout(100, new AnimationTimeOffset(i, tracksFrames), true);
                }

            }
            i++;
        }
    }

    /**
     * For close track animation
     */
    class LastKeyFrameListener implements IViewPropertyChangedListener {
        @Override
        public void propertyChanged(View view, ViewType viewType, int property) {
            if (((KeyframeAnimationView) view).isStopped()) {
                lastKeyFrame.removePropertyChangedListener(lastKeyFrameListener);
                // view.setVisible(false);

                for (String key : animationId.keySet()) {
                    ((KeyframeAnimationView) viewManager.findViewById("baseGameScreen", key)).stop();
                    viewManager.findViewById("baseGameScreen", key).setVisible(false);
                }
                finish();
            }
        }
    }

    class AnimationTimeOffset implements TimeoutCallback {
        int reelNumber;
        KeyframeAnimationView trackAnimation;

        AnimationTimeOffset(int reelNumber, KeyframeAnimationView lastKeyFrame) {
            this.reelNumber = reelNumber;
            this.trackAnimation = lastKeyFrame;
        }

        @Override
        public void onTimeout() {
            BaseGameScreenModel propertiy = ((BaseGameScreenModel) viewManager.getScreenById("baseGameScreen").getModel());
            propertiy.setProperty("TrackRRevers" + (reelNumber), false);
            propertiy.setProperty("TrackR" + (reelNumber), false);
            propertiy.setProperty("TrackRevers" + (reelNumber), false);
            propertiy.setProperty("Track" + (reelNumber), false);

            // if(reelNumber==5){
            trackAnimation.play();
            // }
//            trackAnimation.play();
        }
    }

    /**
     * for slow or fast animation
     */
    private boolean getSlowAnimation() {
        boolean slow = false;
        Map<String, String> trackPositions = new HashMap<>();

        for (int j = 0; j < 5; j++) {
            Iterable<String> tracks = (Iterable<String>) BaseGameScreenModel.getStopReelsSymbols().get(j);
            int row = 0;
            for (String track : tracks) {
                trackPositions.put(j + "" + row, track);
                row++;
            }

        }

        Iterable<? extends IWinLineInfo> winLines = ((AbstractReelGame) GameEngine.current().getGame()).getLinesModelProvider().getLinesModel().getWinningLines();

        label:
        for (IWinLineInfo winLine : winLines) {//all win lines
            Iterable<Integer> positions = winLine.getPositions();

            int lineReel = 0;
            for (int position : positions) {
                if (position > 0) {

                    String track = trackPositions.get(lineReel + "" + position);
                    if (track.equals("Track") || track.equals("TrackR")) {
                        slow = true;
                        break label;
                    }
                }
                lineReel++;
            }
        }
        return slow;
    }

    @Override
    public Class<StartTrackAnimationData> getActionDataType() {
        return StartTrackAnimationData.class;
    }

}

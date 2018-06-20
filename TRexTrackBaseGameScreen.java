package com.atsisa.gox.games.trextrack.screen;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.animation.IAnimationFactory;
import com.atsisa.gox.framework.animation.TweenViewAnimation;
import com.atsisa.gox.framework.animation.TweenViewAnimationData;
import com.atsisa.gox.framework.command.ChangeLanguageCommand;
import com.atsisa.gox.framework.eventbus.IEventBus;
import com.atsisa.gox.framework.eventbus.NextObserver;
import com.atsisa.gox.framework.eventbus.annotation.Subscribe;
import com.atsisa.gox.framework.infrastructure.ISoundManager;
import com.atsisa.gox.framework.infrastructure.IViewManager;
import com.atsisa.gox.framework.rendering.IRenderer;
import com.atsisa.gox.framework.screen.annotation.ExposeMethod;
import com.atsisa.gox.framework.utility.Iterables;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.utility.TimeoutCallback;
import com.atsisa.gox.framework.utility.logger.ILogger;
import com.atsisa.gox.framework.view.ImageView;
import com.atsisa.gox.framework.view.KeyframeAnimationView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.games.octavian.core.event.ApplyReelsRotationTimeEvent;
import com.atsisa.gox.games.octavian.core.event.ReelSpeedChangeEvent;
import com.atsisa.gox.games.octavian.core.service.ReelsService;
import com.atsisa.gox.games.trextrack.event.*;
import com.atsisa.gox.games.trextrack.event.freegames.EnterFreeGamesEvent;
import com.atsisa.gox.games.trextrack.screen.model.BaseGameScreenModel;
import com.atsisa.gox.logic.provider.IPayTableProvider;
import com.atsisa.gox.reels.AbstractReelGame;
import com.atsisa.gox.reels.ReelsPresentationStates;
import com.atsisa.gox.reels.animation.ReelAnimation;
import com.atsisa.gox.reels.command.SkipCommand;
import com.atsisa.gox.reels.event.LinesModelChangedEvent;
import com.atsisa.gox.reels.event.PresentationStateChangedEvent;
import com.atsisa.gox.reels.screen.BaseGameScreen;
import com.atsisa.gox.reels.view.AbstractReel;
import com.atsisa.gox.reels.view.AbstractSymbol;
import com.atsisa.gox.reels.view.ReelGroupView;
import com.atsisa.gox.reels.view.ReelView;
import com.atsisa.gox.reels.view.state.ReelState;
import rx.Observable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Represents the base game screen
 */
public class TRexTrackBaseGameScreen extends BaseGameScreen {

    /**
     * Flag: is debug button visible
     */
    private final String DEBUG_BUTTON_VISIBLE = "debugButtonVisible";
    private List positions;
    private AbstractSymbol symbol;
    private ArrayList previousSymbols;
    private ReelGroupView reelGroupView;
    private static final String REEL_GROUP_VIEW = "reelGroupView";
    private static final String REEL_STATE_STOPPING = "STOPPING";
    private static final String REEL_STATE_SPINNING = "SPINNING";
    private static final String SYMBOL_STATE_SHORT_ANIMATION = "Short";
    private static final String IDLE_REEL_STATE = "IDLE";
    private ReelAnimation reelAnimation;

    //    private Map<Integer, Boolean> trackLongSpin = new HashMap<>();
    private static Map<Integer, Integer> trackLongSpinTimeStart;
    private static Map<Integer, Integer> scatterLongSpinTimeStart;
    private Timeout[] trackLongSpinTimeout = new Timeout[3];
    private Timeout[] scatterLongSpinTimeout = new Timeout[3];


    private Timeout[] trackEndSoundTimeout = new Timeout[4];
    private Timeout[] scatterEndSoundTimeout = new Timeout[5];

    @Inject
    public ReelsService reelsHelper;

    @Inject
    public IPayTableProvider payTableProvider;

    Timeout stopNotWinAnimation;

    /**
     * for determining the state of the drum reels
     */
    private Observable<ReelState>[] reelStateObservable = new Observable[5];
    /**
     * True, if this reels was stopped by clicking on it
     */
    private Map<Integer, Boolean> clickOnSpinningReels = new HashMap() {{
        put(0, false);
        put(1, false);
        put(2, false);
        put(3, false);
        put(4, false);
    }};

//    private boolean[] trackSoundStop;

    ISoundManager soundManager;

    private Map<Integer, Boolean> trackStopSounds = new HashMap();
    private Map<Integer, Boolean> scatterStopSounds = new HashMap();

    private Integer[] spinTime = {400, 700, 1000, 1300, 1600};

    /**
     * Initializes a new instance of the {@link TRexTrackBaseGameScreen} class.
     *
     * @param layoutId         layout identifier
     * @param model            {@link BaseGameScreenModel}
     * @param renderer         {@link IRenderer}
     * @param viewManager      {@link IViewManager}
     * @param animationFactory {@link IAnimationFactory}
     * @param logger           {@link ILogger}
     * @param eventBus         {@link IEventBus}
     */
    @Inject
    public TRexTrackBaseGameScreen(@Named(LAYOUT_ID_PROPERTY) String layoutId, BaseGameScreenModel model, IRenderer renderer, IViewManager viewManager, IAnimationFactory animationFactory,
                                   ILogger logger, IEventBus eventBus, ISoundManager soundManager) {
        super(layoutId, model, renderer, viewManager, animationFactory, logger, eventBus);
        this.soundManager = soundManager;
        eventBus.register(new TRexTrackBaseGameScreen.LinesModelChangedEventObserver(), LinesModelChangedEvent.class);

    }

    @Override
    protected void registerEvents() {

        super.registerEvents();
        getEventBus().register(new PresentationStateChangedEventObserver(), PresentationStateChangedEvent.class);

        getEventBus().register(new CleanCollectionWithWinSymbolsObserver(), CleanCollectionWithWinSymbols.class);

        getEventBus().register(new EnterFreeGamesEventHandler(), EnterFreeGamesEvent.class);

    }

    /**
     * If reels is start spin, then do active buttons for stop reels
     */
    @Override
    protected void afterActivated() {
        super.afterActivated();
        getEventBus().register(new CreateCollectionWithWinSymbolsObserver(), CreateCollectionWithWinSymbols.class);

        reelGroupView = (ReelGroupView) findViewById(REEL_GROUP_VIEW);
        for (int i = 0; i < reelGroupView.getChildCount(); i++) {
            setReelTouchButtonVisible(i + 1, false);
            reelStateObservable[i] = ((ReelView) reelGroupView.getReel(i)).getReelStateObservable();

        }
        getEventBus().post(new ReelSpeedChangeEvent(0, 1.5f));
        getEventBus().post(new ApplyReelsRotationTimeEvent(new ArrayList() {{
            add(400);
            add(700);
            add(1000);
            add(1300);
            add(1600);
        }}));

        initSpeed();

        new Timeout(1000, new TempTimeout(), true);

        new Timeout(300, new TimeoutCallback() {
            @Override
            public void onTimeout() {
                getEventBus().post(new ChangeLanguageCommand("EN"));
            }
        }, true);

    }

    /**
     * for start and stop
     */
    void initSpeed() {
        class ReelsNextObserver extends NextObserver<ReelState> {
            int reelNumber;
            float[] originX = new float[5];
            float[] originY = new float[5];
            float[] originScaleX = new float[5];
            float[] originScaleY = new float[5];
            float[] originHeight = new float[5];
            float[] originWidth = new float[5];

            private static final float SCALE_X = 1.5f;
            private static final float SCALE_Y = 1.5f;
            TweenViewAnimation tweenView = GameEngine.current().getAnimationFactory().createAnimation(TweenViewAnimation.class);
            TweenViewAnimationData[] animation = new TweenViewAnimationData[5];

            ImageView[] scatter = new ImageView[5];

            ReelsNextObserver(int rellNumber) {
                this.reelNumber = rellNumber;

                for (int k = 0; k < 5; k++) {
                    scatter[k] = (ImageView) findViewById("scatter" + (k + 1));
                    originX[k] = scatter[k].getX();
                    originScaleX[k] = scatter[k].getScaleX();
                    originScaleY[k] = scatter[k].getScaleY();

                    originHeight[k] = scatter[k].getHeight();
                    originHeight[k] = scatter[k].getWidth();
                }
            }

            @Override
            public void onNext(ReelState reelState) {

/*                if (reelState.name().equals("STOPPING")) {
//                    soundManager.play("ReelStop" + reelNumber);
//                    System.out.println();
                } else if (reelState.name().equals("IDLE")) {
*//*                    new Timeout(150, () -> {
                        soundManager.stop("ReelStop" + reelNumber);
                    }, true);*//*

                    for (int i = 0; i < 4; i++) {
                        AbstractSymbol scatterSymbol = reelGroupView.getReel(reelNumber).getDisplayedSymbol(i);
                        if (scatterSymbol.getName().equals("Scatter")) {
//                            System.out.println();
                            scatter[reelNumber].setY(-63 + i * 216);
                            originY[reelNumber] = scatter[reelNumber].getY();
                            scatter[reelNumber].setVisible(true);

                            animation[reelNumber] = new TweenViewAnimationData();

                            animation[reelNumber].setDestinationScaleX(SCALE_X);
                            animation[reelNumber].setDestinationScaleY(SCALE_Y);
                            animation[reelNumber].setDestinationX((float) (originX[reelNumber] - (originWidth[reelNumber] * SCALE_X - originWidth[reelNumber]) / 1.7));
                            animation[reelNumber].setDestinationY((float) (originY[reelNumber] - (originHeight[reelNumber] * SCALE_Y - originHeight[reelNumber]) / 2));
                            animation[reelNumber].setTimeSpan(200);
                            tweenView.setTargetView(scatter[reelNumber]);
                            tweenView.setViewAnimationData(animation[reelNumber]);
                            tweenView.play();
                        }
                    }

                    System.out.println();
                }*/
            }
        }
        for (int i = 0; i < reelStateObservable.length; i++) {
            reelStateObservable[i].subscribe(new ReelsNextObserver(i));
        }

    }

    private void handlePresentationStateChangedEvent(PresentationStateChangedEvent presentationStateChangedEvent) {
        switch (presentationStateChangedEvent.getStateName()) {
            case ReelsPresentationStates.IDLE:
                setModelProperty(DEBUG_BUTTON_VISIBLE, true);
                break;
            default:
                setModelProperty(DEBUG_BUTTON_VISIBLE, false);
        }
    }

    private class PresentationStateChangedEventObserver extends NextObserver<PresentationStateChangedEvent> {
        @Override
        public void onNext(final PresentationStateChangedEvent presentationStateChangedEvent) {
            handlePresentationStateChangedEvent(presentationStateChangedEvent);
        }
    }

    /**
     * Initializing handling lines
     */
    private class LinesModelChangedEventObserver extends NextObserver<LinesModelChangedEvent> {

        @Override
        public void onNext(LinesModelChangedEvent linesModelChangedEvent) {
            handleLinesModelChanged(linesModelChangedEvent);
        }
    }

    /**
     * Change the depth of characters when showing win lines
     *
     * @param linesModelChangedEvent
     */
    private void handleLinesModelChanged(LinesModelChangedEvent linesModelChangedEvent) {

        if (linesModelChangedEvent.getChangeType() == LinesModelChangedEvent.CURRENT_WINNING_LINE) {
            if (linesModelChangedEvent.getLinesModel().getCurrentWinningLine().isPresent()) {
                if (previousSymbols == null) {
                    previousSymbols = new ArrayList();
                } else {
                    for (int i = 0; i < previousSymbols.size(); i++) {
                        AbstractSymbol symbolType = ((AbstractSymbol) previousSymbols.get(i));
                        symbolType.setDepth(1);
                    }
                    previousSymbols = new ArrayList();
                }

                positions = Iterables.toList(linesModelChangedEvent.getLinesModel().getCurrentWinningLine().get().getPositions());

                for (int i = 0; i < positions.size(); i++) {
                    if ((int) positions.get(i) >= 0) {
                        symbol = reelGroupView.getReel(i).getDisplayedSymbol((int) positions.get(i));
                        symbol.setDepth(3);
                        previousSymbols.add(symbol);

                    }
                }
            }
        }
    }

    /**
     * Make a collection from the positions of winning symbols and names of symbols.\
     * Then send this map to the winLineScreen
     */
    private class CreateCollectionWithWinSymbolsObserver extends NextObserver<CreateCollectionWithWinSymbols> {

        @Override
        public void onNext(CreateCollectionWithWinSymbols event) {
            for (AbstractReel reelView : reelGroupView.getReels()) {
                int i = 0;

                for (AbstractSymbol symbol : reelView.getDisplayedSymbols()) {
                    if (symbol.getState().equals(SYMBOL_STATE_SHORT_ANIMATION)) {
                        ((BaseGameScreenModel) getModel()).addToSymbolsMap((reelView.getIndex() + 1) + "" + (i + 1), symbol.getName());
                    } else {
                        new AnimateTransparency(symbol).playAnimation();
                    }
                    i++;
                }
            }
            getEventBus().post(new SendWinSymbolsEvent(((BaseGameScreenModel) getModel()).getSymbolsMap()));
        }
    }

    /**
     * For fill the reels stop symbols Map
     */
    @Subscribe
    public void formStoppedSymbolsEvent(FormStoppedSymbols event) {
        /*            WinDescriptor winDescriptor = new WinDescriptor();
        winDescriptor.setScore(200);
        winDescriptor.setWinId(21);
        winDescriptor.setSymbol(new Symbol("Blue2", 10, "BaseSymbol"));
        winDescriptor.setSymbolsAmount(1);

        LineWinDescriptor lineWinDescriptor = new LineWinDescriptor();
        lineWinDescriptor.setWinDescriptor(winDescriptor);
        List<String> list = new ArrayList() {{
            add("LeftToRight");
        }};
        lineWinDescriptor.setWinLineCombinationStyles(list);
        payTableProvider.get().get().getWinDescriptors().set(21, lineWinDescriptor);*/

        List<Iterable<String>> stopListSymbols = ((AbstractReelGame) GameEngine.current().getGame()).getReelGameStateHolder().getStoppedSymbols();
        Map<Integer, Iterable<String>> stopSymbolsmap = ((BaseGameScreenModel) getModel()).getStopReelsSymbols();
        for (int i = 0; i < reelGroupView.getChildCount(); i++) {
            stopSymbolsmap.put(i, stopListSymbols.get(i));
            setReelTouchButtonVisible(i + 1, true);
            clickOnSpinningReels.put(i, false);
        }

        if (((String) ((LinkedList) stopListSymbols.get(0)).get(0)).equals("Track")) {
            //getEventBus().post(new ApplyReelsRotationTimeEvent(new ArrayList(){{add(400);add(700);add(5000);add(1300);add(1600);}}));
        } else {
            // getEventBus().post(new ApplyReelsRotationTimeEvent(new ArrayList(){{add(400);add(700);add(1000);add(1300);add(1600);}}));
        }
//            getModel().setProperty("symbolVisible", true);

        formTrackAnimation();
    }


    private void setReelTouchButtonVisible(int reelNumber, boolean visible) {
        if (visible) {
            setModelProperty("reel" + reelNumber + "Visible", Boolean.TRUE);
        } else {
            setModelProperty("reel" + reelNumber + "Visible", Boolean.FALSE);
        }

    }

    /**
     * For stopping reels by clicking on it
     *
     * @param reelNumber - reel number (0, 1, 2, 3, 4) that we want to stop
     */
    @ExposeMethod
    public void stopReel(int reelNumber) {

        //stop track long animation on this reel
        KeyframeAnimationView keyframeAnimationView = (KeyframeAnimationView) findViewById("trackSpin" + (reelNumber + 1));
        if (keyframeAnimationView != null) {

            if (keyframeAnimationView.isPlaying()) {
                keyframeAnimationView.stop();
                keyframeAnimationView.setVisible(false);
                if (trackLongSpinTimeout[reelNumber - 2] != null && !trackLongSpinTimeout[reelNumber - 2].isCleaned()) {
                    trackLongSpinTimeout[reelNumber - 2].clear();
                }

                if (scatterLongSpinTimeout[reelNumber - 2] != null && !scatterLongSpinTimeout[reelNumber - 2].isCleaned()) {
                    scatterLongSpinTimeout[reelNumber - 2].clear();
                }

                soundManager.stop("tracksLongSound" + (reelNumber + 1));
            }
        }

        //stop scatter long animation on this reel
        keyframeAnimationView = (KeyframeAnimationView) findViewById("ScatterSpin" + (reelNumber + 1));
        if (keyframeAnimationView != null) {

            if (keyframeAnimationView.isPlaying()) {
                keyframeAnimationView.stop();
                keyframeAnimationView.setVisible(false);

                if (scatterLongSpinTimeout[reelNumber - 2] != null && !scatterLongSpinTimeout[reelNumber - 2].isCleaned()) {
                    scatterLongSpinTimeout[reelNumber - 2].clear();
                }

                soundManager.stop("scattersLongSound" + (reelNumber + 1));
            }
        }

        List<String> stoppingSymbolsNames = new ArrayList<>();
        Iterable<String> stoppingSymbols = (Iterable<String>) ((BaseGameScreenModel) getModel()).getStopReelsSymbols().get(reelNumber);
        stoppingSymbols.forEach(symbolName -> {
            stoppingSymbolsNames.add(symbolName);
        });
        AbstractReel reel = reelGroupView.getReel(reelNumber);
        reelAnimation = (ReelAnimation) reelGroupView.getReel(reelNumber).getReelAnimation();
        reelAnimation.setSpeedIncrease(10);
        reelAnimation.speedUp();
        if (reel.getReelState().name().equals(REEL_STATE_SPINNING)) {
//            System.out.println("######## reel: " + reelNumber + "     " + reel.getReelState().name());
            reel.forceStopOnSymbols(stoppingSymbolsNames);
        }
        clickOnSpinningReels.put(reelNumber, true);
        setReelTouchButtonVisible(reelNumber + 1, false);
//        skipReelsSpin();
    }

    /**
     * for skip command if all reels stopped
     */
    private void skipReelsSpin() {
        int stopReelsCount = 0;
        for (int i = 0; i < reelGroupView.getChildCount(); i++) {
            if (reelGroupView.getReel(i).getReelState().name().equals(IDLE_REEL_STATE) || clickOnSpinningReels.get(i)) {
                stopReelsCount++;
            }
        }
        if (stopReelsCount == reelGroupView.getChildCount()) {
            try {
                getEventBus().post(new SkipCommand());
            } catch (Exception e) {
                System.err.println("Skip reels spin ERROR!!!");
            }

        }

    }

    /**
     * Clear collection with wining symbols positions
     */
    private class CleanCollectionWithWinSymbolsObserver extends NextObserver<CleanCollectionWithWinSymbols> {

        @Override
        public void onNext(CleanCollectionWithWinSymbols cleanCollectionWithWinSymbols) {
            ((BaseGameScreenModel) getModel()).clearSymbolsMap();
        }
    }

    @Subscribe
    public void handleSomeEvent(SymbolsTransparency event) {
        if (!event.getTransparency()) {
            for (AbstractReel reelView : reelGroupView.getReels()) {
                for (AbstractSymbol symbol : reelView.getDisplayedSymbols()) {
                    symbol.setAlpha(1);
                    symbol.setState("IDLE");
                }
            }
        }
    }


    class AnimateTransparency {
        TweenViewAnimation tweenViewAnimation;
        TweenViewAnimationData tweenViewAnimationData;
        AbstractSymbol symbol;

        AnimateTransparency(AbstractSymbol symbol) {
            this.symbol = symbol;
            tweenViewAnimation = GameEngine.current().getAnimationFactory().createAnimation(TweenViewAnimation.class);
            tweenViewAnimationData = new TweenViewAnimationData();
        }

        void playAnimation() {
            tweenViewAnimationData.setTimeSpan(300);
            tweenViewAnimationData.setDestinationAlpha(0.6f);
            if (tweenViewAnimation != null && tweenViewAnimation.isPlaying()) {
                tweenViewAnimation.stop();
            }
            tweenViewAnimation.setViewAnimationData(tweenViewAnimationData);
            tweenViewAnimation.setTargetView(symbol);
            tweenViewAnimation.play();

            if (stopNotWinAnimation != null && !stopNotWinAnimation.isCleaned()) {
                stopNotWinAnimation.clear();
            }
            new Timeout(400, new TimeoutCallback() {
                @Override
                public void onTimeout() {
                    symbol.getChildren().forEach(symb -> {
                        if (symb instanceof KeyframeAnimationView && ((KeyframeAnimationView) symb).isPlaying()) {
                            ((KeyframeAnimationView) symb).stop();
                        }
                    });
                }
            }, true);


        }

    }

    /**
     * For animate Track symbols - counts track symbols on the reels and put their names and positions into the hash map
     * For find scatter - symbols on the reels
     */
    private void formTrackAnimation() {
        Map<String, String> trackAnimation2 = new HashMap<>();//track position and track type
        int[] counts = new int[5];//tracks count
        boolean[] scatter = new boolean[5];//scatter on the reels

        for (int i = 0; i < 5; i++) {//columns
            int count = 0;
            Iterable<String> tracks = ((BaseGameScreenModel) getModel()).getStopReelsSymbols().get(i);

            int row = -1;
            int beginRow = -1;
            for (String name : tracks) {
                row++;

                if (name.equals("Track") || name.equals("TrackR")) {

                    count++;
                    if (beginRow == -1) {
                        beginRow = row;
                        trackAnimation2.put((i + "" + beginRow), name);

                        if (name.equals("TrackR")) {

/*                            AbstractReel trackReel = reelGroupView.getReel(i);

                            for (AbstractReel reel : reelGroupView.getReels()) {
                                reel.getDisplayedSymbols().stream().forEach(abstractSymbol -> {
                                    abstractSymbol.setState("Inactive");
                                });
                            }*/
                            getModel().setProperty("TrackRRevers" + (i + 1), true);
                            getModel().setProperty("TrackR" + (i + 1), false);
                            getModel().setProperty("TrackRevers" + (i + 1), true);
                            getModel().setProperty("Track" + (i + 1), false);
                        } else {
                            getModel().setProperty("TrackRRevers" + (i + 1), false);
                            getModel().setProperty("TrackR" + (i + 1), true);
                            getModel().setProperty("TrackRevers" + (i + 1), false);
                            getModel().setProperty("Track" + (i + 1), true);
                        }

                    }
                }

                if (name.equals("Scatter")) {
                    scatter[i] = true;
                }

            }
            counts[i] = count;
        }

        ((BaseGameScreenModel) getModel()).setCounts(counts);
        ((BaseGameScreenModel) getModel()).setTrackAnimation2(trackAnimation2);

        List<Integer> trackTime = setRotationTrackSymbol(counts);

        setRotationScatter(scatter, trackTime);

/*        for(int j=0; j<trackStopSounds.size(); j++){
            trackStopSounds.get(j);
        }*/
        trackEndSoundTimeout = new Timeout[trackStopSounds.size()];
        trackStopSounds.forEach((key, value) -> {

            int time = spinTime[key + 1] + 400;

            TrackLongEndSound trackLongEndSound = new TrackLongEndSound("spin_end_for_tracks_" + key);
            trackEndSoundTimeout[key] = new Timeout(time, trackLongEndSound, true);

        });

        scatterEndSoundTimeout = new Timeout[scatterStopSounds.size()];

        scatterStopSounds.forEach((key, value) -> {

            int time = spinTime[key] + 400;

            scatterEndSoundTimeout[key-1] = new Timeout(time, new TimeoutCallback() {
                @Override
                public void onTimeout() {
                    GameEngine.current().getSoundManager().play("scatter_stop_" + key);
                }
            }, true);

        });


//        getEventBus().post(new ApplyReelsRotationTimeEvent(finalTime));
    }

    class TrackLongEndSound implements TimeoutCallback {
        String endSound;

        TrackLongEndSound(String endSound) {
            this.endSound = endSound;
        }

        @Override
        public void onTimeout() {
            GameEngine.current().getSoundManager().play(endSound);
        }
    }

    /**
     * Set time spin increase reels if in the reels 4 track symbols from left to right
     *
     * @param featureCounts
     */
    private List<Integer> setRotationTrackSymbol(int[] featureCounts) {
//        trackSoundStop = new boolean[4];
        trackStopSounds = new HashMap<>();
        List<Integer> spinTime = new ArrayList<>();
        spinTime.add(400);
        spinTime.add(700);
        trackLongSpinTimeStart = new HashMap<>();
        // if (featureCounts[0] == 4) {
//            trackSoundStop[0] = true;
        //     trackStopSounds.put(0, true);
        //  }

        if (featureCounts[0] == 4 && featureCounts[1] == 4) {
            spinTime.add(3700);

//            trackSoundStop[1] = true;
            trackStopSounds.put(0, true);
            trackLongSpinTimeStart.put(3, 700);
            if (featureCounts[2] == 4) {
                trackStopSounds.put(1, true);
                trackLongSpinTimeStart.put(4, 3700);
                spinTime.add(6700);
//                trackSoundStop[2] = true;
                if (featureCounts[3] == 4) {
                    trackStopSounds.put(2, true);
                    spinTime.add(9700);
                    trackLongSpinTimeStart.put(5, 6700);
//                    trackSoundStop[3] = true;
                    if (featureCounts[4] == 4) {
                        trackStopSounds.put(3, true);
                    }
                } else {
                    spinTime.add(7000);
                }
            } else {
                spinTime.add(4000);
                spinTime.add(4300);
            }
        } else {
            spinTime.add(1000);
            spinTime.add(1300);
            spinTime.add(1600);
        }
//        getEventBus().post(new ApplyReelsRotationTimeEvent(spinTime));

        for (int trackLongKey : trackLongSpinTimeStart.keySet()) {
            KeyframeAnimationView keyframeAnimationView = (KeyframeAnimationView) findViewById("trackSpin" + trackLongKey);

            if (trackLongSpinTimeout[trackLongKey - 3] != null && !trackLongSpinTimeout[trackLongKey - 3].isCleaned()) {
                trackLongSpinTimeout[trackLongKey - 3].clear();
            }


            trackLongSpinTimeout[trackLongKey - 3] = new Timeout(trackLongSpinTimeStart.get(trackLongKey), new TrackLongSpinCallback(keyframeAnimationView, "tracksLongSound" + trackLongKey, true, trackLongKey), true);
            new Timeout(trackLongSpinTimeStart.get(trackLongKey) + 3000, new TrackLongSpinCallback(keyframeAnimationView, "tracksLongSound" + trackLongKey, false, trackLongKey), true);

        }
        return spinTime;
    }

    /**
     * Set time spin increase reels if in the reels 2 scatter symbols
     *
     * @param scatter
     * @param trackTime
     */
    private void setRotationScatter(boolean[] scatter, List<Integer> trackTime) {
        scatterLongSpinTimeStart = new HashMap<>();
        scatterStopSounds = new HashMap<>();
        spinTime = new Integer[]{400, 700, 1000, 1300, 1600};
        int scatterCount = 0;
        for (int j = 0; j < 5; j++) {
            if (scatter[j] == true) {
                scatterCount++;
                if (scatterCount >= 2) {
                    scatterStopSounds.put(j, true);
                }
            }

            if (scatterCount >= 2 && j < 4 && trackTime.get(j + 1) >= spinTime[j + 1]) {
                spinTime[j + 1] = spinTime[j] + 3000;
                scatterLongSpinTimeStart.put(j + 2, spinTime[j]);
            } else if ((scatterCount >= 2 && j < 4) || (j < 4)) {
                spinTime[j + 1] = trackTime.get(j + 1);
            }


        }
        getEventBus().post(new ApplyReelsRotationTimeEvent(Arrays.asList(spinTime)));


        for (int scatterLongKey : scatterLongSpinTimeStart.keySet()) {
            KeyframeAnimationView keyframeAnimationView = (KeyframeAnimationView) findViewById("ScatterSpin" + scatterLongKey);

            if (scatterLongSpinTimeout[scatterLongKey - 3] != null && !scatterLongSpinTimeout[scatterLongKey - 3].isCleaned()) {
                scatterLongSpinTimeout[scatterLongKey - 3].clear();
            }


            scatterLongSpinTimeout[scatterLongKey - 3] = new Timeout(scatterLongSpinTimeStart.get(scatterLongKey), new TrackLongSpinCallback(keyframeAnimationView, "scattersLongSound" + scatterLongKey, true, scatterLongKey), true);
            new Timeout(scatterLongSpinTimeStart.get(scatterLongKey) + 3000, new TrackLongSpinCallback(keyframeAnimationView, "scattersLongSound" + scatterLongKey, false, scatterLongKey), true);

        }

    }


    /**
     * for play track long animation and sound
     */
    class TrackLongSpinCallback implements TimeoutCallback {
        KeyframeAnimationView keyframeAnimationView;
        String sound;
        Boolean play;
        int reelNumber;


        public TrackLongSpinCallback(KeyframeAnimationView keyframeAnimationView, String sound, boolean play, int reelNumber) {
            this.keyframeAnimationView = keyframeAnimationView;
            this.play = play;
            this.sound = sound;
            this.reelNumber = reelNumber;
        }

        @Override
        public void onTimeout() {
            if (play && reelGroupView.getReel(reelNumber - 1).getReelState().name().equals(REEL_STATE_SPINNING)) {
                if (!keyframeAnimationView.isPlaying()) {
                    keyframeAnimationView.stop();
                }
                keyframeAnimationView.play();
                if (!keyframeAnimationView.isVisible()) {
                    keyframeAnimationView.setVisible(true);
                }
                GameEngine.current().getSoundManager().play(sound);


            } else {
                if (!keyframeAnimationView.isStopped()) {
                    keyframeAnimationView.stop();
                }
                if (keyframeAnimationView.isVisible()) {
                    keyframeAnimationView.setVisible(false);
                }
                GameEngine.current().getSoundManager().stop(sound);
            }

        }
    }


    /**
     * for stop track long animation and its sound
     *
     * @param event
     */
    @Subscribe
    public void stopTrackLongAnimationEvent(SkipTrackLongAnimationEvent event) {

        for (Timeout stopSound : trackEndSoundTimeout) {
            stopSound.clear();
        }
        for (Timeout stopSound : scatterEndSoundTimeout) {
            stopSound.clear();
        }

        for (Timeout timeout : trackLongSpinTimeout) {
            if (timeout != null && !timeout.isCleaned()) {
                timeout.clear();
            }
        }

        for (Timeout timeout : scatterLongSpinTimeout) {
            if (timeout != null && !timeout.isCleaned()) {
                timeout.clear();
            }
        }

        for (int i = 3; i < 6; i++) {
            View view = findViewById("trackSpin" + i);
            if (view != null && view.isVisible()) {
                view.setVisible(false);
            }

            if (view != null && ((KeyframeAnimationView) view).isPlaying()) {
                ((KeyframeAnimationView) view).stop();
            }

            soundManager.stop("tracksLongSound" + i);
        }

        for (int i = 3; i < 6; i++) {
            View view = findViewById("ScatterSpin" + i);
            if (view != null && view.isVisible()) {
                view.setVisible(false);
            }

            if (view != null && ((KeyframeAnimationView) view).isPlaying()) {
                ((KeyframeAnimationView) view).stop();
            }

            soundManager.stop("scattersLongSound" + i);
        }

    }

    /**
     * for visible key frame animation under the track symbols
     *
     * @param event
     */
    @Subscribe
    public void changeTagStateEvent(ChangeTagStateEvent event) {
        for (AbstractReel reel : reelGroupView.getReels()) {
            reel.getDisplayedSymbols().stream().forEach(abstractSymbol -> {
                if (abstractSymbol.getName().equals("TrackR") || abstractSymbol.getName().equals("Track")) {
                    abstractSymbol.setState(event.getTag());
                }
            });
        }
    }

    /**
     * For sets track symbols visible after click "start spin"
     *
     * @param event
     */
    @Subscribe
    public void fixTrackSymbolsAfterWin(FixTrackSymbolsAfterWin event) {

        for (int k = 0; k < 5; k++) {
            getModel().setProperty("TrackRRevers" + (k + 1), true);
            getModel().setProperty("TrackR" + (k + 1), false);
            getModel().setProperty("TrackRevers" + (k + 1), true);
            getModel().setProperty("Track" + (k + 1), false);
        }
    }

    class EnterFreeGamesEventHandler extends NextObserver<EnterFreeGamesEvent> {

        @Override
        public void onNext(EnterFreeGamesEvent enterFreeGamesEvent) {
            boolean base;
            boolean free;
            if(enterFreeGamesEvent.getEnter()){
                base = false;
                free = true;
            }else{
                base = true;
                free = false;
            }

            findViewById("Bgr_down_1").setVisible(base);
            findViewById("Bgr_down_2").setVisible(base);
            findViewById("Bgr_up_1").setVisible(base);
            findViewById("Bgr_up_2").setVisible(base);

            findViewById("Bgr_down_1f").setVisible(free);
            findViewById("Bgr_down_2f").setVisible(free);
            findViewById("Bgr_up_1f").setVisible(free);
            findViewById("Bgr_up_2f").setVisible(free);

        }
    }


    //for debug
    class TempTimeout implements TimeoutCallback {

        @Override
        public void onTimeout() {
            new Timeout(1000, new TempTimeout(), true);
            findViewById("scatter1");
        }
    }


}
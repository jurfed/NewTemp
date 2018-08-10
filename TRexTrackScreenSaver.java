package com.atsisa.gox.games.trextrack.screen.screensaver;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.animation.IAnimationFactory;
import com.atsisa.gox.framework.event.InputEvent;
import com.atsisa.gox.framework.event.InputEventType;
import com.atsisa.gox.framework.eventbus.IEventBus;
import com.atsisa.gox.framework.eventbus.NextObserver;
import com.atsisa.gox.framework.infrastructure.IViewManager;
import com.atsisa.gox.framework.rendering.IRenderer;
import com.atsisa.gox.framework.screen.Screen;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.utility.TimeoutCallback;
import com.atsisa.gox.framework.utility.localization.ITranslator;
import com.atsisa.gox.framework.utility.logger.ILogger;
import com.atsisa.gox.framework.view.*;
import com.atsisa.gox.games.octavian.core.screen.OctavianInfoScreen;
import com.atsisa.gox.games.trextrack.event.movie.MovieCompleted;
import com.atsisa.gox.games.trextrack.event.movie.PlayStandbyMovie;
import com.atsisa.gox.games.trextrack.logic.infoscreen.TRexTrackInfoScreenTransition;
import com.atsisa.gox.games.trextrack.screen.model.TRexTrackScreenSaverModel;
import com.atsisa.gox.reels.animation.ReelAnimation;
import com.atsisa.gox.reels.configuration.SymbolsConfiguration;
import com.atsisa.gox.reels.event.PresentationStateChangedEvent;
import com.atsisa.gox.games.octavian.core.event.WakeUpScreenSaverEvent;
import com.atsisa.gox.reels.view.ReelGroupView;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for start and stop playing standby scene
 */
public class TRexTrackScreenSaver extends OctavianInfoScreen {

    public static final String TREX_TRACK_SCREEN_SAVER = "TRexTrackScreenSaver";
    private ButtonView standbyButton;
    private View baseView;
    private static boolean playingVideo = false;

    @Inject
    public TRexTrackScreenSaver(@Named(TREX_TRACK_SCREEN_SAVER) String layoutId, TRexTrackScreenSaverModel model,
                                IRenderer renderer, IViewManager viewManager, IAnimationFactory animationFactory, ILogger logger, IEventBus eventBus, TRexTrackInfoScreenTransition infoScreenTransition, ITranslator translator) {
        super(layoutId, model, renderer, viewManager, animationFactory, logger, eventBus, infoScreenTransition, translator);
        eventBus.register(new ReelGroupStateHandler(), PresentationStateChangedEvent.class);
        eventBus.register(new MovieCompletedHandler(), MovieCompleted.class);
        eventBus.register(new StopVideoPlaying(), WakeUpScreenSaverEvent.class);

    }

    @Override
    protected void beforeActivated() {
        super.beforeActivated();
    }

    @Override
    protected void afterActivated() {
        super.afterActivated();
//        paytable_effect = findViewById("paytable_effect");
        logo_start = GameEngine.current().getViewManager().findViewById("screenSaver", "logo_start");
        text_1 = GameEngine.current().getViewManager().findViewById("screenSaver", "text_1");
        text_2 = GameEngine.current().getViewManager().findViewById("screenSaver", "text_2");
        logo_end = GameEngine.current().getViewManager().findViewById("screenSaver", "logo_end");
        movie = GameEngine.current().getViewManager().findViewById("screenSaver", "movie");

//        movieList.add(paytable_effect);
//        movieList.add(paytable_effect);
        movieList.add(logo_start);
        movieList.add(text_1);
        movieList.add(movie);
        movieList.add(text_1);
        movieList.add(movie);
        movieList.add(text_1);
        movieList.add(movie);
        movieList.add(text_2);
        movieList.add(logo_end);

        /**
         * for start debug standby video play
         */
        standbyButton = GameEngine.current().getViewManager().findViewById("debugScreen", "standby");
        standbyButton.addEventListener(event -> {
            if (((InputEvent) event).getType() == InputEventType.RELEASED) {
                if (startPlayScreenSaver != null) {
                    startPlayScreenSaver.clear();
                }
                startPlayScreenSaver = new Timeout(0, new StartPlayScreenSaver(), true);
            }
        });

        new Timeout(1000, new DebugClass(), true);
    }

    /**
     * Monitors the state of the game for start or stop video playing
     */
    class ReelGroupStateHandler extends NextObserver<PresentationStateChangedEvent> {

        @Override
        public void onNext(PresentationStateChangedEvent stateChangedEvent) {
            gameSatet = stateChangedEvent.getStateName();
            startPlayScreenSaver();
        }
    }

    //For stop playing video
    class StopVideoPlaying extends NextObserver<WakeUpScreenSaverEvent> {

        @Override
        public void onNext(WakeUpScreenSaverEvent wakeUpScreenSaver) {
            if (!wakeUpScreenSaver.isEnable()) {
                gameSatet = "other";
                resetScreenSaver();
            } else {
                if (startPlayScreenSaver != null) {
                    startPlayScreenSaver.clear();
                }
                startPlayScreenSaver = new Timeout(bigTime, new StartPlayScreenSaver(), true);
            }

        }
    }


    /**
     * Start timer for playing video if game state is idle
     */
    void startPlayScreenSaver() {
        if (gameSatet.equals("Idle")) {

            if (startPlayScreenSaver != null) {
                startPlayScreenSaver.clear();
            }
            startPlayScreenSaver = new Timeout(bigTime, new StartPlayScreenSaver(), true);
        }
    }

    /**
     * Start playing first video for screen saver
     */
    class StartPlayScreenSaver implements TimeoutCallback {
        @Override
        public void onTimeout() {
            playingVideo = true;
            baseView = getViewManager().getLayout("baseGameScreen").getRootView();
            baseView.setDepth(12);
            getViewManager().getStage().redraw();

            movieList.get(0).setVisible(true);
            movieList.get(0).play();
            GameEngine.current().getEventBus().post(new PlayStandbyMovie(true));
            stopPlayVideoListener = new StopPlayVideoListener(movieList.get(0), 0);
            movieList.get(0).registerMovieCompleteListener(stopPlayVideoListener);
        }
    }

    /**
     * set visible=false for previous video and start playing next video
     */
    class MovieCompletedHandler extends NextObserver<MovieCompleted> {

        @Override
        public void onNext(MovieCompleted movieCompleted) {
            int completedMovieId = movieCompleted.getMovieId();
            movieList.get(completedMovieId).unregisterMovieCompleteListener(stopPlayVideoListener);
            if (movieList.get(completedMovieId).isPlaying()) {
                movieList.get(completedMovieId).stop();
            }
            if (movieList.get(completedMovieId).isVisible()) {
                movieList.get(completedMovieId).setVisible(false);
            }
            playNextVide(completedMovieId);
        }
    }

    void playNextVide(int completedMovieId) {
        if (completedMovieId < movieList.size() - 1) {
            int newNextMovieId = ++completedMovieId;
//            if (completedMovieId == 1 || completedMovieId == 2) {
//                int delay;
//                if (completedMovieId == 1) {
//                    delay = 15000;
//                } else {
//                    delay = 5000;
//                }
//                paytableEffectTimeout = new Timeout(delay, new TimeoutCallback() {
//                    @Override
//                    public void onTimeout() {
//                        movieList.get(newNextMovieId).setVisible(true);
//                        movieList.get(newNextMovieId).play();
//                        stopPlayVideoListener = new StopPlayVideoListener(movieList.get(newNextMovieId), newNextMovieId);
//                        movieList.get(newNextMovieId).registerMovieCompleteListener(stopPlayVideoListener);
//                        showScenes(newNextMovieId);//shows texts, images and animation on the
//                    }
//                }, true);
//            } else {
            movieList.get(newNextMovieId).setVisible(true);
            movieList.get(newNextMovieId).play();
            stopPlayVideoListener = new StopPlayVideoListener(movieList.get(newNextMovieId), newNextMovieId);
            movieList.get(newNextMovieId).registerMovieCompleteListener(stopPlayVideoListener);
            showScenes(newNextMovieId);//shows texts, images and animation on the
            // }

        } else {
            bigTime = 15000;
            startPlayScreenSaver(); //if all video was played start new timer for play first video
        }
    }


    /**
     * If game state changed then stop playing all video and animation
     */
    void resetScreenSaver() {

        if (startPlayScreenSaver != null) {
            startPlayScreenSaver.clear();
            bigTime = 30000;
//            bigTime = 5000;

            GameEngine.current().getEventBus().post(new PlayStandbyMovie(false));
            if (paytableEffectTimeout != null) {
                paytableEffectTimeout.clear();
            }
        }
//        showScene2(false);
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).isPlaying()) {
                movieList.get(i).unregisterMovieCompleteListener(stopPlayVideoListener);
                movieList.get(i).stop();
                movieList.get(i).setVisible(false);

            }
        }

        IViewManager viewManager = GameEngine.current().getViewManager();
        ViewGroup stage = viewManager.getStage();

        if (playingVideo) {
            playingVideo = false;
            baseView = getViewManager().getLayout("baseGameScreen").getRootView();
            baseView.setDepth(0);

            stage.addChild(getViewManager().getLayout("baseGameScreen").getRootView());
            stage.addChild(getViewManager().getLayout("controlPanelScreen").getRootView());
            stage.addChild(getViewManager().getLayout("winLinesScreen").getRootView());
            stage.addChild(getViewManager().getLayout("payTableScreen").getRootView());
            stage.addChild(getViewManager().getLayout("screenSaver").getRootView());
            stage.addChild(getViewManager().getLayout("sideBarScreen").getRootView());
            stage.addChild(getViewManager().getLayout("logoScreen").getRootView());
            stage.addChild(getViewManager().getLayout("advancedControlPanelScreen").getRootView());
//            viewManager.getStage().redraw();
        }

        Scene1.getInstance().resetScene();
        Scene2.getInstance().resetScene();
        Scene4.getInstance().resetScene();
    }

    /*
    for choose scene for show texts, images, animations
     */
    void showScenes(int movieId) {
        switch (movieId + 1) {
            case 2:
                scene1 = Scene1.getInstance();
                scene1.start();

                break;
            case 4:
                /**
                 * win up to 10 dinosaurs.
                 * 1) 5 double Violet
                 * 2) 5 double pink
                 * 3) 5 duoble green
                 * 4) 5 double yellow
                 * 5) 5 double blue
                 */
                scene2 = Scene2.getInstance();
                scene2.start();
                break;
            case 6:
                showScene3();
                break;
            case 8:
                scene4 = Scene4.getInstance();
                scene4.start();
                break;
        }
    }

    /**
     * wilds and scatter
     */
    void showScene1() {


        System.out.println();
    }


    /**
     * SCATTER
     */
    void showScene3() {
        System.out.println();
    }

    /**
     * TRACK FEATURE
     */
    void showScene4() {
        System.out.println();
    }

/*    void hideScenes() {
        System.out.println();
    }*/

    private String gameSatet = "";
    private MovieView paytable_effect;
    private MovieView logo_start;
    private MovieView text_1;
    private MovieView text_2;
    private MovieView logo_end;
    private MovieView movie;

    private StopPlayVideoListener stopPlayVideoListener;

    private Timeout paytableEffectTimeout;

    private List<MovieView> movieList = new ArrayList<>();
    private static int bigTime = 30000;
    private Timeout startPlayScreenSaver;
    private Scene1 scene1;
    private Scene2 scene2;
    private Scene4 scene4;


    class DebugClass implements TimeoutCallback {
        @Override
        public void onTimeout() {
            new Timeout(1000, new DebugClass(), true);

        }
    }

    void temp() {

        new Timeout(3000, () -> {
        }, true);

    }
}


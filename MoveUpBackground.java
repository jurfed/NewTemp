package com.octavianonline.games.queenCleopatra.action;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.framework.animation.TweenViewAnimationData;
import com.atsisa.gox.framework.command.HideScreenCommand;
import com.atsisa.gox.framework.infrastructure.IViewManager;
import com.atsisa.gox.framework.resource.IImageReference;
import com.atsisa.gox.framework.utility.Timeout;
import com.atsisa.gox.framework.utility.TimeoutCallback;
import com.atsisa.gox.framework.view.ImageView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.framework.view.ViewGroup;
import com.atsisa.gox.games.octavian.core.view.particle.AOctavianParticleView;
import com.atsisa.gox.games.octavian.core.view.particle.OctavianParticleView;
import com.atsisa.gox.reels.view.AbstractSymbol;
import com.atsisa.gox.reels.view.ReelGroupView;
import com.gwtent.reflection.client.annotations.Reflect_Mini;

import java.util.ArrayList;
import java.util.List;

/**
 * Class - action for move up background panel when the free games ends
 */
@Reflect_Mini
public class MoveUpBackground extends Action {

    //The Ids of screens for changing their depth
    private final String LAYOUT_ID_FOR_BGR_FREE_GAMES = "payTableBgrScreen";
    private final String LAYOUT_ID_FOR_BGR_PANEL = "payTableScreen";
    private final String ID_FOR_BASE_GANE_SCREEN = "baseGameScreen";
    private final String ID_FOR_BGR_SCREEN = "bgrScreen";
    private final String ID_FOR_WIN_LINES_SCREEN = "winLinesScreen";
//    private final String ID_FOR_BUTTON_SCREEN_TEMP = "buttonscreen_tmp";

    //image IDs
    private final String ID_FOR_BGR_IMAGE = "bgr_fg";
    private final String ID_FOR_BGR_FREE_GAMES_IMAGE = "center_mg";
    private final String ID_FOR_REEL_BG = "reel_bg";
    private final String ID_FOR_REEL_BG_FREE_GAMES = "reel_bg_free_game";
    private final String ID_FOR_REEL_BG_GROUND = "reel_bg_ground";

    private final int NUMBERS_OF_REELS = 5;

    private AbstractSymbol abstractSymbol;

    private ReelGroupView reelGroupView;

    private final String LAYOUT_BASE_GAME_SCREEN_ID = "baseGameScreen";
    private final String REEL_GROUP_VIEW = "reelGroupView";

    private final String TOMB_SYMBOL = "Tomb";
    /**
     * Panel for move up at the end of the free games mode
     */
    private View freeGamesImage;

    /**
     * Paytable background for free games mode
     */
    private View bgrImage;

    /**
     * flag indicating whether the action is completed
     */
    private static boolean actionIsFinish = false;

    private final static int initialYPosition = 990;
    private final int stepForMoving = 30;
    private final int timeoutForMoving = 10;
    private final float stepForTransparency = 0.02f;

//    private List<OctavianParticleView> particles = new ArrayList<>();

    public static boolean getActionIsFinish() {
        return actionIsFinish;
    }

    public static void setActionIsFinish(boolean isFinish) {
        actionIsFinish = isFinish;
    }

    public static int getinitialYPosition() {
        return initialYPosition;
    }

    /**
     * Id for cropped Scatter image
     */
    private final String SCATTER_CROPPED_SIZE = "scatter_fix1";

    /**
     * Changes the depth of the screens and calls the method of moving the panel up
     */
    @Override
    protected void execute() {
/*        particles = new ArrayList<>();
        Iterable<View> views=((ViewGroup)GameEngine.current().getViewManager().findViewById("payTableScreen","Mega")).getChildren();
        views.forEach(view->{
            if(view instanceof OctavianParticleView){
                particles.add((OctavianParticleView) view);
                ((AOctavianParticleView) view).redraw();
                ((AOctavianParticleView) view).setActiveGravityTimeLines(true);
                ((AOctavianParticleView) view).setActiveVelocityTimeLines(true);
                ((OctavianParticleView) view).start();
            }
        });*/

//        particles = ((ViewGroup)GameEngine.current().getViewManager().findViewById("payTableScreen","Mega")).getChildren();

        bgrImage = GameEngine.current().getViewManager().findViewById(LAYOUT_ID_FOR_BGR_FREE_GAMES, ID_FOR_BGR_IMAGE);
        freeGamesImage = GameEngine.current().getViewManager().findViewById(LAYOUT_ID_FOR_BGR_PANEL, ID_FOR_BGR_FREE_GAMES_IMAGE);


//        particles.forEach(view->view.start());

/*        GameEngine.current().getViewManager().findViewById("payTableBgrScreen","frame_bottom_right").setVisible(true);
        GameEngine.current().getViewManager().findViewById("payTableBgrScreen","frame_bottom_left").setVisible(true);*/

        if (bgrImage.isVisible()) {

            scatterFix();

            if (freeGamesImage.getY() != initialYPosition) {
                setYandAlpha();
            }
            freeGamesImage.setVisible(true);
            new Timeout(0, new MoveTimer(this), false).start();
            IViewManager viewManager = GameEngine.current().getViewManager();
            ViewGroup stage = viewManager.getStage();
            stage.addChild(viewManager.getLayout(ID_FOR_REEL_BG).getRootView());
            stage.addChild(viewManager.getLayout(ID_FOR_REEL_BG_FREE_GAMES).getRootView());
            stage.addChild(viewManager.getLayout(ID_FOR_REEL_BG_GROUND).getRootView());
            stage.addChild(viewManager.getLayout(ID_FOR_BASE_GANE_SCREEN).getRootView());
            stage.addChild(viewManager.getLayout(ID_FOR_BGR_SCREEN).getRootView());

            stage.addChild(viewManager.getLayout(LAYOUT_BASE_GAME_SCREEN_ID).getRootView());
            stage.addChild(viewManager.getLayout(ID_FOR_WIN_LINES_SCREEN).getRootView());
            stage.addChild(viewManager.getLayout("freeGamesBannerScreen").getRootView());
            stage.addChild(viewManager.getLayout("wonPanelScreen").getRootView());
//            stage.addChild(viewManager.getLayout(ID_FOR_BUTTON_SCREEN_TEMP).getRootView());
        }
    }

    /**
     * Timer for moving the panel up and for its transparency
     */
    class MoveTimer implements TimeoutCallback {
        MoveUpBackground moveUpBackground;

        public MoveTimer(MoveUpBackground moveUpBackground) {
            this.moveUpBackground = moveUpBackground;
        }

        @Override
        public void onTimeout() {





            if (freeGamesImage.isVisible()) {
                if (freeGamesImage.getY() > stepForMoving) {//the panel moves
                    IViewManager viewManager = GameEngine.current().getViewManager();
                    ViewGroup stage = viewManager.getStage();
                    stage.addChild(viewManager.getLayout(LAYOUT_BASE_GAME_SCREEN_ID).getRootView());
                    stage.addChild(viewManager.getLayout(ID_FOR_WIN_LINES_SCREEN).getRootView());
                    stage.addChild(viewManager.getLayout("freeGamesBannerScreen").getRootView());
                    stage.addChild(viewManager.getLayout("wonPanelScreen").getRootView());
                    freeGamesImage.setY(freeGamesImage.getY() - stepForMoving);
                    new Timeout(timeoutForMoving, new MoveTimer(moveUpBackground), false).start();
                } else if (freeGamesImage.getAlpha() > stepForTransparency) {//the panel becomes transparent
                    if (!actionIsFinish) {
                        actionIsFinish = true;
                        changeScreensBack();
                        if (!moveUpBackground.isFinished()) {
                            finish();
                            freeGamesImage.setVisible(true);
                        }

                    }
                    freeGamesImage.setY(0);
                    if (bgrImage.isVisible()) {
                        bgrImage.setVisible(false);
                    }
                    freeGamesImage.setAlpha(freeGamesImage.getAlpha() - stepForTransparency);
                    new Timeout(timeoutForMoving, new MoveTimer(moveUpBackground), false).start();
                } else {
                    TweenViewAnimationData hideViewAnimationData = new TweenViewAnimationData();
                    hideViewAnimationData.setDestinationAlpha(0f);
                    hideViewAnimationData.setDelay(1000);
                    hideViewAnimationData.setTimeSpan(500);
                    GameEngine.current().getEventBus().post(new HideScreenCommand("freeGamesBannerScreen", hideViewAnimationData));
//                    stage.addChild(viewManager.getLayout("freeGamesBannerScreen").getRootView());
                    changeScreensBack();
                }
            }
        }
    }

    @Override
    protected void terminate() {
        if (!actionIsFinish) {
            actionIsFinish = true;
            changeScreensBack();
        }
    }

    /**
     * Return the depth of the screens to their original state
     */
    private void changeScreensBack() {
        freeGamesImage.setVisible(false);
        setYandAlpha();
        IViewManager viewManager = GameEngine.current().getViewManager();
        ViewGroup stage = viewManager.getStage();
//        stage.addChild(viewManager.getLayout(ID_FOR_BUTTON_SCREEN_TEMP).getRootView());
        stage.addChild(viewManager.getLayout(LAYOUT_ID_FOR_BGR_FREE_GAMES).getRootView());
        stage.addChild(viewManager.getLayout(LAYOUT_ID_FOR_BGR_PANEL).getRootView());
        stage.addChild(viewManager.getLayout(LAYOUT_BASE_GAME_SCREEN_ID).getRootView());

        stage.addChild(viewManager.getLayout(ID_FOR_WIN_LINES_SCREEN).getRootView());
        stage.addChild(viewManager.getLayout("freeGamesBannerScreen").getRootView());
        stage.addChild(viewManager.getLayout("wonPanelScreen").getRootView());
    }

    /**
     * Return the panel to its original state
     */
    private void setYandAlpha() {
        freeGamesImage.setAlpha(1);
        freeGamesImage.setY(initialYPosition);
    }


    //Set the cropped Scatter - symbol on the first line
    public void scatterFix() {
        reelGroupView = GameEngine.current().getViewManager().findViewById(LAYOUT_BASE_GAME_SCREEN_ID, REEL_GROUP_VIEW);
        for (int i = 0; i < NUMBERS_OF_REELS; i++) {
            abstractSymbol = reelGroupView.getReel(i).getDisplayedSymbol(0);
            if (abstractSymbol.getName().equals(TOMB_SYMBOL)) {
                for (View symbolView : abstractSymbol.getChildren()) {

                    if (symbolView instanceof ImageView) {
                        IImageReference imageReference = GameEngine.current().getResourceManager().getResource(SCATTER_CROPPED_SIZE);
                        ((ImageView) symbolView).setImage(imageReference);
                    }
                }
            }
        }
    }

}
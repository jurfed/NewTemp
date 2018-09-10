package com.octavianonline.games.queenCleopatra.action;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.framework.infrastructure.IViewManager;
import com.atsisa.gox.framework.view.ViewGroup;
import com.atsisa.gox.reels.view.AbstractSymbol;
import com.atsisa.gox.reels.view.ReelGroupView;
import com.gwtent.reflection.client.annotations.Reflect_Mini;


/**
 * To set the depth of symbols to the initial state: scatter=1, any other symbol=0
 * <p>
 * Depth of symbols versus activity and type:
 * 0 - not active simple symbols
 * 1 - not active scatter symbols
 * 1 - active simple symbols not on the current win line
 * 2 - active scatter symbols not on the current win line
 * 3 - active symbols on the current win line
 */
@Reflect_Mini
public class ReturnOriginalSymbolDepth extends Action {

    private final int NUMBERS_OF_REELS = 5;
    private final int NUMBERS_OF_CELLS = 3;

    private final String LAYOUT_BASE_GAME_SCREEN_ID = "baseGameScreen";
    private final String REEL_GROUP_VIEW = "reelGroupView";

    /**
     * For getting Reel Group
     */
    private ReelGroupView reelGroupView;

    /**
     * For getting symbol on the reel
     */
    private AbstractSymbol abstractSymbol;

    private final String TOMB_SYMBOL = "Tomb";

    @Override
    protected void execute() {
        reelGroupView = GameEngine.current().getViewManager().findViewById(LAYOUT_BASE_GAME_SCREEN_ID, REEL_GROUP_VIEW);

        //Getting a list of reel cell object
        for (int i = 0; i < NUMBERS_OF_REELS; i++) {
            for (int j = 0; j < NUMBERS_OF_CELLS; j++) {
                abstractSymbol = reelGroupView.getReel(i).getDisplayedSymbol(j);
                if (abstractSymbol.getName().equals(TOMB_SYMBOL)) {
                    abstractSymbol.setDepth(1);
                } else {
                    abstractSymbol.setDepth(0);
                }
            }
        }
       // WinAreaAnimation winAreaAnimation = new WinAreaAnimation();
        reelGroupView.redraw();

        IViewManager viewManager = GameEngine.current().getViewManager();
        ViewGroup stage = viewManager.getStage();

        stage.addChild(viewManager.getLayout("universalBigWinScreen").getRootView());
        stage.addChild(viewManager.getLayout("payTableBgrScreen").getRootView());
        stage.addChild(viewManager.getLayout("payTableScreen").getRootView());
        stage.addChild(viewManager.getLayout("particlesScreen").getRootView());



        finish();
    }
}

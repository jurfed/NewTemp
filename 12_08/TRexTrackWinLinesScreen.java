package com.atsisa.gox.games.trextrack.screen;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.animation.IAnimationFactory;
import com.atsisa.gox.framework.eventbus.IEventBus;
import com.atsisa.gox.framework.eventbus.NextObserver;
import com.atsisa.gox.framework.eventbus.annotation.Subscribe;
import com.atsisa.gox.framework.infrastructure.IViewManager;
import com.atsisa.gox.framework.rendering.IRenderer;
import com.atsisa.gox.framework.screen.model.ScreenModel;
import com.atsisa.gox.framework.utility.IMutator;
import com.atsisa.gox.framework.utility.Iterables;
import com.atsisa.gox.framework.utility.logger.ILogger;
import com.atsisa.gox.framework.view.KeyframeAnimationView;
import com.atsisa.gox.framework.view.LineShapeView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.games.trextrack.TRexTrackWildHitChecker;
import com.atsisa.gox.games.trextrack.event.*;
import com.atsisa.gox.games.trextrack.screen.basegamescreen.TRexTrackBaseGameScreen;
import com.atsisa.gox.games.trextrack.screen.model.BaseGameScreenModel;
import com.atsisa.gox.games.trextrack.screen.model.WinLinesScreenModel;
import com.atsisa.gox.games.trextrack.view.LineSymbolWinAnimation;
import com.atsisa.gox.reels.AbstractReelGame;
import com.atsisa.gox.reels.ILinesModel;
import com.atsisa.gox.reels.IWinLineInfo;
import com.atsisa.gox.reels.event.BetModelChangedEvent;
import com.atsisa.gox.reels.event.LinesModelChangedEvent;
import com.atsisa.gox.reels.model.IValueFormatter;
import com.atsisa.gox.reels.screen.WinLinesScreen;
import com.atsisa.gox.reels.view.AbstractReel;
import com.atsisa.gox.reels.view.AbstractSymbol;
import com.atsisa.gox.reels.view.ReelGroupView;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

public class TRexTrackWinLinesScreen extends WinLinesScreen {
    private static Map<String, Set<Integer>> symbolsWinCounts;

    /**
     * Initializes a new instance of the {@link WinLinesScreen} class.
     *
     * @param layoutId          layout identifier
     * @param model             {@link ScreenModel}
     * @param renderer          {@link IRenderer}
     * @param viewManager       {@link IViewManager}
     * @param animationFactory  {@link IAnimationFactory}
     * @param logger            {@link ILogger}
     * @param eventBus          {@link IEventBus}
     * @param linesModelMutator the lines model mutator
     * @param creditsFormatter  the credits formatter
     */
    @Inject
    public TRexTrackWinLinesScreen(@Named(LAYOUT_ID_PROPERTY) String layoutId, WinLinesScreenModel model, IRenderer renderer, IViewManager viewManager, IAnimationFactory animationFactory, ILogger logger, IEventBus eventBus, IMutator<ILinesModel> linesModelMutator, IValueFormatter creditsFormatter) {
        super(layoutId, model, renderer, viewManager, animationFactory, logger, eventBus, linesModelMutator, creditsFormatter);
        this.linesModelMutator = linesModelMutator;
        eventBus.register(new TRexTrackWinLinesScreen.LinesModelChangedEventObserver(), LinesModelChangedEvent.class);

    }

    public static Map<String, Set<Integer>> getSymbolsWinCounts() {
        return symbolsWinCounts;
    }


    public static void setSymbolsWinCounts(Map<String, Set<Integer>> symbolsWinCounts) {
        TRexTrackWinLinesScreen.symbolsWinCounts = symbolsWinCounts;
    }

    public static final String LAYOUT_ID_PROPERTY = "TRexTrackWinLinesScreenLayoutId";
//    private Map<String, String> winSymbols;

    /**
     * The lines model mutator.
     */
    private IMutator<ILinesModel> linesModelMutator;

    /**
     * The lines model.
     */
    private ILinesModel linesModel;

    private List<LineSymbolWinAnimation> lineSymbolWinAnimations;

    private List<LineShapeView> lineShapeViewList = new ArrayList<>();

    private int betIndex;

    List linePositions;

//    /**
//     * Initializes a new instance of the {@link WinLinesScreen} class.
//     *
//     * @param layoutId          layout identifier
//     * @param model             {@link ScreenModel}
//     * @param renderer          {@link IRenderer}
//     * @param viewManager       {@link IViewManager}
//     * @param animationFactory  {@link IAnimationFactory}
//     * @param logger            {@link ILogger}
//     * @param eventBus          {@link IEventBus}
//     * @param linesModelMutator the lines model mutator
//     */
//    @Inject
//    public TRexTrackWinLinesScreen(@Named(LAYOUT_ID_PROPERTY) String layoutId, WinLinesScreenModel model, IRenderer renderer, IViewManager viewManager, IAnimationFactory animationFactory, ILogger logger, IEventBus eventBus, IMutator<ILinesModel> linesModelMutator) {
//        super(layoutId, model, renderer, viewManager, animationFactory, logger, eventBus, linesModelMutator);
//        this.linesModelMutator = linesModelMutator;
//        eventBus.register(new TRexTrackWinLinesScreen.LinesModelChangedEventObserver(), LinesModelChangedEvent.class);
//
//    }

    @Override
    protected void registerEvents() {
        super.registerEvents();
        getEventBus().register(new TRexTrackWinLinesScreen.CleanCollectionWithWinSymbolsObserver(), CleanCollectionWithWinSymbols.class);
        getEventBus().register(new ChangeAnimationSpeed(), WinAnimationSpeed.class);
        getEventBus().register(new ShowAllWinLines(), BetModelChangedEvent.class);
        getEventBus().register(new HideAllWinLines(), HideWinLinesEvent.class);
        getEventBus().register(new PlayDinoWinSoundListener(), PlayDinoWinSoundEvent.class);

        betIndex = ((AbstractReelGame) GameEngine.current().getGame()).getBetModelProvider().getBetModel().getSelectedBetIndex();
    }

    @Override
    protected void afterActivated() {
        super.afterActivated();

        lineShapeViewList.addAll(findViewByType(LineShapeView.class));


    }

    private class LinesModelChangedEventObserver extends NextObserver<LinesModelChangedEvent> {

        @Override
        public void onNext(LinesModelChangedEvent linesModelChangedEvent) {
            handleLinesModelChanged(linesModelChangedEvent);
        }
    }

    /**
     * Start the mechanism of symbol replacement on winning lines
     *
     * @param linesModelChangedEvent
     */
    private void handleLinesModelChanged(LinesModelChangedEvent linesModelChangedEvent) {
        if (linesModelChangedEvent.getChangeType() == LinesModelChangedEvent.CURRENT_WINNING_LINE) {

            if (linesModelChangedEvent.getLinesModel().getCurrentWinningLine().isPresent()) {

                linePositions = Iterables.toList(linesModelChangedEvent.getLinesModel().getCurrentWinningLine().get().getPositions());

                LineSymbolWinAnimation.refreshActiveFrames(getWinLineSymbolsPositions(linePositions));
                if (((WinLinesScreenModel) getModel()).isCanAnalize()) {
                    ((WinLinesScreenModel) getModel()).setCanAnalize(false);

                    setTrackKeyFrameAnimation();

                    Iterable<? extends IWinLineInfo> winLines = linesModelChangedEvent.getLinesModel().getWinningLines();
                    analyzeWinLines(winLines);
                    formWinSymbolsCounts(winLines);
                }

                for (LineSymbolWinAnimation lineSymbol : lineSymbolWinAnimations) {

                    if (lineSymbol != null) {
                        lineSymbol.setWild2Symbol(linesModelChangedEvent.getLinesModel().getCurrentWinningLine());
                    }
                }

            }
        }
    }

    /**
     * for start key frame animation under the track symbols
     */
    private void setTrackKeyFrameAnimation() {
        int symbolId = TRexTrackWildHitChecker.getWinTrackSymbol();//get random symbol id for replace track symbol
        //send the command for start playing key frame animation under the track symbols
//        GameEngine.current().getEventBus().post(new ChangeTagStateEvent(BaseGameScreenModel.getKeyFrameViews().get(symbolId)));
        //sets key frame animation? than is under the track symbols for the wining lines
        Map<String, String> trackSkin = ((WinLinesScreenModel) getModel()).getSymbolsSkin();
        trackSkin.put("TrackR", BaseGameScreenModel.getKeyFrameViews().get(symbolId));//key Frame animation for track symbols: pink, blue, ...
        trackSkin.put("Track", BaseGameScreenModel.getKeyFrameViews().get(symbolId));//key Frame animation for track symbols: pink, blue, ...
    }

    private void formWinSymbolsCounts(Iterable<? extends IWinLineInfo> winLines) {
        symbolsWinCounts = new HashMap<>();

        for (IWinLineInfo winLineInfo : winLines) {
            List linePositions = Iterables.toList(winLineInfo.getPositions());
            int reelNumber = 1;

            int simpleSymbolCount = 0;
            int wildSymbolCount = 0;
            String normalSymbolName = "";
            String wildSymbolName = "Wild";

            String firstTrackSymbol;
//            firstTrackSymbol = winSymbols.get(1 + "" + (((int) linePositions.get(0)+1)));
            firstTrackSymbol = TRexTrackBaseGameScreen.getAnimationMap().get(1 + "" + (((int) linePositions.get(0) + 1)));
            if (firstTrackSymbol != null && (firstTrackSymbol.equals("Track") || firstTrackSymbol.equals("TrackR"))) {
                firstTrackSymbol = BaseGameScreenModel.getSymbolsForCountWinTxt().get(TRexTrackWildHitChecker.getWinTrackSymbol());
            }
            for (Object linePosition : linePositions) {

                String symbol;
                if (((int) linePosition) >= 0) {
//                    symbol = winSymbols.get(reelNumber + "" + (((int) linePosition) + 1));
                    symbol = TRexTrackBaseGameScreen.getAnimationMap().get(reelNumber + "" + (((int) linePosition) + 1));
                    if (symbol != null && (symbol.equals("Track") || symbol.equals("TrackR")))
                        symbol = BaseGameScreenModel.getSymbolsForCountWinTxt().get(TRexTrackWildHitChecker.getWinTrackSymbol());
                    if (symbol != null && symbol.toString().equals(wildSymbolName)) {
                        wildSymbolCount++;
                        if (BaseGameScreenModel.getDinoSymbols().contains(firstTrackSymbol)) {
                            wildSymbolCount++;
                        }
                    } else {
                        if (symbol != null && symbol.toString().equals("Yellow2") || symbol.toString().equals("Violet2") || symbol.toString().equals("Pink2") || symbol.toString().equals("Blue2") || symbol.toString().equals("Green2")) {
                            simpleSymbolCount++;
                        }
                        simpleSymbolCount++;
                        if (symbol != null) {
                            normalSymbolName = symbol.toString();
                        }
                    }
                }
                reelNumber++;
            }

            fillSymbolSet(simpleSymbolCount + wildSymbolCount, normalSymbolName);
            if (wildSymbolCount > 0) {
                fillSymbolSet(wildSymbolCount, wildSymbolName);
            }

        }
        getEventBus().post(new PayTableAnimation(symbolsWinCounts, true));
    }

    /**
     * counting win text animation for symbols
     *
     * @param count
     * @param symbolName
     */
    private void fillSymbolSet(int count, String symbolName) {
        String symbol = symbolName.replaceAll("[0-9]", "");
        if (symbolsWinCounts.get(symbol) == null) {
            Set set = new HashSet();
            set.add(count);
            symbolsWinCounts.put(symbol, set);
        } else {
            Set<Integer> oldSetValues = symbolsWinCounts.get(symbol);
            oldSetValues.add(count);
        }
    }


    /**
     * Get the map with wining symbols positions and their names
     *
     * @param event
     */
    @Subscribe
    public void handleSomeEvent(SendWinSymbolsEvent event) {
//        winSymbols = event.getSymbolsMap();
    }

    /**
     * Replace the symbols on the winning lines and run their KeyFrameAnimationView
     *
     * @param winLines
     */
    void analyzeWinLines(Iterable<? extends IWinLineInfo> winLines) {
        Map<String, String> animationMap = TRexTrackBaseGameScreen.getAnimationMap();
        LineSymbolWinAnimation.unsubscribe();
        lineSymbolWinAnimations = new ArrayList<>();
        Map symbolsSkin = ((WinLinesScreenModel) getModel()).getSymbolsSkin();
        for (IWinLineInfo winLineInfo : winLines) {
            if (winLineInfo.getLineNumber() != 0) {
                List linePositions = Iterables.toList(winLineInfo.getPositions());
                int reelNumber = 1;

                for (int i = 0; i < linePositions.size(); i++) {
                    String symbol = "";

                    if (((int) linePositions.get(i)) >= 0) {
//                        symbol = winSymbols.get(reelNumber + "" + (((int) linePositions.get(i)) + 1));
                        symbol = TRexTrackBaseGameScreen.getAnimationMap().get(reelNumber + "" + (((int) linePositions.get(i)) + 1));
                        int frameIndex = getFrameIndex(reelNumber, ((int) linePositions.get(i)));
                        LineSymbolWinAnimation lineSymbolWinAnimation = findViewById("line" + (winLineInfo.getLineNumber()) + "" + (reelNumber));
                        lineSymbolWinAnimations.add(lineSymbolWinAnimation);
                        if (!((String) (symbolsSkin.get(symbol))).equals("scatterSymbolAnimation")) {
                            lineSymbolWinAnimation.playWinSymbols((String) (symbolsSkin.get(symbol)), frameIndex + 1);
                        }
                    }
                    //System.out.println(symbol);
                    reelNumber++;

                }
            }
        }
    }


    /**
     * form the list of positions with wining positions
     *
     * @param linePositions - current line
     * @return - list with win symbols position for current line
     */
    private ArrayList<String> getWinLineSymbolsPositions(List linePositions) {
        ArrayList positions = new ArrayList();
        int reelNumber = 1;
        for (Object reelPosition : linePositions) {
            if (((int) reelPosition) >= 0) {
                positions.add(reelNumber + "" + ((int) reelPosition + 1));
            }
            reelNumber++;
        }

        return positions;
    }

    /**
     * Define the KeyFrameAnimation number for playing animation in the wining lines
     *
     * @param reel
     * @param positionOnReel
     * @return
     */
    private int getFrameIndex(int reel, int positionOnReel) {
        ReelGroupView reelGroupView = (ReelGroupView) GameEngine.current().getViewManager().findViewById("baseGameScreen", "reelGroupView");
        AbstractReel thisReel = reelGroupView.getReel(reel - 1);
        AbstractSymbol symbol = thisReel.getDisplayedSymbol(positionOnReel);
        int frameNumber = 0;
/*        List<String> l = new ArrayList() {{
            add("IDLE");
        }};*/

        for (View symbolChild : symbol.getChildrenRaw()) {
/*            if (symbolChild.getId()!=null && symbolChild.getId().equals("22")){
                symbolChild.setTags(l);
            }*/

            if (symbolChild instanceof KeyframeAnimationView) {
                frameNumber = ((KeyframeAnimationView) symbolChild).getCurrentFrameIndex();
            }
        }

        return frameNumber;
    }

    /**
     * Reset flag for run mechanism of symbol replacement
     */
    public class CleanCollectionWithWinSymbolsObserver extends NextObserver<CleanCollectionWithWinSymbols> {

        @Override
        public void onNext(CleanCollectionWithWinSymbols cleanCollectionWithWinSymbols) {
            ((WinLinesScreenModel) getModel()).setCanAnalize(true);

        }
    }


    private class ChangeAnimationSpeed extends NextObserver<WinAnimationSpeed> {

        @Override
        public void onNext(WinAnimationSpeed winAnimationSpeed) {
            if (winAnimationSpeed.getFast()) {
                LineSymbolWinAnimation.setTimeForStep1(75);
                LineSymbolWinAnimation.setTimeForStep2(80);
                LineSymbolWinAnimation.setTimeBeforeSteps(80);
            } else {
                LineSymbolWinAnimation.setTimeForStep1(150);
                LineSymbolWinAnimation.setTimeForStep2(160);
                LineSymbolWinAnimation.setTimeBeforeSteps(160);
            }
        }
    }

    //for show custom 50 lines on the screen and set alpha 0 to the drawn lines
    private class ShowAllWinLines extends NextObserver<BetModelChangedEvent> {

        @Override
        public void onNext(BetModelChangedEvent betModelChangedEvent) {

            if (betIndex != betModelChangedEvent.getBetModel().getSelectedBetIndex()) {
                betIndex = betModelChangedEvent.getBetModel().getSelectedBetIndex();

                GameEngine.current().getViewManager().findViewById("payTableScreen", "50Lines").setVisible(true);

                lineShapeViewList.stream().forEach(line -> {
                    if (line.getAlpha() != 0) {
                        line.setAlpha(0);
                    }
                });
            }

        }
    }

    @Override
    @Subscribe
    public void handleBetModelChangedEvent(BetModelChangedEvent betModelChangedEvent) {
        //super.handleBetModelChangedEvent(betModelChangedEvent);
    }

    //for hide custom 50 lines on the screen and set alpha 1 to the drawn lines
    private class HideAllWinLines extends NextObserver<HideWinLinesEvent> {

        @Override
        public void onNext(HideWinLinesEvent hideWinLinesEvent) {
            GameEngine.current().getViewManager().findViewById("payTableScreen", "50Lines").setVisible(false);
            lineShapeViewList.stream().forEach(line -> {
                if (line.getAlpha() != 1) {
                    line.setAlpha(1);
                }
            });
        }
    }

    /**
     * For playing dino sounds when the first line presets
     */
    private class PlayDinoWinSoundListener extends NextObserver<PlayDinoWinSoundEvent> {

        @Override
        public void onNext(PlayDinoWinSoundEvent playDinoWinSoundEvent) {
            int position = (int) linePositions.get(0);
            if (position >= 0 && position < 4) {
                ReelGroupView reelGroupView = (ReelGroupView) GameEngine.current().getViewManager().findViewById("baseGameScreen", "reelGroupView");
                AbstractReel thisReel = reelGroupView.getReel(0);
                AbstractSymbol symbol = thisReel.getDisplayedSymbol(position);
                String symbolName = symbol.getName();
                if (symbolName.equals("Track") || symbolName.equals("TrackR")) {
                    int trackSymbolId = TRexTrackWildHitChecker.getWinTrackSymbol();
                    List<String> trackSymbolsList = TRexTrackWildHitChecker.getTrackSymbolsList();
                    symbolName = trackSymbolsList.get(trackSymbolId - 1);

                }
                Map<String, String> dinoWinSounds = ((WinLinesScreenModel) getModel()).getWinSymbolsSounds();
                String soundId = dinoWinSounds.get(symbolName);
                if (soundId != null) {
                    GameEngine.current().getSoundManager().play(soundId);
                }
            }


        }
    }


    @Override
    @Subscribe
    public void handleLinesModelChangedEvent(LinesModelChangedEvent linesModelChangedEvent) {
        super.handleLinesModelChangedEvent(linesModelChangedEvent);
/*        linesModel = linesModelMutator.mutate(linesModelChangedEvent.getLinesModel());
        if (linesModelChangedEvent.hasCurrentWinningLineChanged()) {
            Optional<IWinLineInfo> winLineInfo = linesModel.getCurrentWinningLine();
            if (winLineInfo.isPresent()) {
                notifyWinningLineShown(winLineInfo.get(), showWinLine(winLineInfo.get()), linesModelChangedEvent);
            } else {
                hideWinningLines();
            }
            return;
        }
        updateActiveLines(linesModelChangedEvent.hasSelectedLinesChanged());*/
    }

    /**
     * Notifies that the win line has been shown.
     * @param winLineInfo the win line info
     * @param winLine     the win line
     * @param sourceEvent the source event
     */
/*    private void notifyWinningLineShown(IWinLineInfo winLineInfo, IWinLine winLine, Object sourceEvent) {
        WinningLineShownEvent event = new WinningLineShownEvent(winLineInfo, winLine);
        event.setSourceEvent(sourceEvent);
        getEventBus().post(event);
    }

    */

    /**
     * Updates active lines according to current lines model.
     *
     * @param shouldShowLines a value indicating whether active lines should be shown
     *//*
    private void updateActiveLines(final boolean shouldShowLines) {
        if (linesModel == null || winLines.isEmpty()) {
            return;
        }
        WinLineState nextState = shouldShowLines ? WinLineState.SHOWN : WinLineState.ACTIVE;

        int selectedLines = linesModel.getSelectedLines();

        if (activeLines != selectedLines) {
            if (activeLines == 0) {
                for (int lineNumber = 1; lineNumber <= selectedLines; ++lineNumber) {
                    getWinLine(lineNumber).setState(nextState);
                }
            } else if (activeLines < selectedLines) {
                for (int lineNumber = activeLines; lineNumber <= selectedLines; ++lineNumber) {
                    getWinLine(lineNumber).setState(nextState);
                }
            } else {
                for (int lineNumber = selectedLines + 1; lineNumber <= activeLines; ++lineNumber) {
                    getWinLine(lineNumber).setState(WinLineState.INACTIVE);
                }
            }
        }

        activeLines = selectedLines;

        if (shouldShowLines ^ activeLinesShown) {
            for (int lineNumber = 1; lineNumber <= activeLines; lineNumber++) {
                getWinLine(lineNumber).setState(nextState);
            }
            activeLinesShown = shouldShowLines;
        }
    }*/
}

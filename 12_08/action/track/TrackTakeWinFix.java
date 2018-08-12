package com.atsisa.gox.games.trextrack.action.track;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.framework.view.ImageView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.framework.view.ViewGroup;
import com.atsisa.gox.games.trextrack.TRexTrackWildHitChecker;
import com.atsisa.gox.games.trextrack.screen.model.BaseGameScreenModel;
import com.atsisa.gox.reels.view.ReelGroupView;
import com.atsisa.gox.reels.view.SymbolView;

import java.util.ArrayList;

/**
 * For remove IDLE tag from TrackSymbols when user press "take win" and return it when the drum start rotation.
 * And for add IDLE tag for imageViews symbols, that under the track (violetSymbol, blueSymbol, ...) when user pressed "take win" and remove it when the drum start rotation.
 */
/*public class TrackTakeWinFix extends Action<TrackTakeWinFixData> {
    ReelGroupView reelGroupView = (ReelGroupView) GameEngine.current().getViewManager().findViewById("baseGameScreen", "reelGroupView");
    int trackSymbolValue;

    @Override
    protected void execute() {
        if (this.actionData.getRemoveIDLE()) {//when user press "take win"
            trackSymbolValue = TRexTrackWildHitChecker.getWinTrackSymbol();

            reelGroupView.getReels().forEach(reel ->
            {
                for (View symbol : reel.getChildren()) {

                    ((ViewGroup) symbol).getChildren().forEach(object -> {

                        for (View view : ((SymbolView) object).getChildren()) {

                            if (view instanceof ImageView && view.getId() != null && (view.getId().replaceAll("[0-9]", "").equals("trackSymbolR") || view.getId().replaceAll("[0-9]", "").equals("trackSymbol"))) {
                                view.removeTag("IDLE");
                            } else if (view instanceof ImageView && view.getId() != null && view.getId().replaceAll("[0-9]", "").equals(BaseGameScreenModel.getImageViews().get(trackSymbolValue))) {
                                view.removeTag(BaseGameScreenModel.getImageViews().get(trackSymbolValue));
                                view.setTags(new ArrayList() {{
                                    add("IDLE");
                                }});
                                view.setVisible(true);
                            }
                            view.redraw();
                        }
                        symbol.redraw();
                    });
                }
                reel.redraw();
            });

        } else {//when user press "spin reels"
            trackSymbolValue = TRexTrackWildHitChecker.getWinTrackSymbol();

            reelGroupView.getReels().forEach(reel ->
            {
                for (View symbol : reel.getChildren()) {

                    ((ViewGroup) symbol).getChildren().forEach(object -> {

                        for (View view : ((SymbolView) object).getChildren()) {

                            if (view instanceof ImageView && view.getId() != null && (view.getId().replaceAll("[0-9]", "").equals("trackSymbolR") || view.getId().replaceAll("[0-9]", "").equals("trackSymbol"))) {
                                view.getTags().forEach(tag->{
                                    view.removeTag(tag);
                                });
                                view.setTags(new ArrayList() {{
                                    add("IDLE");
                                }});
                            } else if (view instanceof ImageView && view.getId() != null && view.getId().replaceAll("[0-9]", "").equals(BaseGameScreenModel.getImageViews().get(trackSymbolValue))) {
                                view.getTags().forEach(tag->{
                                    view.removeTag(tag);
                                });
                                view.setTags(new ArrayList() {{
                                    add(BaseGameScreenModel.getImageViews().get(trackSymbolValue));
                                }});
                                view.setVisible(true);
                            }
                            view.redraw();
                        }
                        symbol.redraw();
                    });
                    reel.redraw();
                }
            });
        }


        finish();
    }

    @Override
    public Class<TrackTakeWinFixData> getActionDataType() {
        return TrackTakeWinFixData.class;
    }
}*/

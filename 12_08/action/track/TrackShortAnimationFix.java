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
 * Fix the flicker of the matrix
 */
/*
public class TrackShortAnimationFix extends Action<TrackShortAnimationFixData> {
    ReelGroupView reelGroupView = (ReelGroupView) GameEngine.current().getViewManager().findViewById("baseGameScreen", "reelGroupView");
    int trackSymbolValue;

    @Override
    protected void execute() {
        if (this.actionData.getShortFix()) {
            trackSymbolValue = TRexTrackWildHitChecker.getWinTrackSymbol();

            reelGroupView.getReels().forEach(reel ->
            {
                for (View symbol : reel.getChildren()) {

                    ((ViewGroup) symbol).getChildren().forEach(object -> {

                        for (View view : ((SymbolView) object).getChildren()) {

                            if (view instanceof ImageView && view.getId() != null && view.getId().replaceAll("[0-9]", "").equals(BaseGameScreenModel.getImageViews().get(trackSymbolValue))) {
//                                view.removeTag(BaseGameScreenModel.getImageViews().get(trackSymbolValue));
                                view.getTags().forEach(tag->{
                                    view.removeTag(tag);
                                });
                                view.setTags(new ArrayList() {{
                                    add("Short");
                                }});
                                view.setVisible(true);
                            }

                        }

                    });
                }
            });

        } else {//when user press "spin reels"
            trackSymbolValue = TRexTrackWildHitChecker.getWinTrackSymbol();

            reelGroupView.getReels().forEach(reel ->
            {
                for (View symbol : reel.getChildren()) {

                    ((ViewGroup) symbol).getChildren().forEach(object -> {

                        for (View view : ((SymbolView) object).getChildren()) {

                            if (view instanceof ImageView && view.getId() != null && view.getId().replaceAll("[0-9]", "").equals(BaseGameScreenModel.getImageViews().get(trackSymbolValue))) {
//                                view.removeTag("Short");
                                view.getTags().forEach(tag->{
                                    view.removeTag(tag);
                                });
                                view.setTags(new ArrayList() {{
                                    add(BaseGameScreenModel.getImageViews().get(trackSymbolValue));
                                }});
                                view.setVisible(true);
                            }

                        }

                    });
                }
            });
        }


        finish();
    }

    @Override
    public Class<TrackShortAnimationFixData> getActionDataType() {
        return TrackShortAnimationFixData.class;
    }
}
*/

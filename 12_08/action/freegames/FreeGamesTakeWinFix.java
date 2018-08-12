package com.atsisa.gox.games.trextrack.action.freegames;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.framework.view.ImageView;
import com.atsisa.gox.framework.view.View;
import com.atsisa.gox.framework.view.ViewGroup;
import com.atsisa.gox.games.trextrack.TRexTrackWildHitChecker;
import com.atsisa.gox.games.trextrack.event.specialanimation.ShowUnderTrackSymbolEvent;
import com.atsisa.gox.games.trextrack.screen.model.BaseGameScreenModel;
import com.atsisa.gox.reels.view.ReelGroupView;
import com.atsisa.gox.reels.view.SymbolView;
import com.gwtent.reflection.client.annotations.Reflect_Mini;

import java.util.ArrayList;

@Reflect_Mini
public class FreeGamesTakeWinFix extends Action<FreeGamesTakeWinFixData> {

    @Override
    protected void execute() {
//        GameEngine.current().getEventBus().post(new ShowUnderTrackSymbolEvent(this.actionData.getShowUnderTrackSymbol()));
        finish();
    }

    @Override
    public Class<FreeGamesTakeWinFixData> getActionDataType() {
        return FreeGamesTakeWinFixData.class;
    }
}

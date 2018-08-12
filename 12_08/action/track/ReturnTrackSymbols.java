package com.atsisa.gox.games.trextrack.action.track;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.games.trextrack.event.specialanimation.ReturnSymbolsEvent;

/**
 * Created by Zver on 12.08.2018.
 */
public class ReturnTrackSymbols extends Action {
    @Override
    protected void execute() {
        GameEngine.current().getEventBus().post(new ReturnSymbolsEvent());
        finish();
    }
}

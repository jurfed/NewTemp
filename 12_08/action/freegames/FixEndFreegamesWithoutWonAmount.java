package com.atsisa.gox.games.trextrack.action.freegames;

import com.atsisa.gox.framework.GameEngine;
import com.atsisa.gox.framework.action.Action;
import com.atsisa.gox.games.trextrack.TRexTrackGameLogicModule;
import com.atsisa.gox.logic.IGameLogicStateRepository;
import com.atsisa.gox.reels.AbstractReelGame;

public class FixEndFreegamesWithoutWonAmount extends Action {
    @Override
    protected void execute() {
//        TRexTrackGameLogicModule.serviceResolver.resolve(IGameLogicStateRepository.class).clearState();
        finish();
    }
}

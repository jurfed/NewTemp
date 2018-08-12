package com.atsisa.gox.games.trextrack.action;

import com.atsisa.gox.framework.action.AbstractActionModule;
import com.atsisa.gox.games.trextrack.action.collect.TransferWinCustom;
import com.atsisa.gox.games.trextrack.action.freegames.*;
import com.atsisa.gox.games.trextrack.action.lineshow.HideWinLinesAction;
import com.atsisa.gox.games.trextrack.action.lineshow.LinePause;
import com.atsisa.gox.games.trextrack.action.reelsRotationTime.SetReelsRotationTime;
import com.atsisa.gox.games.trextrack.action.sounds.CollectSound;
import com.atsisa.gox.games.trextrack.action.sounds.StartSoundSpeenReelBaseGame;
import com.atsisa.gox.games.trextrack.action.sounds.StopSoundSpeenReelBaseGame;
import com.atsisa.gox.games.trextrack.action.takewin.CustomSendTakeWinRequest;
import com.atsisa.gox.games.trextrack.action.takewin.UnLockCanTakeWin;
import com.atsisa.gox.games.trextrack.action.track.ReturnTrackSymbols;
import com.atsisa.gox.games.trextrack.action.track.StartTrackAnimation;

public class TRexTrackActionModule  extends AbstractActionModule {

    public static final String XML_NAMESPACE = "http://www.atsisa.com/gox/games/trextrack/action";

    @Override
    public String getXmlNamespace() {
        return XML_NAMESPACE;
    }

    @Override
    protected void register() {
        registerAction("StartAnalyzeWinSymbols", StartAnalyzeWinSymbolsAction.class);
        registerAction("FindStoppedSymbols", FindStoppedSymbols.class);
        registerAction("SetReelsRotationTime", SetReelsRotationTime.class);
        registerAction("StopPayTableAnimation", StopPayTableAnimation.class);
        registerAction("LinePause", LinePause.class);
        registerAction("StartSoundSpeenReelBaseGame", StartSoundSpeenReelBaseGame.class);
        registerAction("StopSoundSpeenReelBaseGame", StopSoundSpeenReelBaseGame.class);
        registerAction("SetSymbolsTransparency", SetSymbolsTransparency.class);
        registerAction("StartTrackAnimation", StartTrackAnimation.class);
        registerAction("InitCollect", InitCollectAction.class);
        registerAction("SetTagsForTrackSymbols", SetTagsForTrackSymbols.class);
/*        registerAction("TrackTakeWinFix", TrackTakeWinFix.class);
        registerAction("TrackShortAnimationFix", TrackShortAnimationFix.class);*/
        registerAction("CollectSound", CollectSound.class);
        registerAction("HideWinLinesAction", HideWinLinesAction.class);
        registerAction("EnterFreeGames", EnterFreeGames.class);
        registerAction("Temp", Temp.class);
        registerAction("Temp2", Temp2.class);
        registerAction("Temp3", Temp3.class);
        registerAction("Temp4", Temp4.class);
        registerAction("Temp5", Temp5.class);
        registerAction("ExecuteNextDependingOnLoseFreeGame", ExecuteNextDependingOnLoseFreeGame.class);
        registerAction("PlayStartVideo", PlayStartVideo.class);
        registerAction("PlayStartVideo2", PlayStartVideo2.class);
        registerAction("InitFreeGameCollect", InitFreeGameCollectAction.class);
        registerAction("TransferWinCustom", TransferWinCustom.class);
        registerAction("ExecuteNextDependingOnNoTakeWin", ExecuteNextDependingOnNoTakeWin.class);
        registerAction("ReturnOriginalSymbolDepth", ReturnOriginalSymbolDepth.class);
        registerAction("FreeGamesTakeWinFix", FreeGamesTakeWinFix.class);
        registerAction("AfterFreeGamesRetriggerSound", AfterFreeGamesRetriggerSound.class);
        registerAction("ReturnTrackSymbols", ReturnTrackSymbols.class);


        //SendTakeWinRequest FIX
        registerAction("UnLockCanTakeWin", UnLockCanTakeWin.class);
        registerAction("CustomSendTakeWinRequest", CustomSendTakeWinRequest.class);
    }
}

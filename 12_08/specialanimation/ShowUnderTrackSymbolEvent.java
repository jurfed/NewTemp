package com.atsisa.gox.games.trextrack.event.specialanimation;

public class ShowUnderTrackSymbolEvent {
    private boolean showUnderTrackSymbol;


    public ShowUnderTrackSymbolEvent(boolean showUnderTrackSymbol) {
        this.showUnderTrackSymbol = showUnderTrackSymbol;
    }


    public boolean getShowUnderTrackSymbol() {
        return showUnderTrackSymbol;
    }

    public void setShowUnderTrackSymbol(boolean showUnderTrackSymbol) {
        this.showUnderTrackSymbol = showUnderTrackSymbol;
    }
}

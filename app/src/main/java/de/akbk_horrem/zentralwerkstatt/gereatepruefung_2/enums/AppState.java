package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums;

public enum AppState {
    NO_CONNECTION,
    CONNECTED,
    LOGGED_IN;

    public String getText() {
        return toString().toLowerCase();
    }
}

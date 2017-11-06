package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums;

public enum SharedPreferenceEnum {
    SHARED_PREFERENCE,
    BENUTZER,
    HOST,
    PFAD,
    PASSWORT,
    ROOT_PASSWORT,
    SHOW_MESSAGE,
    OFFLINE_MODE,
    BARCODE;

    public String getText() {
        return toString().toLowerCase();
    }
}

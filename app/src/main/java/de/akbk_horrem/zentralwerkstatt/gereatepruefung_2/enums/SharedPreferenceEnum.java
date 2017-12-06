package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums;

/**
 * Enums für die Speicherung der SharedPreferemces
 */
public enum SharedPreferenceEnum {
    SHARED_PREFERENCE,
    BENUTZER,
    HOST,
    PFAD,
    PASSWORT,
    ROOT_PASSWORT,
    SHOW_MESSAGE,
    OFFLINE_MODE,
    DATABASE_USER;

    /**
     * @return Gibt die Name des Enums als einen kleingeschriebenen String zurück
     */
    public String getText() {
        return toString().toLowerCase();
    }
}

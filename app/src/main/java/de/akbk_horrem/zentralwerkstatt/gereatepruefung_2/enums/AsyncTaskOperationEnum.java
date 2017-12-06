package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums;

/**
 * Enums, die die DBAsyncTask-Operationen darstellen
 */
public enum AsyncTaskOperationEnum {
    CHECK_CONNECTION,
    LOGIN,
    GET_DATA,
    INSERT_DATA,
    GET_DATABASE_USER;

    /**
     * @return Gibt die Name des Enums als einen kleingeschriebenen String zurück
     */
    public String getText() {
        return toString().toLowerCase();
    }
}

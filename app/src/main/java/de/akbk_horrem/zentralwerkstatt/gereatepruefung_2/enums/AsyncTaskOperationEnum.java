package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums;

public enum AsyncTaskOperationEnum {
    CHECK_CONNECTION,
    LOGIN,
    GET_DATA,
    INSERT_DATA,
    GET_DATABASE_USER;

    public String getText() {
        return toString().toLowerCase();
    }
}

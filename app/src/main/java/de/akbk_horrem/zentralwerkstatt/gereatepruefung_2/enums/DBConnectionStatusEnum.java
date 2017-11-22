package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums;

public enum DBConnectionStatusEnum {
    CONNECTION_STATUS,
    CONNECTED,
    DATABASE_FAILED,
    CONNECTION_FAILED,
    TRANSFER_FAILED,
    LOGIN_FAILED,
    LOGIN_SUCCESS,
    SUCCESS,
    BARCODE_FAILED,
    INSERT_FAILED,
    SYNC_FAILED,
    DATABASE_USER;

    public String getText() {
        return toString().toLowerCase();
    }
}

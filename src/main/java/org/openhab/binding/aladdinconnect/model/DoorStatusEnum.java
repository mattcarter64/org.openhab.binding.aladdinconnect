package org.openhab.binding.aladdinconnect.model;

public enum DoorStatusEnum {

    DOOR_STATUS_UNKNOWN(0),
    DOOR_STATUS_OPEN(1),
    DOOR_STATUS_OPENING(2),
    DOOR_STATUS_TIMEOUT_OPEN(3),
    DOOR_STATUS_CLOSED(4),
    DOOR_STATUS_CLOSING(5),
    DOOR_STATUS_TIMEOUT_CLOSE(6),
    NOT_CONFIGURED(7),
    UNKNOWN(-1);

    private int code;

    private DoorStatusEnum(int code) {
        this.code = code;
    }

    public static DoorStatusEnum valueOf(int value) {

        for (DoorStatusEnum status : values()) {

            if (status.getCode() == value) {

                return status;
            }
        }

        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }
}

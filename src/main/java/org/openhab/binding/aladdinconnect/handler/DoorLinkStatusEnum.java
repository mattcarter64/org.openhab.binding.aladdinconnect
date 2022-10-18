package org.openhab.binding.aladdinconnect.handler;

public enum DoorLinkStatusEnum {

    Unknown(0),
    NotConfigured(1),
    Paired(2),
    Connected(3);

    private int code;

    private DoorLinkStatusEnum(int code) {
        this.code = code;
    }

    public static DoorLinkStatusEnum valueOf(int value) {

        for (DoorLinkStatusEnum status : values()) {

            if (status.getCode() == value) {

                return status;
            }
        }

        throw new IllegalArgumentException("Unknown value for DoorLinkStatusEnum: " + value);
    }

    public int getCode() {
        return code;
    }
}

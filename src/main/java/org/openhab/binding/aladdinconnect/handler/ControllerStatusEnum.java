package org.openhab.binding.aladdinconnect.handler;

public enum ControllerStatusEnum {

    Offline(0),
    Connected(1);

    private int code;

    private ControllerStatusEnum(int code) {
        this.code = code;
    }

    public static ControllerStatusEnum valueOf(int value) {

        for (ControllerStatusEnum status : values()) {

            if (status.getCode() == value) {

                return status;
            }
        }

        throw new IllegalArgumentException("Unknown value for ControllerStatusEnum: " + value);
    }

    public int getCode() {
        return code;
    }
}

package org.openhab.binding.aladdinconnect.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Door {
    private String desired_door_status_outcome;
    private String updated_at;
    private String desired_door_status;
    private int id;
    private String last_status_ts;
    private int user_id;
    private String vehicle_color;
    private int door_index;
    private int icon;
    private int link_status;
    private String door_updated_at;
    private String created_at;
    private int desired_status;
    private int status;
    private int fault;
    private int ble_strength;
    private boolean is_enabled;
    private int battery_level;
    private int device_id;
    private String name;
    private String vehicle_type;

    public String getOhId() {

        return device_id + "_" + door_index;
    }
}

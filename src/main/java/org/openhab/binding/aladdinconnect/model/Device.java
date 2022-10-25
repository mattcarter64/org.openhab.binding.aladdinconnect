package org.openhab.binding.aladdinconnect.model;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Device {

    private boolean is_locked;
    private String legacy_rid;
    private int id;
    private String legacy_id;
    private List<Door> doors;
    private String ssid;
    private String ownership;
    private int user_id;
    private int rssi;
    private String location_name;
    private String description;
    private String updated_at;
    private boolean is_expired;
    private boolean is_updating_firmware;
    private String timezone;
    private String legacy_key;
    private String model;
    private String created_at;
    private boolean is_enabled;
    private String zipcode;
    private int status;
    private String lua_version;
    private String serial;
    private int location_id;
    private int family;
    private String name;
    private String vendor;
}

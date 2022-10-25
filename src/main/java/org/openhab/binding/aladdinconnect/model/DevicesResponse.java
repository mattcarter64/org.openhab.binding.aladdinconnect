package org.openhab.binding.aladdinconnect.model;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DevicesResponse {

    private String message;
    private List<Device> devices;
}

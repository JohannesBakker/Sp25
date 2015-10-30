package com.john.chargemanager.MicroService.Ble;

import java.util.HashMap;

/**
 * Created by dev on 10/30/2015.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();

    static {
        // Services

        // Serial Services
        attributes.put("00005500-D102-11E1-9B23-00025B00A5A5", "Charge Serial Service");                // UUID_SERIAL_SERVICE
        attributes.put("00005501-D102-11E1-9B23-00025B00A5A5 ", "Charge Serial Data Transfer Service"); // UUID_SERIAL_DATA_TRANSFER

        // Battery Control Services
        attributes.put("00002016-d102-11e1-9b23-00025b00a5a5", "Battery Control Service");                  // BATTERY_CONTROL_SERVICE_UUID
        attributes.put("00002013-d102-11e1-9b23-00025b00a5a5", "Battery Control Discharge USB1 Service");   // BATTERY_CONTROL_DISCHG_USB1_UUID
        attributes.put("00002018-d102-11e1-9b23-00025b00a5a5", "Battery Control Discharge USB2 Service");   // BATTERY_CONTROL_DISCHG_USB2_UUID
        attributes.put("00002014-d102-11e1-9b23-00025b00a5a5", "Battery Control Battery Charge Service");   // BATTERY_CONTROL_BAT_CHG_UUID
        attributes.put("00002011-d102-11e1-9b23-00025b00a5a5", "Battery Control Quality Service");          // BATTERY_CONTROL_QUALITY_UUID
        attributes.put("00002012-d102-11e1-9b23-00025b00a5a5", "Find Me Service");                          // FIND_ME_UUID

        // OTA Update Service
        attributes.put("00001016-d102-11e1-9b23-00025b00a5a5", "CSR OTA Update Service");           // CSR_OTA_UPDATE_SERVICE_UUID
        attributes.put("00001013-d102-11e1-9b23-00025b00a5a5", "CSR OTA Current App Service");      // CSR_OTA_CURRENT_APP_UUID
        attributes.put("00001018-d102-11e1-9b23-00025b00a5a5", "CSR OTA Read CS Block Service");    // CSR_OTA_READ_CS_BLOCK_UUID
        attributes.put("00001014-d102-11e1-9b23-00025b00a5a5", "CSR OTA Data Transfer Service");    // CSR_OTA_DATA_TRANSFER_UUID
        attributes.put("00001011-d102-11e1-9b23-00025b00a5a5", "CSR OTA Version Service");          // CSR_OTA_VERSION_UUID
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);

        return name == null? defaultName: name;
    }
}

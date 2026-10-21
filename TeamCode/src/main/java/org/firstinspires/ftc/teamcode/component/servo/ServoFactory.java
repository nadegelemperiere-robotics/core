/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Factory that turns a JSON servo entry into a concrete
   ServoEx instance. Dispatches on the "type" field
   (defaults to "single"). Returns null if the entry is
   malformed or the device is unavailable.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.servo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;
import com.seattlesolvers.solverslib.hardware.servos.ServoExGroup;

import org.firstinspires.ftc.teamcode.realtime.Logger;

public final class ServoFactory {

    static final String sTypeKey     = "type";
    static final String sHwmapKey    = "hwmap";
    static final String sInvertedKey = "inverted";
    static final String sMinKey      = "min";
    static final String sMaxKey      = "max";
    static final String sMembersKey  = "members";

    static final String sTypeSingle  = "single";
    static final String sTypeGroup   = "group";
    static final String sTypeMock    = "mock";

    private ServoFactory() {}

    public static ServoEx create(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        String type = cfg.optString(sTypeKey, sTypeSingle);
        switch (type) {
            case sTypeSingle: return buildSingle(hwMap, log, cfg, keyForLogs);
            case sTypeGroup:  return buildGroup(hwMap, log, cfg, keyForLogs);
            case sTypeMock:   return buildMock(cfg);
            default:
                log.warning("Servo '" + keyForLogs + "': unknown type '" + type + "' — skipped");
                return null;
        }
    }

    private static ServoEx buildSingle(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        if (!cfg.has(sHwmapKey)) {
            log.warning("Servo '" + keyForLogs + "' is missing required field 'hwmap' — skipped");
            return null;
        }
        try {
            String hwmap = cfg.getString(sHwmapKey);
            ServoEx servo;
            if (cfg.has(sMinKey) && cfg.has(sMaxKey)) {
                servo = new ServoEx(hwMap, hwmap, cfg.getDouble(sMinKey), cfg.getDouble(sMaxKey));
            }
            else {
                servo = new ServoEx(hwMap, hwmap);
            }
            servo.setInverted(cfg.optBoolean(sInvertedKey, false));
            return servo;
        }
        catch (RuntimeException | JSONException e) {
            log.warning("Servo '" + keyForLogs + "' instantiation failed: " + e.getMessage());
            return null;
        }
    }

    private static ServoExGroup buildGroup(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        JSONArray members = cfg.optJSONArray(sMembersKey);
        if (members == null || members.length() == 0) {
            log.warning("Servo group '" + keyForLogs + "': missing or empty 'members' — skipped");
            return null;
        }
        ServoEx leader;
        try { leader = create(hwMap, log, members.getJSONObject(0), keyForLogs + "[0]"); }
        catch (JSONException e) { return null; }
        if (leader == null) { return null; }
        ServoEx[] followers = new ServoEx[members.length() - 1];
        for (int i = 1; i < members.length(); i++) {
            try {
                ServoEx s = create(hwMap, log, members.getJSONObject(i), keyForLogs + "[" + i + "]");
                if (s == null) { return null; }
                followers[i - 1] = s;
            }
            catch (JSONException e) { return null; }
        }
        return new ServoExGroup(leader, followers);
    }

    private static ServoMock buildMock(JSONObject cfg) {
        double min = cfg.optDouble(sMinKey, 0.0);
        double max = cfg.optDouble(sMaxKey, 1.0);
        ServoMock m = new ServoMock(min, max);
        m.setInverted(cfg.optBoolean(sInvertedKey, false));
        return m;
    }
}

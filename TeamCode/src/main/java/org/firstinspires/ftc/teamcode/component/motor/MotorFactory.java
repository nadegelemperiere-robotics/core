/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Factory that turns a JSON motor entry into a concrete
   Motor instance. Dispatches on the "type" field
   (defaults to "single"). Returns null if the entry is
   malformed or the device is unavailable — callers
   handle the null without crashing.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.motor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;
import com.seattlesolvers.solverslib.hardware.motors.MotorGroup;

import org.firstinspires.ftc.teamcode.realtime.Logger;

public final class MotorFactory {

    static final String sTypeKey     = "type";
    static final String sHwmapKey    = "hwmap";
    static final String sInvertedKey = "inverted";
    static final String sMembersKey  = "members";

    static final String sTypeSingle  = "single";
    static final String sTypeGroup   = "group";
    static final String sTypeMock    = "mock";

    private MotorFactory() {}

    /**
     * @param hwMap  FTC HardwareMap (may be null in mock-only test setups)
     * @param logger logger for warnings
     * @param cfg    JSON config for one motor entry
     * @param keyForLogs human-readable identifier used in log messages
     * @return the constructed Motor, or null if construction failed
     */
    public static Motor create(HardwareMap hwMap, Logger logger, JSONObject cfg, String keyForLogs) {
        String type = cfg.optString(sTypeKey, sTypeSingle);
        switch (type) {
            case sTypeSingle: return buildSingle(hwMap, logger, cfg, keyForLogs);
            case sTypeGroup:  return buildGroup(hwMap, logger, cfg, keyForLogs);
            case sTypeMock:   return buildMock(logger, cfg, keyForLogs);
            default:
                logger.warning("Motor '" + keyForLogs + "': unknown type '" + type + "' — skipped");
                return null;
        }
    }

    private static MotorEx buildSingle(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        if (!cfg.has(sHwmapKey)) {
            log.warning("Motor '" + keyForLogs + "' is missing required field 'hwmap' — skipped");
            return null;
        }
        try {
            String hwmap = cfg.getString(sHwmapKey);
            MotorEx m = new MotorEx(hwMap, hwmap);
            m.setInverted(cfg.optBoolean(sInvertedKey, false));
            return m;
        }
        catch (RuntimeException | JSONException e) {
            log.warning("Motor '" + keyForLogs + "' instantiation failed: " + e.getMessage());
            return null;
        }
    }

    private static MotorGroup buildGroup(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        JSONArray members = cfg.optJSONArray(sMembersKey);
        if (members == null || members.length() == 0) {
            log.warning("Motor group '" + keyForLogs + "': missing or empty 'members' — skipped");
            return null;
        }
        Motor leader;
        try { leader = create(hwMap, log, members.getJSONObject(0), keyForLogs + "[0]"); }
        catch (JSONException e) { return null; }
        if (leader == null) { return null; }
        Motor[] followers = new Motor[members.length() - 1];
        for (int i = 1; i < members.length(); i++) {
            try {
                Motor m = create(hwMap, log, members.getJSONObject(i), keyForLogs + "[" + i + "]");
                if (m == null) { return null; }
                followers[i - 1] = m;
            }
            catch (JSONException e) { return null; }
        }
        return new MotorGroup(leader, followers);
    }

    private static MotorMock buildMock(Logger log, JSONObject cfg, String keyForLogs) {
        MotorMock m = new MotorMock();
        m.setInverted(cfg.optBoolean(sInvertedKey, false));
        return m;
    }
}
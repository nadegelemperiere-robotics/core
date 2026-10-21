/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Factory that turns a JSON cr-servo entry into a concrete
   CRServo instance. Dispatches on the "type" field
   (defaults to "single").
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.crservo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.hardware.motors.CRServo;
import com.seattlesolvers.solverslib.hardware.motors.CRServoGroup;

import org.firstinspires.ftc.teamcode.realtime.Logger;

public final class CRServoFactory {

    static final String sTypeKey     = "type";
    static final String sHwmapKey    = "hwmap";
    static final String sInvertedKey = "inverted";
    static final String sMembersKey  = "members";

    static final String sTypeSingle  = "single";
    static final String sTypeGroup   = "group";
    static final String sTypeMock    = "mock";

    private CRServoFactory() {}

    public static CRServo create(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        String type = cfg.optString(sTypeKey, sTypeSingle);
        switch (type) {
            case sTypeSingle: return buildSingle(hwMap, log, cfg, keyForLogs);
            case sTypeGroup:  return buildGroup(hwMap, log, cfg, keyForLogs);
            case sTypeMock:   return buildMock(cfg);
            default:
                log.warning("CR servo '" + keyForLogs + "': unknown type '" + type + "' — skipped");
                return null;
        }
    }

    private static CRServo buildSingle(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        if (!cfg.has(sHwmapKey)) {
            log.warning("CR servo '" + keyForLogs + "' is missing required field 'hwmap' — skipped");
            return null;
        }
        try {
            String hwmap = cfg.getString(sHwmapKey);
            CRServo cr = new CRServo(hwMap, hwmap);
            cr.setInverted(cfg.optBoolean(sInvertedKey, false));
            return cr;
        }
        catch (RuntimeException | JSONException e) {
            log.warning("CR servo '" + keyForLogs + "' instantiation failed: " + e.getMessage());
            return null;
        }
    }

    private static CRServoGroup buildGroup(HardwareMap hwMap, Logger log, JSONObject cfg, String keyForLogs) {
        JSONArray members = cfg.optJSONArray(sMembersKey);
        if (members == null || members.length() == 0) {
            log.warning("CR servo group '" + keyForLogs + "': missing or empty 'members' — skipped");
            return null;
        }
        CRServo leader;
        try { leader = create(hwMap, log, members.getJSONObject(0), keyForLogs + "[0]"); }
        catch (JSONException e) { return null; }
        if (leader == null) { return null; }
        CRServo[] followers = new CRServo[members.length() - 1];
        for (int i = 1; i < members.length(); i++) {
            try {
                CRServo c = create(hwMap, log, members.getJSONObject(i), keyForLogs + "[" + i + "]");
                if (c == null) { return null; }
                followers[i - 1] = c;
            }
            catch (JSONException e) { return null; }
        }
        return new CRServoGroup(leader, followers);
    }

    private static CRServoMock buildMock(JSONObject cfg) {
        CRServoMock m = new CRServoMock();
        m.setInverted(cfg.optBoolean(sInvertedKey, false));
        return m;
    }
}

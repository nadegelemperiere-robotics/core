/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Hardware registry: parses the global `hardware` section of
   the configuration, instantiates SolversLib actuator wrappers
   from the FTC HardwareMap, and caches the instances so that
   multiple subsystems referencing the same logical actuator
   share a single underlying object.

   Cardinality is transparent: a logical motor key can be backed
   by a single physical motor (MotorEx) or a group (MotorGroup).
   Consumers always see the SolversLib base type (Motor / ServoEx /
   CRServo) and call set(power) on it without knowing whether it
   drives one or several physical actuators.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.hardware;

/* System includes */
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/* Json includes */
import org.json.JSONException;
import org.json.JSONObject;

/* FTC Controller includes */
import com.qualcomm.robotcore.hardware.HardwareMap;

/* SolversLib includes */
import com.seattlesolvers.solverslib.hardware.motors.CRServo;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

/* Core includes */
import org.firstinspires.ftc.teamcode.component.crservo.CRServoFactory;
import org.firstinspires.ftc.teamcode.component.motor.MotorFactory;
import org.firstinspires.ftc.teamcode.component.servo.ServoFactory;
import org.firstinspires.ftc.teamcode.configuration.ConfigurableItem;
import org.firstinspires.ftc.teamcode.realtime.Logger;

public class Hardware implements ConfigurableItem {

    static final String sMotorsKey   = "motors";
    static final String sServosKey   = "servos";
    static final String sCRServosKey = "cr-servos";

    static final String sHwmapKey    = "hwmap";

    final HardwareMap                mHwMap;
    final Logger                     mLogger;

    /* Resolved actuators, keyed by their logical name. A value is either a
       single MotorEx (object JSON) or a MotorGroup (array JSON) — consumers
       see Motor either way. Same idea for servos and CR servos. */
    final Map<String, Motor>         mMotors;
    final Map<String, ServoEx>       mServos;
    final Map<String, CRServo>       mCRServos;

    /* Reverse mapping logical key -> HardwareMap name. Needed only by
       libraries that re-instantiate devices themselves (e.g. PedroPathing).
       Package-private — escape hatch, not the regular subsystem API.
       For groups, holds the leader's hwmap. */
    final Map<String, String>        mMotorHwmap;
    final Map<String, String>        mServoHwmap;
    final Map<String, String>        mCRServoHwmap;

    JSONObject                       mRawSection;
    boolean                          mIsConfigured;

    public Hardware(HardwareMap hwMap, Logger logger) {
        mHwMap        = hwMap;
        mLogger       = logger;
        mMotors       = new LinkedHashMap<>();
        mServos       = new LinkedHashMap<>();
        mCRServos     = new LinkedHashMap<>();
        mMotorHwmap   = new LinkedHashMap<>();
        mServoHwmap   = new LinkedHashMap<>();
        mCRServoHwmap = new LinkedHashMap<>();
        mRawSection   = null;
        mIsConfigured = false;
    }

    @Override
    public void read(JSONObject reader) {

        mMotors.clear();
        mServos.clear();
        mCRServos.clear();
        mMotorHwmap.clear();
        mServoHwmap.clear();
        mCRServoHwmap.clear();
        mRawSection   = reader;
        mIsConfigured = true;

        try {
            if (reader.has(sMotorsKey))   { readMotors(reader.getJSONObject(sMotorsKey)); }
            if (reader.has(sServosKey))   { readServos(reader.getJSONObject(sServosKey)); }
            if (reader.has(sCRServosKey)) { readCRServos(reader.getJSONObject(sCRServosKey)); }
        }
        catch (JSONException e) {
            mLogger.error("Hardware section structure invalid: " + e.getMessage());
            mIsConfigured = false;
        }
    }

    /* ---------------- Motors ---------------- */

    private void readMotors(JSONObject motors) throws JSONException {
        Iterator<String> keys = motors.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject entry = motors.optJSONObject(key);
            if (entry == null) {
                mLogger.warning("Motor '" + key + "' has invalid shape (expected object) — skipped");
                continue;
            }
            Motor m = MotorFactory.create(mHwMap, mLogger, entry, key);
            if (m != null) {
                mMotors.put(key, m);
                // Track hwmap name for the Pedro escape hatch
                // (only meaningful for non-mock single motors; group/mock skip this)
                if (entry.has(sHwmapKey)) {
                    try { mMotorHwmap.put(key, entry.getString(sHwmapKey)); }
                    catch (JSONException e) { /* ignored */ }
                }
            }
        }
    }

    /* ---------------- Servos ---------------- */

    private void readServos(JSONObject servos) throws JSONException {
        Iterator<String> keys = servos.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject entry = servos.optJSONObject(key);
            if (entry == null) {
                mLogger.warning("Servo '" + key + "' has invalid shape (expected object) — skipped");
                continue;
            }
            ServoEx s = ServoFactory.create(mHwMap, mLogger, entry, key);
            if (s != null) {
                mServos.put(key, s);
                if (entry.has(sHwmapKey)) {
                    try { mServoHwmap.put(key, entry.getString(sHwmapKey)); }
                    catch (JSONException e) { /* ignored */ }
                }
            }
        }
    }

    /* ---------------- CR Servos ---------------- */

    private void readCRServos(JSONObject crServos) throws JSONException {
        Iterator<String> keys = crServos.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject entry = crServos.optJSONObject(key);
            if (entry == null) {
                mLogger.warning("CR servo '" + key + "' has invalid shape (expected object) — skipped");
                continue;
            }
            CRServo c = CRServoFactory.create(mHwMap, mLogger, entry, key);
            if (c != null) {
                mCRServos.put(key, c);
                if (entry.has(sHwmapKey)) {
                    try { mCRServoHwmap.put(key, entry.getString(sHwmapKey)); }
                    catch (JSONException e) { /* ignored */ }
                }
            }
        }
    }

    /* ---------------- ConfigurableItem boilerplate ---------------- */

    @Override
    public void write(JSONObject writer) {
        if (mRawSection == null) { return; }
        try {
            Iterator<String> keys = mRawSection.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                writer.put(k, mRawSection.get(k));
            }
        }
        catch (JSONException e) {
            mLogger.error("Hardware write failed: " + e.getMessage());
        }
    }

    @Override
    public boolean isConfigured() { return mIsConfigured; }

    /** Hardware section is read-only at runtime — no Panels-mutable state. */
    @Override
    public boolean hasChanged() { return false; }

    @Override
    public String logConfiguration(String header) {

        StringBuilder sb = new StringBuilder();

        sb.append(header).append("motors (").append(mMotors.size()).append("):\n");
        for (Map.Entry<String, Motor> e : mMotors.entrySet()) {
            sb.append(header).append("  ").append(e.getKey())
              .append(" (").append(e.getValue().getClass().getSimpleName()).append(")\n");
        }

        sb.append(header).append("servos (").append(mServos.size()).append("):\n");
        for (Map.Entry<String, ServoEx> e : mServos.entrySet()) {
            sb.append(header).append("  ").append(e.getKey())
              .append(" (").append(e.getValue().getClass().getSimpleName()).append(")\n");
        }

        sb.append(header).append("cr-servos (").append(mCRServos.size()).append("):\n");
        for (Map.Entry<String, CRServo> e : mCRServos.entrySet()) {
            sb.append(header).append("  ").append(e.getKey())
              .append(" (").append(e.getValue().getClass().getSimpleName()).append(")\n");
        }

        return sb.toString();
    }

    /* ------------------------------------------------------------
       Resolution API : called by subsystems after Configuration.read()
       Returns null if a key is not available. Returned types are the
       SolversLib base classes — could be a single device or a group
       transparently. Consumers don't know and shouldn't care.
       ------------------------------------------------------------ */

    public Motor   resolveMotor(String key)   { return mMotors.get(checkKey(key)); }
    public ServoEx resolveServo(String key)   { return mServos.get(checkKey(key)); }
    public CRServo resolveCRServo(String key) { return mCRServos.get(checkKey(key)); }

    /* ------------------------------------------------------------
       Escape hatch: hwmap name lookup. NOT for regular subsystem
       code — only libraries that re-instantiate devices themselves
       (e.g. PedroPathing) need this. Will be restricted to a
       PedroAdapter in a follow-up step. For groups, returns the
       leader's hwmap.
       ------------------------------------------------------------ */

    public String motorHwmapName(String key)   { return mMotorHwmap.get(checkKey(key)); }
    public String servoHwmapName(String key)   { return mServoHwmap.get(checkKey(key)); }
    public String crServoHwmapName(String key) { return mCRServoHwmap.get(checkKey(key)); }

    /**
     * Validates a logical key like "front-left" (the category is given by the resolve method).
     */
    private static String checkKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Hardware key is null");
        }
        return key;
    }

}
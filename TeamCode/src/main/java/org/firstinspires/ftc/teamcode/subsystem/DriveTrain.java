/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   DriveTrain subsystem. Reads its motor refs from the JSON
   config, resolves them through Hardware, and builds the
   PedroPathing Follower with the matching HardwareMap names.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.subsystem;

/* Json includes */
import org.json.JSONException;
import org.json.JSONObject;


/* Solverslib includes */
import com.seattlesolvers.solverslib.hardware.motors.Motor;

/* PedroPathing includes */
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.MecanumConfig;

/* Core includes */
import org.firstinspires.ftc.teamcode.configuration.ConfigurableItem;
import org.firstinspires.ftc.teamcode.hardware.Hardware;
import org.firstinspires.ftc.teamcode.realtime.Logger;

public class DriveTrain implements ConfigurableItem {

    static final String sTypeKey                    = "type";

    static final String sTypeMecanum             = "mecanum";

    static final String sFrontLeftWheelRef       = "front-left";
    static final String sFrontRightWheelRef      = "front-right";
    static final String sBackLeftWheelRef        = "back-left";
    static final String sBackRightWheelRef       = "back-right";

    final Logger    mLogger;
    String          mType;

    final Hardware  mHardware;
    Motor           mFrontLeft;
    Motor           mFrontRight;
    Motor           mBackLeft ;
    Motor           mBackRight;
    Follower        mFollower;
    boolean         mIsConfigured;
    boolean         mCanFollow;

    public DriveTrain(Hardware hardware, Logger logger) {
        mHardware     = hardware;
        mLogger       = logger;
        mFollower     = null;
        mIsConfigured = false;
        mCanFollow    = false;
        mFrontLeft    = null;
        mBackLeft     = null;
        mFrontRight   = null;
        mBackRight    = null;
    }

    @Override
    public void read(JSONObject reader) {

        mIsConfigured = false;
        mCanFollow    = false;
        mFollower     = null;

        try {
            mType          = reader.getString(sTypeKey);
        }
        catch (JSONException e) {
            mLogger.error("DriveTrain config missing required field: " + e.getMessage());
            return;
        }

        if (!sTypeMecanum.equals(mType)) {
            mLogger.error("DriveTrain type '" + mType + "' is not supported (only 'mecanum')");
            return;
        }

        try {
            mFrontLeft = mHardware.resolveMotor(sFrontLeftWheelRef);
            mFrontRight = mHardware.resolveMotor(sFrontRightWheelRef);
            mBackLeft = mHardware.resolveMotor(sBackLeftWheelRef);
            mBackRight = mHardware.resolveMotor(sBackRightWheelRef);
        }
        catch (Exception e) {
            mLogger.error("Motor not resolved: " + e.getMessage());
            return;
        }

        if(mFrontLeft == null) { mLogger.error("Front left motor not found "); return; }
        else if(mFrontRight == null) { mLogger.error("Front right motor not found "); return; }
        else if(mBackLeft == null) { mLogger.error("Back left motor not found "); return; }
        else if(mBackRight == null) { mLogger.error("Back right motor not found "); return; }
        else { mIsConfigured = true; }

        try {

            MecanumConfig dvtConfig = new MecanumConfig(c -> {
                c.frontLeftName.set(mFrontLeft.motor.getDeviceName());
                c.frontRightName.set(mFrontRight.motor.getDeviceName());
                c.backLeftName.set(mBackLeft.motor.getDeviceName());
                c.backRightName.set(mBackRight.motor.getDeviceName());
                c.frontLeftDirection.set(mFrontLeft.motor.getDirection());
                c.frontRightDirection.set(mFrontRight.motor.getDirection());
                c.backLeftDirection.set(mBackLeft.motor.getDirection());
                c.backRightDirection.set(mBackRight.motor.getDirection());
            });
        }
        catch(Exception e) {
            mLogger.error("DriveTrain instantiation failed: " + e.getMessage());
            return;
        }

        mCanFollow = true;

    }

    @Override
    public void write(JSONObject writer) {
        try {
            writer.put(sTypeKey,            mType);
        }
        catch (JSONException e) {
            mLogger.error("DriveTrain write failed: " + e.getMessage());
        }
    }

    @Override
    public boolean isConfigured() { return mIsConfigured; }

    /** No Panels-mutable state yet on DriveTrain. */
    @Override
    public boolean hasChanged() { return false; }

    @Override
    public String logConfiguration(String header) {
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("type=").append(mType).append("\n");
        return sb.toString();
    }

    /** True if every motor was resolved and the Follower was built. */
    public boolean canFollow() { return mCanFollow; }

    /** Returns the built Follower, or null if canFollow() is false. */
    public Follower follower() { return mFollower; }
}
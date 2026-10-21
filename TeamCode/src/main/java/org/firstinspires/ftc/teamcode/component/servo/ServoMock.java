/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   In-memory mock for SolversLib ServoEx. Used when the
   configuration sets "type": "mock" so tests and dev
   environments can run without any physical servo.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.servo;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

public class ServoMock extends ServoEx {

    private double  mPosition;
    private double  mRawPosition;
    private boolean mInverted;
    private double  mMin;
    private double  mMax;
    private double  mCachingTolerance;
    private boolean mDisabled;

    public ServoMock() {
        this(0.0, 1.0);
    }

    public ServoMock(double min, double max) {
        super();
        mMin              = min;
        mMax              = max;
        mPosition         = 0.0;
        mRawPosition      = 0.0;
        mInverted         = false;
        mCachingTolerance = 0.0;
        mDisabled         = false;
    }

    /* ── Test helpers ── */

    public void mockReset() {
        mPosition = 0; mRawPosition = 0; mDisabled = false;
    }

    /* ── ServoEx overrides ── */

    @Override
    public void set(double position) {
        if (mDisabled) { return; }
        mPosition = position;
        // Mirror ServoEx behavior: physical position = scaled into [min, max] then optionally inverted
        double raw = mMin + (mMax - mMin) * position;
        mRawPosition = mInverted ? (mMax + mMin - raw) : raw;
    }

    @Override
    public double get() { return mPosition; }

    @Override
    public double getRawPosition() { return mRawPosition; }

    @Override
    public ServoEx setInverted(boolean inverted) {
        mInverted = inverted;
        return this;
    }

    @Override
    public boolean getInverted() { return mInverted; }

    @Override
    public ServoEx setPwm(PwmControl.PwmRange range) { /* no-op */ return this; }

    @Override
    public Servo getServo() { return null; }

    @Override
    public ServoControllerEx getController() { return null; }

    @Override
    public int getPortNumber() { return -1; }

    @Override
    public ServoEx setCachingTolerance(double tolerance) {
        mCachingTolerance = tolerance;
        return this;
    }

    @Override
    public double getCachingTolerance() { return mCachingTolerance; }

    @Override
    public void disable() { mDisabled = true; }

    @Override
    public String getDeviceType() { return "ServoMock"; }
}

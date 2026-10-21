/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   In-memory mock for SolversLib Motor. Used when the
   configuration sets "type": "mock" so tests and dev
   environments can run without any physical motor.

   Stores power and encoder position internally. Most
   advanced features (PIDF tuning, velocity control) are
   no-ops — sufficient for subsystem-logic testing, not
   for PID-tuning testing.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.motor;

import com.seattlesolvers.solverslib.hardware.motors.Motor;

public class MotorMock extends Motor {

    private double  mPower;
    private boolean mInverted;
    private int     mPosition;
    private double  mVelocity;
    private boolean mDisabled;
    private int     mTargetPosition;

    private double            mDistancePerPulse;
    private double            mCpr;
    private double            mMaxRpm;
    private RunMode           mRunMode;
    private ZeroPowerBehavior mZeroPowerBehavior;
    private double            mPositionTolerance;
    private double            mPositionCoeff;
    private final double[]    mVeloCoeffs        = new double[] {0, 0, 0};
    private final double[]    mFeedforwardCoeffs = new double[] {0, 0, 0};

    public MotorMock() {
        super();
        mPower             = 0.0;
        mInverted          = false;
        mPosition          = 0;
        mVelocity          = 0.0;
        mDisabled          = false;
        mTargetPosition    = 0;
        mDistancePerPulse  = 1.0;
        mCpr               = 384.5;     // goBILDA 312rpm-ish default — arbitrary, mockable
        mMaxRpm            = 312.0;
        mRunMode           = RunMode.RawPower;
        mZeroPowerBehavior = ZeroPowerBehavior.BRAKE;
        mPositionTolerance = 10;
        mPositionCoeff     = 0;
    }

    /* Test helpers to customize the mock further */
    public void mockSetCPR(double cpr)       { mCpr = cpr; }
    public void mockSetMaxRPM(double rpm)    { mMaxRpm = rpm; }

    /* ── Test helpers : let tests inject sensor state ─── */

    public void mockSetPosition(int position) { mPosition = position; }
    public void mockSetVelocity(double v)     { mVelocity = v; }
    public void mockReset() {
        mPower = 0; mPosition = 0; mVelocity = 0; mTargetPosition = 0; mDisabled = false;
    }

    /* ── Motor overrides : route around the null `motor` field ─── */

    @Override
    public void set(double output) {
        if (mDisabled) { return; }
        mPower = mInverted ? -output : output;
    }

    @Override
    public double get() { return mPower; }

    @Override
    public double getRawPower() { return mPower; }

    @Override
    public Motor setInverted(boolean inverted) {
        mInverted = inverted;
        return this;
    }

    @Override
    public boolean getInverted() { return mInverted; }

    @Override
    public int getCurrentPosition() { return mPosition; }

    @Override
    public double getCorrectedVelocity() { return mVelocity; }

    @Override
    public void resetEncoder() { mPosition = 0; }

    @Override
    public void stopAndResetEncoder() { mPower = 0; mPosition = 0; }

    @Override
    public void setTargetPosition(int target) { mTargetPosition = target; }

    @Override
    public boolean atTargetPosition() { return mPosition == mTargetPosition; }

    @Override
    public void disable()    { mDisabled = true; mPower = 0; }

    @Override
    public void stopMotor()  { mPower = 0; }

    @Override
    public String getDeviceType() { return "MotorMock"; }

    /* ── Encoder API : parent delegates to `encoder` field which is null ── */

    @Override
    public Motor.Encoder setDistancePerPulse(double dpp) {
        mDistancePerPulse = dpp;
        return null;  // parent returns the encoder; mock has none
    }

    @Override
    public double getDistance() { return mPosition * mDistancePerPulse; }

    @Override
    public double getRate()     { return mVelocity * mDistancePerPulse; }

    @Override
    public void   setTargetDistance(double distance) {
        if (mDistancePerPulse != 0.0) {
            mTargetPosition = (int) (distance / mDistancePerPulse);
        }
    }

    /* ── Motor type / capabilities : parent reads from `motor.getMotorType()` ── */

    @Override
    public double getCPR()    { return mCpr; }

    @Override
    public double getMaxRPM() { return mMaxRpm; }

    @Override
    public Motor setRunMode(RunMode mode) {
        mRunMode = mode;
        return this;
    }

    @Override
    public Motor setZeroPowerBehavior(ZeroPowerBehavior behavior) {
        mZeroPowerBehavior = behavior;
        return this;
    }

    /* ── Velocity (protected in parent — accessible to subclasses) ── */

    @Override
    protected double getVelocity() { return mVelocity; }

    /* ── PID / feedforward : parent uses null controllers ── */

    @Override
    public void   setPositionTolerance(double t)     { mPositionTolerance = t; }

    @Override
    public void   setPositionCoefficient(double kp)  { mPositionCoeff = kp; }

    @Override
    public double getPositionCoefficient()           { return mPositionCoeff; }

    @Override
    public void   setVeloCoefficients(double kp, double ki, double kd) {
        mVeloCoeffs[0] = kp; mVeloCoeffs[1] = ki; mVeloCoeffs[2] = kd;
    }

    @Override
    public double[] getVeloCoefficients() {
        return new double[] { mVeloCoeffs[0], mVeloCoeffs[1], mVeloCoeffs[2] };
    }

    @Override
    public void   setFeedforwardCoefficients(double ks, double kv) {
        mFeedforwardCoeffs[0] = ks; mFeedforwardCoeffs[1] = kv; mFeedforwardCoeffs[2] = 0;
    }

    @Override
    public void   setFeedforwardCoefficients(double ks, double kv, double ka) {
        mFeedforwardCoeffs[0] = ks; mFeedforwardCoeffs[1] = kv; mFeedforwardCoeffs[2] = ka;
    }

    @Override
    public double[] getFeedforwardCoefficients() {
        return new double[] {
            mFeedforwardCoeffs[0], mFeedforwardCoeffs[1], mFeedforwardCoeffs[2] };
    }
}

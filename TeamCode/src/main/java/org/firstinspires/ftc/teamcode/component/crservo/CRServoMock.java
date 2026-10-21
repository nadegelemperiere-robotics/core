/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   In-memory mock for SolversLib CRServo. CRServo extends
   Motor, so this mock must override the CRServo-specific
   methods (crServo field is null) AND the Motor methods
   inherited that touch the null motor/encoder/PID state.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.crservo;

import com.seattlesolvers.solverslib.hardware.motors.CRServo;
import com.seattlesolvers.solverslib.hardware.motors.Motor;

public class CRServoMock extends CRServo {

    private double  mPower;
    private boolean mInverted;
    private boolean mDisabled;

    private RunMode           mRunMode;
    private ZeroPowerBehavior mZeroPowerBehavior;
    private double            mPositionTolerance;
    private double            mPositionCoeff;
    private final double[]    mVeloCoeffs        = new double[] {0, 0, 0};
    private final double[]    mFeedforwardCoeffs = new double[] {0, 0, 0};

    public CRServoMock() {
        super();
        mPower             = 0.0;
        mInverted          = false;
        mDisabled          = false;
        mRunMode           = RunMode.RawPower;
        mZeroPowerBehavior = ZeroPowerBehavior.BRAKE;
        mPositionTolerance = 10;
        mPositionCoeff     = 0;
    }

    /* ── Test helpers ── */

    public void mockReset() { mPower = 0; mDisabled = false; }

    /* ── CRServo overrides ── */

    @Override
    public void set(double output) {
        if (mDisabled) { return; }
        mPower = mInverted ? -output : output;
    }

    @Override
    public double getRawPower() { return mPower; }

    @Override
    public CRServo setInverted(boolean inverted) {
        mInverted = inverted;
        return this;
    }

    @Override
    public boolean getInverted() { return mInverted; }

    @Override
    public void disable()   { mDisabled = true; mPower = 0; }

    @Override
    public void stop()      { mPower = 0; }

    @Override
    public void stopMotor() { mPower = 0; }

    @Override
    public String getDeviceType() { return "CRServoMock"; }

    /* ── Motor-inherited methods : neutralise NPE on null motor/encoder ── */

    @Override
    public double get() { return mPower; }

    @Override
    public int getCurrentPosition() { return 0; }

    @Override
    public double getCorrectedVelocity() { return 0.0; }

    @Override
    public void resetEncoder() { /* CR servo has no encoder */ }

    @Override
    public void stopAndResetEncoder() { mPower = 0; }

    @Override
    public void setTargetPosition(int target) { /* no-op */ }

    @Override
    public boolean atTargetPosition() { return true; }

    @Override
    public Motor.Encoder setDistancePerPulse(double dpp) { return null; }

    @Override
    public double getDistance() { return 0.0; }

    @Override
    public double getRate()     { return 0.0; }

    @Override
    public void   setTargetDistance(double distance) { /* no-op */ }

    @Override
    public double getCPR()    { return 0.0; }

    @Override
    public double getMaxRPM() { return 0.0; }

    @Override
    public Motor setRunMode(RunMode mode) { mRunMode = mode; return this; }

    @Override
    public Motor setZeroPowerBehavior(ZeroPowerBehavior behavior) {
        mZeroPowerBehavior = behavior;
        return this;
    }

    @Override
    protected double getVelocity() { return 0.0; }

    @Override
    public void   setPositionTolerance(double t)    { mPositionTolerance = t; }

    @Override
    public void   setPositionCoefficient(double kp) { mPositionCoeff = kp; }

    @Override
    public double getPositionCoefficient()          { return mPositionCoeff; }

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

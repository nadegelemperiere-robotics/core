/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Unit tests for the DriveTrain subsystem. Focus on the
   config parsing contract:
   - isConfigured = "I understood my config"
   - canFollow    = "I have every motor I need AND the
                     Follower was built"
   These are independent: a DriveTrain can be configured
   but unable to follow if motors are missing.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.subsystem;

// JUnit
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// Mockito
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

// JSON
import org.json.JSONObject;

// FTC
import com.qualcomm.robotcore.hardware.HardwareMap;

// Core
import org.firstinspires.ftc.teamcode.hardware.Hardware;
import org.firstinspires.ftc.teamcode.realtime.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DriveTrainTest {

    Hardware    mHardware;
    HardwareMap mHwMap;
    Logger      mLogger;
    DriveTrain  mDriveTrain;

    @Before
    public void setUp() {
        mHardware   = mock(Hardware.class);
        mHwMap      = mock(HardwareMap.class);
        mLogger     = mock(Logger.class);
        mDriveTrain = new DriveTrain(mHardware, mLogger);
    }

    private JSONObject validConfig() throws Exception {
        return new JSONObject(
            "{\"type\":\"mecanum\","
          + "\"front-left\":\"fl\","
          + "\"front-right\":\"fr\","
          + "\"back-left\":\"bl\","
          + "\"back-right\":\"br\"}");
    }

    // ── isConfigured contract ────────────────────────────────────────────────

    @Test
    public void newDriveTrainIsNotConfigured() {
        assertFalse(mDriveTrain.isConfigured());
        assertFalse(mDriveTrain.canFollow());
        assertNull(mDriveTrain.follower());
    }

    @Test
    public void missingTypeKeyMakesUnconfigured() throws Exception {
        JSONObject cfg = new JSONObject("{\"front-left\":\"fl\"}");
        mDriveTrain.read(cfg);
        assertFalse(mDriveTrain.isConfigured());
    }

    @Test
    public void missingMotorRefMakesUnconfigured() throws Exception {
        JSONObject cfg = new JSONObject("{\"type\":\"mecanum\",\"front-left\":\"fl\"}");
        mDriveTrain.read(cfg);
        assertFalse(mDriveTrain.isConfigured());
    }

    @Test
    public void unsupportedTypeMakesUnconfigured() throws Exception {
        JSONObject cfg = new JSONObject(
            "{\"type\":\"swerve\","
          + "\"front-left\":\"fl\",\"front-right\":\"fr\","
          + "\"back-left\":\"bl\",\"back-right\":\"br\"}");
        mDriveTrain.read(cfg);
        assertFalse(mDriveTrain.isConfigured());
    }

    @Test
    public void validConfigWithAllMotorsAvailableIsConfigured() throws Exception {
        // All motors resolve to real hwmap names. Follower build will fail
        // (HardwareMap is a mock, no real DcMotorEx behind), but configuration
        // parsing must succeed: isConfigured=true, canFollow=false.
        when(mHardware.motorHwmapName("fl")).thenReturn("frontLeft");
        when(mHardware.motorHwmapName("fr")).thenReturn("frontRight");
        when(mHardware.motorHwmapName("bl")).thenReturn("backLeft");
        when(mHardware.motorHwmapName("br")).thenReturn("backRight");

        mDriveTrain.read(validConfig());

        assertTrue(mDriveTrain.isConfigured());
        // canFollow depends on the Follower build, which throws on a mock HwMap
        assertFalse(mDriveTrain.canFollow());
        assertNull(mDriveTrain.follower());
    }

    // ── canFollow contract (graceful degradation) ────────────────────────────

    @Test
    public void allMotorsMissingDisablesFollowing() throws Exception {
        // Hardware returns null for every motor ref by default (Mockito default for String)
        mDriveTrain.read(validConfig());

        assertTrue("config is well-formed", mDriveTrain.isConfigured());
        assertFalse("but no motors -> cannot follow", mDriveTrain.canFollow());
        assertNull(mDriveTrain.follower());
    }

    @Test
    public void partialMotorAvailabilityDisablesFollowing() throws Exception {
        // Only 3 out of 4 present
        when(mHardware.motorHwmapName("fl")).thenReturn("frontLeft");
        when(mHardware.motorHwmapName("fr")).thenReturn("frontRight");
        when(mHardware.motorHwmapName("bl")).thenReturn("backLeft");
        // br missing on purpose

        mDriveTrain.read(validConfig());

        assertTrue(mDriveTrain.isConfigured());
        assertFalse("missing one motor -> cannot follow", mDriveTrain.canFollow());
    }

    // ── re-read resets state ─────────────────────────────────────────────────

    @Test
    public void rereadResetsCanFollow() throws Exception {
        when(mHardware.motorHwmapName("fl")).thenReturn("frontLeft");
        when(mHardware.motorHwmapName("fr")).thenReturn("frontRight");
        when(mHardware.motorHwmapName("bl")).thenReturn("backLeft");
        when(mHardware.motorHwmapName("br")).thenReturn("backRight");
        mDriveTrain.read(validConfig());

        // Now re-read with a malformed config: state should fully reset
        mDriveTrain.read(new JSONObject("{}"));
        assertFalse(mDriveTrain.isConfigured());
        assertFalse(mDriveTrain.canFollow());
        assertNull(mDriveTrain.follower());
    }

    // ── write round-trips the refs ───────────────────────────────────────────

    @Test
    public void writeEchoesParsedRefs() throws Exception {
        when(mHardware.motorHwmapName("fl")).thenReturn("frontLeft");
        when(mHardware.motorHwmapName("fr")).thenReturn("frontRight");
        when(mHardware.motorHwmapName("bl")).thenReturn("backLeft");
        when(mHardware.motorHwmapName("br")).thenReturn("backRight");
        mDriveTrain.read(validConfig());

        JSONObject out = new JSONObject();
        mDriveTrain.write(out);

        assertNotNull(out.getString("type"));
        assertNotNull(out.getString("front-left"));
        assertNotNull(out.getString("front-right"));
        assertNotNull(out.getString("back-left"));
        assertNotNull(out.getString("back-right"));
    }
}
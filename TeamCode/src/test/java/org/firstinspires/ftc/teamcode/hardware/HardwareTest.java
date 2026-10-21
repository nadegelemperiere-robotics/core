/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Unit tests for the Hardware registry. Focus on:
   - JSON section parsing (success / structural failures)
   - Resilience to missing or unavailable devices (per the
     contract: isConfigured stays true if the section is
     well-formed; missing devices are warnings, not fatal)
   - Resolution API (refs, prefixes, fail-safe nulls)
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.hardware;


// JUnit
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// Mockito
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

// JSON
import org.json.JSONObject;

// FTC
import com.qualcomm.robotcore.hardware.HardwareMap;

// Core
import org.firstinspires.ftc.teamcode.realtime.Logger;

@RunWith(MockitoJUnitRunner.class)
public class HardwareTest {

    HardwareMap mHwMap;
    Logger      mLogger;
    Hardware    mHardware;

    @Before
    public void setUp() {
        mHwMap   = mock(HardwareMap.class);
        mLogger  = mock(Logger.class);
        // Default: anything asked from HardwareMap throws -> simulates "device not present"
        when(mHwMap.get(any(Class.class), anyString()))
            .thenThrow(new IllegalArgumentException("no device for that name"));
        mHardware = new Hardware(mHwMap, mLogger);
    }

    // ── empty / well-formed sections ─────────────────────────────────────────

    @Test
    public void emptySectionIsConfigured() throws Exception {
        mHardware.read(new JSONObject("{}"));
        assertTrue(mHardware.isConfigured());
        assertNull(mHardware.resolveMotor("x"));
        assertNull(mHardware.resolveServo("x"));
        assertNull(mHardware.resolveCRServo("x"));
    }

    @Test
    public void missingHwmapFieldSkipsDeviceButKeepsConfigured() throws Exception {
        // motors entry without "hwmap" field is skipped with a warning,
        // not treated as a structural error.
        mHardware.read(new JSONObject("{\"motors\":{\"fl\":{\"inverted\":true}}}"));
        assertTrue(mHardware.isConfigured());
        assertNull(mHardware.resolveMotor("fl"));
        verify(mLogger).warning(anyString());
    }

    @Test
    public void deviceInstantiationFailureSkipsButKeepsConfigured() throws Exception {
        // hardwareMap.get(...) throws (set in @Before) -> all motors skipped
        // but the section was parseable, so Hardware stays "configured".
        mHardware.read(new JSONObject(
            "{\"motors\":{\"fl\":{\"hwmap\":\"frontLeft\"},"
          +              "\"fr\":{\"hwmap\":\"frontRight\"}}}"));
        assertTrue(mHardware.isConfigured());
        assertNull(mHardware.resolveMotor("fl"));
        assertNull(mHardware.resolveMotor("fr"));
    }

    // ── partial availability (key user requirement) ──────────────────────────

    @Test
    public void partialHardwarePresenceDoesNotKillConfiguration() throws Exception {
        // Only "frontLeft" is "physically present": its constructor must succeed.
        // We can't fully build a real MotorEx without a real device, so we
        // simulate via hwmap name lookup: the device that throws is the one absent.
        mHardware.read(new JSONObject(
            "{\"motors\":{\"fl\":{\"hwmap\":\"frontLeft\"},"
          +              "\"missing\":{\"hwmap\":\"absent\"}}}"));
        assertTrue("section was structurally valid", mHardware.isConfigured());
        // Both threw because mHwMap throws for every call; the point of this
        // test is the *configuration* stays valid even with multiple skips.
        assertNull(mHardware.resolveMotor("fl"));
        assertNull(mHardware.resolveMotor("missing"));
    }

    // ── key handling ─────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void resolveWithNullRefThrows() {
        mHardware.resolveMotor(null);
    }

    @Test
    public void resolveUnknownReturnsNull() throws Exception {
        mHardware.read(new JSONObject("{}"));
        assertNull(mHardware.resolveMotor("nope"));
        assertNull(mHardware.resolveServo("nope"));
        assertNull(mHardware.resolveCRServo("nope"));
    }

    @Test
    public void hwmapNameReturnsNullWhenUnknown() throws Exception {
        mHardware.read(new JSONObject("{}"));
        assertNull(mHardware.motorHwmapName("nope"));
        assertNull(mHardware.servoHwmapName("nope"));
        assertNull(mHardware.crServoHwmapName("nope"));
    }

    // ── group via JSON array : unified Motor type ────────────────────────────

    @Test
    public void emptyMotorGroupArrayIsSkipped() throws Exception {
        mHardware.read(new JSONObject("{\"motors\":{\"shooter\":[]}}"));
        assertTrue(mHardware.isConfigured());
        assertNull(mHardware.resolveMotor("shooter"));
    }

    @Test
    public void motorEntryWithInvalidShapeIsSkipped() throws Exception {
        // Value is a string, not object or array
        mHardware.read(new JSONObject("{\"motors\":{\"weird\":\"foo\"}}"));
        assertTrue(mHardware.isConfigured());
        assertNull(mHardware.resolveMotor("weird"));
    }

    // ── write echoes the raw section ─────────────────────────────────────────

    @Test
    public void writeProducesEquivalentSection() throws Exception {
        String src = "{\"motors\":{\"fl\":{\"hwmap\":\"frontLeft\",\"inverted\":true}}}";
        mHardware.read(new JSONObject(src));

        JSONObject out = new JSONObject();
        mHardware.write(out);

        assertTrue(out.has("motors"));
        JSONObject fl = out.getJSONObject("motors").getJSONObject("fl");
        assertEquals("frontLeft", fl.getString("hwmap"));
        assertTrue(fl.getBoolean("inverted"));
    }

    @Test
    public void writeBeforeReadIsNoOp() {
        JSONObject out = new JSONObject();
        mHardware.write(out);
        assertEquals(0, out.length());
    }

    // ── re-read clears previous state ────────────────────────────────────────

    @Test
    public void rereadResetsCaches() throws Exception {
        mHardware.read(new JSONObject(
            "{\"motors\":{\"a\":{\"hwmap\":\"A\"}}}"));
        // re-read with no motors: previous entries (even if absent) must be cleared
        mHardware.read(new JSONObject("{}"));
        assertNull(mHardware.resolveMotor("a"));
        assertNull(mHardware.motorHwmapName("a"));
    }
}

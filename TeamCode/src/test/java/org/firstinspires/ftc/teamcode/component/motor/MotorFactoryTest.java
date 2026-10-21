/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Unit tests for MotorFactory. Focus on type dispatch and
   the mock path that runs without a real HardwareMap.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.component.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorGroup;

import org.firstinspires.ftc.teamcode.realtime.Logger;

@RunWith(MockitoJUnitRunner.class)
public class MotorFactoryTest {

    HardwareMap mHwMap;
    Logger      mLogger;

    @Before
    public void setUp() {
        mHwMap  = mock(HardwareMap.class);
        mLogger = mock(Logger.class);
        when(mHwMap.get(any(Class.class), anyString()))
            .thenThrow(new IllegalArgumentException("no device"));
    }

    // ── mock type : no hardware needed ────────────────────────────────────────

    @Test
    public void mockTypeProducesMotor() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"mock\"}"), "test");
        assertNotNull(m);
        assertTrue(m instanceof MotorMock);
    }

    @Test
    public void mockTypeRespectsInverted() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"mock\",\"inverted\":true}"), "test");
        assertTrue(m.getInverted());
    }

    @Test
    public void mockMotorSetGetRoundTrip() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"mock\"}"), "test");
        m.set(0.7);
        assertEquals(0.7, m.get(), 1e-9);
    }

    @Test
    public void mockMotorInvertedNegatesPower() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"mock\",\"inverted\":true}"), "test");
        m.set(0.5);
        assertEquals(-0.5, m.get(), 1e-9);
    }

    @Test
    public void mockMotorEncoderHelpers() {
        MotorMock m = new MotorMock();
        m.mockSetPosition(100);
        assertEquals(100, m.getCurrentPosition());
        m.resetEncoder();
        assertEquals(0, m.getCurrentPosition());
    }

    // ── single type : default when type omitted ───────────────────────────────

    @Test
    public void singleTypeDefaultWhenAbsent() throws Exception {
        // type omitted, hwmap present → should try single path → fails because HwMap throws
        // but the dispatch should at least pick "single" (we observe via the warning)
        Motor m = MotorFactory.create(mHwMap, mLogger,
            new JSONObject("{\"hwmap\":\"x\"}"), "test");
        assertNull(m);
        verify(mLogger).warning(anyString());
    }

    @Test
    public void singleTypeMissingHwmapIsSkipped() throws Exception {
        Motor m = MotorFactory.create(mHwMap, mLogger,
            new JSONObject("{\"type\":\"single\"}"), "test");
        assertNull(m);
        verify(mLogger).warning(anyString());
    }

    // ── group type : composed of nested entries ───────────────────────────────

    @Test
    public void groupOfMocksProducesMotorGroup() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject(
                "{\"type\":\"group\",\"members\":["
              + "  {\"type\":\"mock\"},"
              + "  {\"type\":\"mock\",\"inverted\":true}"
              + "]}"), "shooter");
        assertNotNull(m);
        assertTrue(m instanceof MotorGroup);
    }

    @Test
    public void groupMissingMembersIsSkipped() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"group\"}"), "shooter");
        assertNull(m);
    }

    @Test
    public void groupEmptyMembersIsSkipped() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"group\",\"members\":[]}"), "shooter");
        assertNull(m);
    }

    @Test
    public void groupWithFailingMemberReturnsNull() throws Exception {
        // First mock OK, second member missing hwmap (single, default) → whole group disabled
        Motor m = MotorFactory.create(mHwMap, mLogger,
            new JSONObject(
                "{\"type\":\"group\",\"members\":["
              + "  {\"type\":\"mock\"},"
              + "  {\"type\":\"single\"}"
              + "]}"), "shooter");
        assertNull(m);
    }

    // ── unknown type ──────────────────────────────────────────────────────────

    @Test
    public void unknownTypeReturnsNullWithWarning() throws Exception {
        Motor m = MotorFactory.create(null, mLogger,
            new JSONObject("{\"type\":\"swerve-pod\"}"), "test");
        assertNull(m);
        verify(mLogger).warning(anyString());
    }
}
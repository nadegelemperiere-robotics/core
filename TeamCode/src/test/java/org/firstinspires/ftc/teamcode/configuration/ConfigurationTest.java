/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Unit tests for the Configuration class. Focus on the
   I/O contract (read/write/snapshot) and the periodic
   change-detection pipeline that drives the Panels LED
   and the auto-snapshot at OpMode stop.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.configuration;

// System
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

// JUnit
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

// Mockito
import static org.mockito.Mockito.mock;
import org.mockito.junit.MockitoJUnitRunner;

// JSON
import org.json.JSONObject;

// Panels
import com.bylazar.configurables.PanelsConfigurables;

// Core
import org.firstinspires.ftc.teamcode.realtime.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {

    Configuration mConfig;
    Logger        mLogger;
    Path          mTmpDir;

    @BeforeClass
    public static void mockPanelsConfigurables() throws Exception {
        Class<?> unsafeClass    = Class.forName("sun.misc.Unsafe");
        Field    theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object   unsafe         = theUnsafeField.get(null);

        Field  instanceField = PanelsConfigurables.class.getDeclaredField("INSTANCE");
        Method staticBase    = unsafeClass.getMethod("staticFieldBase",   Field.class);
        Method staticOffset  = unsafeClass.getMethod("staticFieldOffset", Field.class);
        Method putObject     = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);

        Object base   = staticBase.invoke(unsafe, instanceField);
        long   offset = (long) staticOffset.invoke(unsafe, instanceField);
        putObject.invoke(unsafe, base, offset, mock(PanelsConfigurables.class));
    }

    @Before
    public void setUp() throws Exception {
        mLogger = mock(Logger.class);
        mConfig = new Configuration();
        mConfig.logger(mLogger);
        mTmpDir = Files.createTempDirectory("ConfigurationTest");
        Configuration.SAVE_SNAPSHOT_NOW   = false;
        Configuration.HAS_UNSAVED_CHANGES = false;
    }

    @After
    public void tearDown() throws Exception {
        deleteRecursive(mTmpDir.toFile());
    }

    /** A fake configurable item that lets the test script Panels-style mutations. */
    static class FakeItem implements ConfigurableItem {
        boolean    configured = false;
        JSONObject lastRead   = null;
        String     valueRead  = null;
        String     valueWrite = "written-value";
        boolean    nextChange = false;

        @Override public void    read(JSONObject reader)  { lastRead = reader; valueRead = reader.optString("k", null); configured = true; }
        @Override public void    write(JSONObject writer) { try { writer.put("k", valueWrite); } catch (Exception e) {} }
        @Override public boolean isConfigured()           { return configured; }
        @Override public boolean hasChanged() {
            boolean r = nextChange;
            nextChange = false;
            return r;
        }
        @Override public String  logConfiguration(String header) { return header + "fake\n"; }
    }

    /** A bare ConfigurableItem stub for tests that don't care about change detection. */
    static ConfigurableItem inertItem(final boolean configured) {
        return new ConfigurableItem() {
            @Override public void    read(JSONObject reader)              {}
            @Override public void    write(JSONObject writer)             {}
            @Override public boolean isConfigured()                       { return configured; }
            @Override public boolean hasChanged()                         { return false; }
            @Override public String  logConfiguration(String header)  { return ""; }
        };
    }

    private File writeFile(String name, String content) throws Exception {
        File f = mTmpDir.resolve(name).toFile();
        try (FileWriter w = new FileWriter(f)) { w.write(content); }
        return f;
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) { for (File c : f.listFiles()) { deleteRecursive(c); } }
        f.delete();
    }

    // ── read / isValid ───────────────────────────────────────────────────────

    @Test
    public void readMissingFileMarksInvalid() {
        mConfig.readJson(mTmpDir.resolve("does-not-exist.json").toString());
        assertFalse(mConfig.isValid());
    }

    @Test
    public void readMalformedJsonMarksInvalid() throws Exception {
        File f = writeFile("bad.json", "this is not json");
        mConfig.readJson(f.getAbsolutePath());
        assertFalse(mConfig.isValid());
    }

    @Test
    public void readValidJsonDispatchesToItem() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("foo", item);
        File f = writeFile("c.json", "{\"foo\": {\"k\": \"hello\"}}");
        mConfig.readJson(f.getAbsolutePath());
        assertTrue(mConfig.isValid());
        assertEquals("hello", item.valueRead);
    }

    @Test
    public void readNestedDotPath() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("a.b.c", item);
        File f = writeFile("n.json", "{\"a\":{\"b\":{\"c\":{\"k\":\"deep\"}}}}");
        mConfig.readJson(f.getAbsolutePath());
        assertEquals("deep", item.valueRead);
    }

    @Test
    public void readMissingKeyMarksInvalid() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("missing", item);
        File f = writeFile("c.json", "{\"foo\": {}}");
        mConfig.readJson(f.getAbsolutePath());
        assertFalse(mConfig.isValid());
    }

    @Test
    public void readItemNotConfiguredMarksInvalid() throws Exception {
        mConfig.register("foo", inertItem(false));
        File f = writeFile("c.json", "{\"foo\": {}}");
        mConfig.readJson(f.getAbsolutePath());
        assertFalse(mConfig.isValid());
    }

    // ── search ───────────────────────────────────────────────────────────────

    @Test
    public void searchExistingTopic() throws Exception {
        File f = writeFile("s.json", "{\"a\":{\"b\":{\"x\":1}}}");
        mConfig.readJson(f.getAbsolutePath());
        JSONObject result = mConfig.search("a.b");
        assertNotNull(result);
        assertEquals(1, result.getInt("x"));
    }

    @Test
    public void searchMissingReturnsNull() throws Exception {
        File f = writeFile("s.json", "{\"a\":{}}");
        mConfig.readJson(f.getAbsolutePath());
        assertNull(mConfig.search("a.b"));
    }

    // ── write ────────────────────────────────────────────────────────────────

    @Test
    public void writeProducesParseableJson() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("foo", item);
        File src = writeFile("src.json", "{\"foo\":{\"k\":\"x\"}}");
        mConfig.readJson(src.getAbsolutePath());

        File out = mTmpDir.resolve("out.json").toFile();
        mConfig.write(out.getAbsolutePath());

        assertTrue(out.exists());
        String content = new String(Files.readAllBytes(out.toPath()));
        JSONObject parsed = new JSONObject(content);
        assertEquals("written-value", parsed.getJSONObject("foo").getString("k"));
    }

    // ── periodic / change detection pipeline ─────────────────────────────────

    @Test
    public void newConfigHasNoUnsavedChanges() {
        assertFalse(mConfig.hasUnsavedChanges());
    }

    @Test
    public void updatePollsHasChangedOnEveryItem() {
        FakeItem a = new FakeItem();
        FakeItem b = new FakeItem();
        mConfig.register("a", a);
        mConfig.register("b", b);

        a.nextChange = true;
        b.nextChange = true;
        mConfig.periodic();
        // Both items' hasChanged was consumed by the poll
        assertFalse(a.nextChange);
        assertFalse(b.nextChange);
        assertTrue(mConfig.hasUnsavedChanges());
    }

    @Test
    public void itemReportingChangeLightsTheLed() {
        FakeItem a = new FakeItem();
        mConfig.register("a", a);

        a.nextChange = true;
        mConfig.periodic();
        assertTrue(mConfig.hasUnsavedChanges());
        assertTrue(Configuration.HAS_UNSAVED_CHANGES);
    }

    @Test
    public void ledStaysOnAcrossPollsUntilSnapshot() throws Exception {
        FakeItem a = new FakeItem();
        mConfig.register("a", a);
        File src = writeFile("base.json", "{\"a\":{}}");
        mConfig.readJson(src.getAbsolutePath());

        a.nextChange = true;
        mConfig.periodic();
        assertTrue("first poll lights LED", mConfig.hasUnsavedChanges());

        // Item is now clean (hasChanged consumed on last poll). LED must stay on.
        mConfig.periodic();
        assertTrue("LED is sticky between polls", mConfig.hasUnsavedChanges());
    }

    @Test
    public void snapshotResetsTheLed() throws Exception {
        FakeItem a = new FakeItem();
        mConfig.register("a", a);
        File src = writeFile("base.json", "{\"a\":{}}");
        mConfig.readJson(src.getAbsolutePath());

        a.nextChange = true;
        mConfig.periodic();
        assertTrue(mConfig.hasUnsavedChanges());

        assertNotNull(mConfig.snapshotTo(mTmpDir.toString()));
        assertFalse(mConfig.hasUnsavedChanges());
    }

    // ── snapshot ─────────────────────────────────────────────────────────────

    @Test
    public void snapshotBeforeReadReturnsNull() {
        assertNull(mConfig.snapshotTo(mTmpDir.toString()));
    }

    @Test
    public void snapshotProducesTimestampedFile() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("foo", item);
        File src = writeFile("base.json", "{\"foo\":{\"k\":\"x\"}}");
        mConfig.readJson(src.getAbsolutePath());

        String snapDir = mTmpDir.resolve("snaps").toString();
        String snap = mConfig.snapshotTo(snapDir);

        assertNotNull(snap);
        File snapFile = new File(snap);
        assertTrue(snapFile.exists());
        assertTrue(snapFile.getName().startsWith("base-"));
        assertTrue(snapFile.getName().endsWith(".json"));
    }

    @Test
    public void snapshotIfChangedIsNoOpWhenClean() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("foo", item);
        File src = writeFile("base.json", "{\"foo\":{\"k\":\"x\"}}");
        mConfig.readJson(src.getAbsolutePath());

        assertNull(mConfig.snapshotIfChanged());
    }

    // ── Panels SAVE_SNAPSHOT_NOW trigger ─────────────────────────────────────

    @Test
    public void saveSnapshotNowTriggerIsHonoredOnNextUpdate() throws Exception {
        FakeItem item = new FakeItem();
        mConfig.register("foo", item);
        File src = writeFile("base.json", "{\"foo\":{\"k\":\"x\"}}");
        mConfig.readJson(src.getAbsolutePath());
        mConfig.snapshotDirectory(mTmpDir.resolve("snaps").toString());

        Configuration.SAVE_SNAPSHOT_NOW = true;
        mConfig.periodic();
        assertFalse("trigger is consumed", Configuration.SAVE_SNAPSHOT_NOW);
        // A snapshot file must have been written
        assertTrue(new File(mTmpDir.resolve("snaps").toString()).isDirectory());
    }
}
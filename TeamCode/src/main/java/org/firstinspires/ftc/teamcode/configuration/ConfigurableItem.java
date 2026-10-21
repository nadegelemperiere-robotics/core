package org.firstinspires.ftc.teamcode.configuration;

/* Json includes */
import org.json.JSONObject;

public interface ConfigurableItem {

    /**
     * Reads configuration from a JSON object.
     *
     * @param reader JSON object containing configuration
     */
    void        read(JSONObject reader);

    /**
     * Writes configuration to a JSON object. Implementations MUST serialize
     * the current Panels-mutable state (e.g. static fields exposed via
     * @Configurable), not just the value read from disk — otherwise
     * snapshots will silently drop runtime tuning.
     *
     * @param writer JSON object to write configuration into
     */
    void        write(JSONObject writer);

    /**
     * @return true if the object was correctly configured by read()
     */
    boolean     isConfigured();

    /**
     * Reports whether the item has been mutated since the last call.
     * Polled every loop tick by Configuration.update() to drive the
     * "unsaved changes" LED and the auto-snapshot at OpMode stop.
     *
     * Destructive read: returns the current state then resets the
     * internal flag to false.
     *
     * Recommended pattern for the detection itself: implementations
     * that already have a periodic lifecycle hook (e.g. SolversLib
     * Subsystem.periodic()) should compare their cached state vs the
     * Panels-mutable static there and set an internal flag. Items
     * with no natural periodic hook can do the comparison directly
     * inside hasChanged(). The interface deliberately does NOT impose
     * a periodic() method, to avoid clashing with SolversLib Subsystem
     * for items that implement both.
     *
     * @return true if a change was detected since the last call
     */
    boolean     hasChanged();

    /**
     * @return human-readable dump of the current configuration, prefixed
     *         with header on each line
     */
    String      logConfiguration(String header);

}


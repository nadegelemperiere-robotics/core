/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Base OpMode for season code.

   Owns the standard Configuration + Hardware + Logger triplet,
   exposes registerSubsystems() for the season to hook into, and
   automatically snapshots the configuration on stop() if dirty
   so runtime tuning made via Panels is preserved without the
   user having to remember to save.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.opmodes;

/* Solverslib includes */
import com.seattlesolvers.solverslib.command.CommandOpMode;

/* Panels includes */
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.gamepad.PanelsGamepad;

/* Core includes */
import org.firstinspires.ftc.teamcode.configuration.Alliance;
import org.firstinspires.ftc.teamcode.configuration.Configuration;
import org.firstinspires.ftc.teamcode.control.GamepadCombinedEx;
import org.firstinspires.ftc.teamcode.hardware.Hardware;
import org.firstinspires.ftc.teamcode.realtime.Logger;

public abstract class CoreOpMode extends CommandOpMode {

    protected GamepadCombinedEx mGamepad1;
    protected GamepadCombinedEx mGamepad2;

    protected Configuration     mConfiguration;
    protected Hardware          mHardware;
    protected Logger            mLogger;

    /** Configuration name (without .json), loaded from /sdcard/FIRST/<name>.json. Override to use another one. */
    public String configurationName() { return "biobuzz"; }

    @Override
    public void initialize() {

        // Initialize logging
        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(), true);

        // Initialize configuration
        mConfiguration = new Configuration();
        mConfiguration.logger(mLogger);
        mConfiguration.register("logging", mLogger);

        // Initialize HW
        mHardware = new Hardware(hardwareMap, mLogger);
        mConfiguration.register("hardware", mHardware);

        initialize_content();

        // Dispatch configuration
        mConfiguration.read(configurationName());
        mConfiguration.log();

        // Initialize Gamepads
        mGamepad1 = new GamepadCombinedEx(gamepad1, PanelsGamepad.INSTANCE.getFirstManager());
        mGamepad2 = new GamepadCombinedEx(gamepad2, PanelsGamepad.INSTANCE.getSecondManager());


        mLogger.periodic();

    }

    @Override
    public void initialize_loop() {

        super.initialize_loop();

        mGamepad1.readButtons();
        mGamepad2.readButtons();

        initialize_loop_content();

        Alliance.INSTANCE.feedback(mGamepad1);
        Alliance.INSTANCE.feedback(mGamepad2);

        mLogger.periodic();
    }


    public void initialize_content() {}
    public void initialize_loop_content() {}

    @Override
    public void run() {

        super.run();

        if (mConfiguration != null) { mConfiguration.periodic(); }
    }

    @Override
    public void end() {

        super.end();

        if (mConfiguration != null) {
            String path = mConfiguration.snapshotIfChanged();
            if (path != null) {
                mLogger.info("Configuration snapshot saved to " + path);
            }
        }
    }
}
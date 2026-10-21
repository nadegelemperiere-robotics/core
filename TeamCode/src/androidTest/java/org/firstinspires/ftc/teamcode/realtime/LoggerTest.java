/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Realtime logger test
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.realtime;

/* System includes */
import android.util.Log;

/* Qualcomm includes */
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/* Panels includes */
import com.bylazar.telemetry.PanelsTelemetry;

// Core includes
import org.firstinspires.ftc.teamcode.realtime.Logger;

@TeleOp(name = "LoggerTest", group = "Test")
public class LoggerTest extends LinearOpMode {

    private enum Suite {
        NONE,
        CONSTRUCTOR,
        LEVEL_FILTER
    }

    Suite   mCurrentSuite;
    Suite   mNextSuite;

    Logger  mLogger;

    public void runOpMode() {

        telemetry.clear();
        telemetry.addLine("----- LOGGER TESTS -----");
        telemetry.update();
        PanelsTelemetry.INSTANCE.getFtcTelemetry().clear();
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("LOGGER TESTS");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().update();
        Log.i("test","LOGGER TESTS");
        mCurrentSuite = Suite.NONE;
        mNextSuite = Suite.CONSTRUCTOR;

        waitForStart();

        while (opModeIsActive()) {

            if(mNextSuite != mCurrentSuite) {
                mCurrentSuite = mNextSuite;
                this.launch(mCurrentSuite);
            }

            sleep(200);
        }

    }

    private void launch(Suite suite) {
        if(suite == Suite.CONSTRUCTOR)   { this.constructorTest(); }
        else if(suite == Suite.LEVEL_FILTER) { this.levelTest(); }
        else {
            telemetry.addLine("Unknown suite " + suite);
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("Unknown suite " + suite);
        }
    }

    private void constructorTest() {

        telemetry.addLine("--> Constructor test\n");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("--> Constructor test");
        Log.i("LoggerTest.constructorTest:0075","----> Constructor test");

        telemetry.addLine("----> Driver station only");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Driver station only");
        Log.i("LoggerTest.constructorTest:0079","----> Driver station only");
                
        mLogger = new Logger(telemetry, null, false);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error shall only appear on driver station");
            mLogger.warning(target, "Warning shall only appear on driver station");
            mLogger.metric(target, "Target","DriverStation");
            mLogger.info(target, "Info shall only appear on driver station");
            mLogger.debug(target, "Debug shall only appear on driver station");
            mLogger.verbose(target, "Trace shall only appear on driver station");
        }
        mLogger.stop();

        telemetry.addLine("----> Dashboard only");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Dashboard only");
        Log.i("LoggerTest.constructorTest:0094","----> Dashboard station only\n");

        mLogger = new Logger(null, PanelsTelemetry.INSTANCE.getTelemetry(),false);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall only appear on dashboard");
            mLogger.warning(target, "Warning shall only appear on dashboard");
            mLogger.metric(target, "Target","Dashboard");
            mLogger.info(target, "Info shall only appear on dashboard");
            mLogger.debug(target, "Debug shall only appear on dashboard");
            mLogger.verbose(target, "Trace shall only appear on dashboard");
        }
        mLogger.stop();

        telemetry.addLine("----> System only");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> System only");
        Log.i("LoggerTest.constructorTest:0109","----> System only");

        mLogger = new Logger(null, null,true);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall only appear in system");
            mLogger.warning(target, "Warning line shall only appear in system");
            mLogger.metric(target, "Target","System");
            mLogger.info(target, "Info shall only appear in system");
            mLogger.debug(target, "Debug shall only appear in system");
            mLogger.verbose(target, "Trace shall only appear in system");
        }
        mLogger.stop();

        telemetry.addLine("----> Nowhere");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Nowhere");
        Log.i("LoggerTest.constructorTest:0124","----> Nowhere");

        mLogger = new Logger(null, null, false);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall not appear");
            mLogger.warning(target, "Warning line shall not appear");
            mLogger.metric(target, "Target","None");
            mLogger.info(target, "Info line shall not appear");
            mLogger.debug(target, "Debug line shall not appear");
            mLogger.verbose(target, "Trace line shall not appear");
        }
        mLogger.stop();

        telemetry.addLine("----> Driver station, system and dashboard");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Driver station and dashboard");
        Log.i("LoggerTest.constructorTest:0139","----> Driver station, system and dashboard");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(),true);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall appear on driver station, system and dashboard");
            mLogger.warning(target, "Warning line shall appear on driver station, system and dashboard");
            mLogger.metric(target, "Target","All");
            mLogger.info(target, "Info line shall appear on driver station, system and dashboard");
            mLogger.debug(target, "Debug line shall appear on driver station, system and dashboard");
            mLogger.verbose(target, "Trace line shall appear on driver station, system and dashboard");
        }
        mLogger.stop();

        for (Logger.Target target : Logger.Target.values()) {
            mLogger.update(target);
        }

        mNextSuite = Suite.LEVEL_FILTER;

    }

    private void levelTest() {

        telemetry.addLine("--> Level filter test\n");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("--> Level filter test");
        Log.i("LoggerTest.levelTest:0164","----> Level filter test");

        // --- ERROR level : only errors shall appear ---
        telemetry.addLine("----> Level ERROR : only errors shall appear");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Level ERROR : only errors shall appear");
        Log.i("LoggerTest.levelTest:0169","----> Level ERROR : only errors shall appear");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(), true);
        mLogger.level(Logger.Level.ERROR);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target,   "ERROR   - shall appear   (level=ERROR)");
            mLogger.warning(target, "WARNING - shall NOT appear (level=ERROR)");
            mLogger.metric(target,  "METRIC", "shall NOT appear");
            mLogger.info(target,    "INFO    - shall NOT appear (level=ERROR)");
            mLogger.debug(target,   "DEBUG   - shall NOT appear (level=ERROR)");
            mLogger.verbose(target, "VERBOSE - shall NOT appear (level=ERROR)");
        }
        mLogger.stop();

        // --- WARN level : errors and warnings shall appear ---
        telemetry.addLine("----> Level WARN : errors and warnings shall appear");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Level WARN : errors and warnings shall appear");
        Log.i("LoggerTest.levelTest:0186","----> Level WARN : errors and warnings shall appear");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(), true);
        mLogger.level(Logger.Level.WARNING);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target,   "ERROR   - shall appear   (level=WARN)");
            mLogger.warning(target, "WARNING - shall appear   (level=WARN)");
            mLogger.metric(target,  "METRIC", "shall NOT appear");
            mLogger.info(target,    "INFO    - shall NOT appear (level=WARN)");
            mLogger.debug(target,   "DEBUG   - shall NOT appear (level=WARN)");
            mLogger.verbose(target, "VERBOSE - shall NOT appear (level=WARN)");
        }
        mLogger.stop();

        // --- INFO level : errors, warnings, metrics and infos shall appear ---
        telemetry.addLine("----> Level INFO : errors, warnings, metrics, infos shall appear");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Level INFO : errors, warnings, metrics, infos shall appear");
        Log.i("LoggerTest.levelTest:0203","----> Level INFO : errors, warnings, metrics, infos shall appear");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(), true);
        mLogger.level(Logger.Level.INFO);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target,   "ERROR   - shall appear   (level=INFO)");
            mLogger.warning(target, "WARNING - shall appear   (level=INFO)");
            mLogger.metric(target,  "METRIC", "shall appear");
            mLogger.info(target,    "INFO    - shall appear   (level=INFO)");
            mLogger.debug(target,   "DEBUG   - shall NOT appear (level=INFO)");
            mLogger.verbose(target, "VERBOSE - shall NOT appear (level=INFO)");
        }
        mLogger.stop();

        // --- DEBUG level : all except verbose shall appear ---
        telemetry.addLine("----> Level DEBUG : all except verbose shall appear");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Level DEBUG : all except verbose shall appear");
        Log.i("LoggerTest.levelTest:220","----> Level DEBUG : all except verbose shall appear");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(), true);
        mLogger.level(Logger.Level.DEBUG);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target,   "ERROR   - shall appear   (level=DEBUG)");
            mLogger.warning(target, "WARNING - shall appear   (level=DEBUG)");
            mLogger.metric(target,  "METRIC", "shall appear");
            mLogger.info(target,    "INFO    - shall appear   (level=DEBUG)");
            mLogger.debug(target,   "DEBUG   - shall appear   (level=DEBUG)");
            mLogger.verbose(target, "VERBOSE - shall NOT appear (level=DEBUG)");
        }
        mLogger.stop();

        // --- VERBOSE level : everything shall appear ---
        telemetry.addLine("----> Level VERBOSE : all messages shall appear");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Level VERBOSE : all messages shall appear");
        Log.i("LoggerTest.levelTest:237","----> Level VERBOSE : all messages shall appear");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(), true);
        mLogger.level(Logger.Level.VERBOSE);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target,   "ERROR   - shall appear   (level=VERBOSE)");
            mLogger.warning(target, "WARNING - shall appear   (level=VERBOSE)");
            mLogger.metric(target,  "METRIC", "shall appear");
            mLogger.info(target,    "INFO    - shall appear   (level=VERBOSE)");
            mLogger.debug(target,   "DEBUG   - shall appear   (level=VERBOSE)");
            mLogger.verbose(target, "VERBOSE - shall appear   (level=VERBOSE)");
        }
        mLogger.stop();

        for (Logger.Target target : Logger.Target.values()) {
            mLogger.update(target);
        }

    }



}

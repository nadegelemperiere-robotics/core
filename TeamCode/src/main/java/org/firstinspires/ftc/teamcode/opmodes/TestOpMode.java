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

/* Panels includes */
import com.bylazar.telemetry.PanelsTelemetry;

// Solverslib includes
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

/* Core includes */
import org.firstinspires.ftc.teamcode.subsystem.DriveTrain;
import org.firstinspires.ftc.teamcode.configuration.Alliance;


@TeleOp(name="TestOpMode",group="Competition")
public  class TestOpMode extends CoreOpMode {

    DriveTrain   mDriveTrain;

    /** Configuration name (without .json), loaded from /sdcard/FIRST/<name>.json. */
    @Override
    public String configurationName() { return "test"; }

    @Override
    public void initialize_content() {

        mDriveTrain = new DriveTrain(mHardware,mLogger);
        mConfiguration.register("robot.subsystems.drivetrain", mDriveTrain);
    }

    @Override
    public void initialize_loop_content() {

        if(mGamepad1.wasJustPressed(GamepadKeys.Button.X)) {
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("ALLIANCE","BLUE");
            Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        }
        if(mGamepad1.wasJustPressed(GamepadKeys.Button.B)) {
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("ALLIANCE","RED");
            Alliance.INSTANCE.setColor(Alliance.Color.RED);
        }

    }
}
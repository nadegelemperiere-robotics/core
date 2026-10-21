/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Alliance management test
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.configuration;

// Qualcomm includes
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// Panels includes
import com.bylazar.gamepad.PanelsGamepad;
import com.bylazar.telemetry.PanelsTelemetry;

// Solverslib includes
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

// Core includes
import org.firstinspires.ftc.teamcode.control.GamepadCombinedEx;

@TeleOp(name="AllianceTest",group="Test")
public class AllianceTest extends CommandOpMode {


    GamepadCombinedEx  mGamepad1;
    GamepadCombinedEx  mGamepad2;

    @Override
    public void initialize() {

        mGamepad1 = new GamepadCombinedEx(gamepad1, PanelsGamepad.INSTANCE.getFirstManager());
        mGamepad2 = new GamepadCombinedEx(gamepad2, PanelsGamepad.INSTANCE.getSecondManager());

    }


    @Override
    public void initialize_loop() {
        super.initialize_loop();

        mGamepad1.readButtons();
        mGamepad2.readButtons();

        if(mGamepad1.wasJustPressed(GamepadKeys.Button.X)) {
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("ALLIANCE","BLUE");
            Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        }
        if(mGamepad1.wasJustPressed(GamepadKeys.Button.B)) {
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("ALLIANCE","RED");
            Alliance.INSTANCE.setColor(Alliance.Color.RED);
        }

        PanelsTelemetry.INSTANCE.getFtcTelemetry().update();

        Alliance.INSTANCE.feedback(mGamepad1);
        Alliance.INSTANCE.feedback(mGamepad2);

    }
}

package org.firstinspires.ftc.teamcode.subsystem.drivetrain;

// Pedropathing includes
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

// Procedures includes
import org.firstinspires.ftc.teamcode.subsystem.drivetrain.procedures.MecanumTuner;


public class Tuning {

    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

}

/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Inverted FTC coordinate system for PedroPathing 3.x
   ------------------------------------------------------- */

package org.firstinspires.ftc.teamcode.geometry;

// Pedro Pathing includes
import com.pedropathing.math.Pose;

// FTC SDK includes
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * An enum that contains an inverted FTC standard coordinate system (for DECODE game).
 *
 * <p>Pedro to inverted FTC : translation by (-72,-72) then rotation by -π/2, i.e. (dx,dy) → (dy,-dx).</p>
 *
 * @author BeepBot99
 * @author Baron Henderson
 */
public enum InvertedFTCCoordinates implements CoordinateSystem {
    INSTANCE;

    private static final double HALF_FIELD = 72;

    @Override
    public Pose fromPedro(Pose pose) {
        double dx = pose.x() - HALF_FIELD;
        double dy = pose.y() - HALF_FIELD;
        return new Pose(dy, -dx, AngleUnit.normalizeRadians(pose.heading() - Math.PI / 2));
    }

    @Override
    public Pose toPedro(Pose pose) {
        return new Pose(
                -pose.y() + HALF_FIELD,
                pose.x() + HALF_FIELD,
                AngleUnit.normalizeRadians(pose.heading() + Math.PI / 2));
    }
}

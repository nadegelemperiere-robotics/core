/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Coordinate system interface (replaces the PedroPathing 2.x
   CoordinateSystem, removed in PedroPathing 3.x)
   ------------------------------------------------------- */

package org.firstinspires.ftc.teamcode.geometry;

// Pedro Pathing includes
import com.pedropathing.math.Pose;

/**
 * A coordinate system that can convert poses to and from the Pedro coordinate system.
 *
 * <p>Since PedroPathing 3.x, {@link Pose} no longer carries its coordinate system :
 * all {@link Pose} objects are assumed to be expressed in Pedro coordinates, and
 * conversions have to be done explicitly through this interface.</p>
 */
public interface CoordinateSystem {

    /**
     * Converts a {@link Pose} to Pedro coordinates from this coordinate system
     *
     * @param pose The {@link Pose} to convert, in this coordinate system
     * @return The converted {@link Pose}, in Pedro coordinate system
     */
    Pose toPedro(Pose pose);

    /**
     * Converts a {@link Pose} to this coordinate system from Pedro coordinates
     *
     * @param pose The {@link Pose} to convert, in the Pedro coordinate system
     * @return The converted {@link Pose}, in this coordinate system
     */
    Pose fromPedro(Pose pose);
}

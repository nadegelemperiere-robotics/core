/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Conversions between PedroPathing poses and other
   pose formats (FTC SDK, SolversLib)
   ------------------------------------------------------- */

package org.firstinspires.ftc.teamcode.geometry;

// Pedro Pathing includes
import com.pedropathing.math.Pose;

// Solverslib includes
import com.seattlesolvers.solverslib.geometry.Pose2d;

// FTC SDK includes
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/**
 * Converts PedroPathing poses (always in Pedro coordinates) to and from other pose formats.
 */
public final class PoseConverter {

    private PoseConverter() {}

    /**
     * Converts a Pose to a Pose3D in the desired coordinate system.
     *
     * @param pose the Pose object, in Pedro coordinates
     * @param desiredCoordinateSystem the desired coordinate system
     * @return a Pose3D object with the x and y coordinates in inches and the heading in radians
     */
    public static Pose3D poseToPose3D(Pose pose, CoordinateSystem desiredCoordinateSystem) {
        Pose converted = desiredCoordinateSystem.fromPedro(pose);
        return new Pose3D(
                new Position(DistanceUnit.INCH, converted.x(), converted.y(), 0, 0),
                new YawPitchRollAngles(AngleUnit.RADIANS, converted.heading(), 0, 0, 0));
    }

    /**
     * Returns a pose from a Pose3D and a coordinate system.
     *
     * @param pose3d the Pose3D object
     * @param coordinateSystem the coordinate system of the Pose3D
     * @return a Pose object in Pedro coordinates, with the x and y coordinates in inches and the heading in radians
     */
    public static Pose pose3DToPose(Pose3D pose3d, CoordinateSystem coordinateSystem) {
        Position inches = pose3d.getPosition().toUnit(DistanceUnit.INCH);
        double heading = pose3d.getOrientation().getYaw(AngleUnit.RADIANS);
        return coordinateSystem.toPedro(new Pose(inches.x, inches.y, heading));
    }

    /**
     * Converts a Pose to a Pose2d in the desired coordinate system.
     *
     * @param pose the Pose object, in Pedro coordinates
     * @param desiredCoordinateSystem the desired coordinate system
     * @return a Pose2d object with the x and y coordinates in inches and the heading in radians
     */
    public static Pose2d poseToPose2d(Pose pose, CoordinateSystem desiredCoordinateSystem) {
        Pose converted = desiredCoordinateSystem.fromPedro(pose);
        return new Pose2d(converted.x(), converted.y(), converted.heading());
    }

    /**
     * Returns a pose from a Pose2d and a coordinate system.
     *
     * @param pose2d the Pose2d object
     * @param coordinateSystem the coordinate system of the Pose2d
     * @return a Pose object in Pedro coordinates, with the x and y coordinates in inches and the heading in radians
     */
    public static Pose pose2dToPose(Pose2d pose2d, CoordinateSystem coordinateSystem) {
        return coordinateSystem.toPedro(new Pose(pose2d.getX(), pose2d.getY(), pose2d.getHeading()));
    }

    /**
     * Converts a Pose to a Pose2D in the desired coordinate system.
     *
     * @param pose the Pose object, in Pedro coordinates
     * @param desiredCoordinateSystem the desired coordinate system
     * @return a Pose2D object with the x and y coordinates in inches and the heading in radians
     */
    public static Pose2D poseToPose2D(Pose pose, CoordinateSystem desiredCoordinateSystem) {
        Pose converted = desiredCoordinateSystem.fromPedro(pose);
        return new Pose2D(DistanceUnit.INCH, converted.x(), converted.y(), AngleUnit.RADIANS, converted.heading());
    }

    /**
     * Returns a pose from a Pose2D and a coordinate system.
     *
     * @param pose2d the Pose2D object
     * @param coordinateSystem the coordinate system of the Pose2D
     * @return a Pose object in Pedro coordinates, with the x and y coordinates in inches and the heading in radians
     */
    public static Pose pose2DToPose(Pose2D pose2d, CoordinateSystem coordinateSystem) {
        return coordinateSystem.toPedro(new Pose(
                pose2d.getX(DistanceUnit.INCH),
                pose2d.getY(DistanceUnit.INCH),
                pose2d.getHeading(AngleUnit.RADIANS)));
    }
}

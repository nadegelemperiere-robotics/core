/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Unit tests for PoseConverter and FTCCoordinates.
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.geometry;

// System includes
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// JUnit includes
import static org.junit.Assert.assertEquals;
import org.junit.Test;

// Pedro Pathing includes
import com.pedropathing.math.Pose;

// SolversLib includes
import com.seattlesolvers.solverslib.geometry.Pose2d;

// FTC SDK includes
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;


public class PoseConverterTest {

    private enum Reference {
        LIMELIGHT,
        SOLVERSLIB,
        PEDROPATHING,
        FTC_CONTROLLER
    }

    // Tight delta for exact arithmetic; 1e-6 where meter↔inch conversion is involved
    private static final double EXACT  = 1e-9;
    private static final double APPROX = 1e-6;

    // Field corners indexed 0..3 — Pedro order: (0,0), (144,0), (0,144), (144,144)
    // FTC/Limelight orientation uses FTCCoordinates: subtract(72,72) then rotate(+π/2)
    //   rotation +π/2: (dx,dy) → (-dy, dx)
    private static final Map<Reference, Object[]> CORNERS = new LinkedHashMap<>();

    static {
        CORNERS.put(Reference.PEDROPATHING, new Pose[]{
            new Pose(  0,   0, - Math.PI / 2),
            new Pose(144,   0, 0),
            new Pose(  0, 144, Math.PI / 2),
            new Pose(144, 144, Math.PI)
        });
        CORNERS.put(Reference.FTC_CONTROLLER, new Pose2D[]{
            new Pose2D(DistanceUnit.INCH, 72,  -72, AngleUnit.RADIANS, 0),
            new Pose2D(DistanceUnit.INCH, 72, 72, AngleUnit.RADIANS, Math.PI / 2),
            new Pose2D(DistanceUnit.INCH,  -72,  -72, AngleUnit.RADIANS, Math.PI),
            new Pose2D(DistanceUnit.INCH,  -72, 72, AngleUnit.RADIANS, -Math.PI / 2)
        });
        CORNERS.put(Reference.LIMELIGHT, new Pose3D[]{
            new Pose3D(new Position(DistanceUnit.METER, 1.8288,  -1.8288, 0, 0), new YawPitchRollAngles(AngleUnit.DEGREES, 0, 0, 0, 0)),
            new Pose3D(new Position(DistanceUnit.METER, 1.8288, 1.8288, 0, 0), new YawPitchRollAngles(AngleUnit.DEGREES, 90, 0, 0, 0)),
            new Pose3D(new Position(DistanceUnit.METER,  -1.8288,  -1.8288, 0, 0), new YawPitchRollAngles(AngleUnit.DEGREES, 180, 0, 0, 0)),
            new Pose3D(new Position(DistanceUnit.METER,  -1.8288, 1.8288, 0, 0), new YawPitchRollAngles(AngleUnit.DEGREES, -90, 0, 0, 0))
        });
        CORNERS.put(Reference.SOLVERSLIB, new Pose2d[]{
            new Pose2d(  72,   -72, 0),
            new Pose2d(72,   72, Math.PI/2),
            new Pose2d(  -72, -72, Math.PI),
            new Pose2d(-72, 72, -Math.PI/2)
        });
    }

    @Test
    public void limelightToPedroPathing() {

        int i_corner = 0;
        for (Object corner : Objects.requireNonNull(CORNERS.get(Reference.LIMELIGHT))) {
            Pose result = PoseConverter.pose3DToPose((Pose3D)corner, FTCCoordinates.INSTANCE);
            Pose reference = (Pose)(Objects.requireNonNull(CORNERS.get(Reference.PEDROPATHING))[i_corner]);

            assertEquals(reference.x(), result.x(), EXACT);
            assertEquals(reference.y(), result.y(), EXACT);
            assertEquals(Math.cos(reference.heading()), Math.cos(result.heading()), EXACT);
            assertEquals(Math.sin(reference.heading()), Math.sin(result.heading()), EXACT);

            i_corner ++;
        }

    }

    @Test
    public void solverslibToPedroPathing() {

        int i_corner = 0;
        for (Object corner : Objects.requireNonNull(CORNERS.get(Reference.SOLVERSLIB))) {
            Pose result = PoseConverter.pose2dToPose((Pose2d)corner, FTCCoordinates.INSTANCE);
            Pose reference = (Pose)(Objects.requireNonNull(CORNERS.get(Reference.PEDROPATHING))[i_corner]);

            assertEquals(reference.x(), result.x(), EXACT);
            assertEquals(reference.y(), result.y(), EXACT);
            assertEquals(Math.cos(reference.heading()), Math.cos(result.heading()), EXACT);
            assertEquals(Math.sin(reference.heading()), Math.sin(result.heading()), EXACT);

            i_corner ++;
        }
    }

    @Test
    public void ftcControllerToPedroPathing() {

        int i_corner = 0;
        for (Object corner : Objects.requireNonNull(CORNERS.get(Reference.FTC_CONTROLLER))) {
            Pose result = PoseConverter.pose2DToPose((Pose2D)corner, FTCCoordinates.INSTANCE);
            Pose reference = (Pose)(Objects.requireNonNull(CORNERS.get(Reference.PEDROPATHING))[i_corner]);

            assertEquals(reference.x(), result.x(), EXACT);
            assertEquals(reference.y(), result.y(), EXACT);
            assertEquals(Math.cos(reference.heading()), Math.cos(result.heading()), EXACT);
            assertEquals(Math.sin(reference.heading()), Math.sin(result.heading()), EXACT);

            i_corner ++;
        }

    }


    @Test
    public void pedroPathingToLimelight() {

        int i_corner = 0;
        for (Object corner : Objects.requireNonNull(CORNERS.get(Reference.PEDROPATHING))) {
            Pose3D result = PoseConverter.poseToPose3D((Pose)corner, FTCCoordinates.INSTANCE);
            Pose3D reference = (Pose3D)(Objects.requireNonNull(CORNERS.get(Reference.LIMELIGHT))[i_corner]);

            assertEquals(reference.getPosition().x, result.getPosition().toUnit(DistanceUnit.METER).x, EXACT);
            assertEquals(reference.getPosition().y, result.getPosition().toUnit(DistanceUnit.METER).y, EXACT);
            assertEquals(Math.cos(reference.getOrientation().getYaw(AngleUnit.RADIANS)), Math.cos(result.getOrientation().getYaw()), EXACT);
            assertEquals(Math.sin(reference.getOrientation().getYaw(AngleUnit.RADIANS)), Math.sin(result.getOrientation().getYaw()), EXACT);

            i_corner ++;
        }

    }

    @Test
    public void pedroPathingToSolverslib() {

        int i_corner = 0;
        for (Object corner : Objects.requireNonNull(CORNERS.get(Reference.PEDROPATHING))) {
            Pose2d result = PoseConverter.poseToPose2d((Pose)corner, FTCCoordinates.INSTANCE);
            Pose2d reference = (Pose2d)(Objects.requireNonNull(CORNERS.get(Reference.SOLVERSLIB))[i_corner]);

            assertEquals(reference.getX(), result.getX(), EXACT);
            assertEquals(reference.getY(), result.getY(), EXACT);
            assertEquals(Math.cos(reference.getHeading()), Math.cos(result.getHeading()), EXACT);
            assertEquals(Math.sin(reference.getHeading()), Math.sin(result.getHeading()), EXACT);

            i_corner ++;
        }
    }

    @Test
    public void pedroPathingToFtcController() {

        int i_corner = 0;
        for (Object corner : Objects.requireNonNull(CORNERS.get(Reference.PEDROPATHING))) {
            Pose2D result = PoseConverter.poseToPose2D((Pose)corner, FTCCoordinates.INSTANCE);
            Pose2D reference = (Pose2D)(Objects.requireNonNull(CORNERS.get(Reference.FTC_CONTROLLER))[i_corner]);

            assertEquals(reference.getX(DistanceUnit.INCH), result.getX(DistanceUnit.INCH), EXACT);
            assertEquals(reference.getY(DistanceUnit.INCH), result.getY(DistanceUnit.INCH), EXACT);
            assertEquals(Math.cos(reference.getHeading(AngleUnit.RADIANS)), Math.cos(result.getHeading(AngleUnit.RADIANS)), EXACT);
            assertEquals(Math.sin(reference.getHeading(AngleUnit.RADIANS)), Math.sin(result.getHeading(AngleUnit.RADIANS)), EXACT);

            i_corner ++;
        }

    }


}

/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Alliance management class
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.configuration;

// Android includes
import android.content.Context;

// FTC SDK includes
import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop;

// Solverslib includes
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

// Panels includes
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.configurables.annotations.IgnoreConfigurable;
import com.bylazar.configurables.PanelsConfigurables;
import com.bylazar.alliance.PanelsAlliance;

@Configurable
public class Alliance {

    @IgnoreConfigurable
    static final  int               sFeedbackDefaultDuration = 100;

    static public Alliance.Color    COLOR = Alliance.Color.NONE;

    // Registered alliance colors with their values
    public enum Color {

        BLUE(0.0),
        RED(1.0),
        NONE(2.0);

        final double mValue;

        Color(double value)         { mValue = value; }
        public Double getValue()    { return mValue; }
    }

    // Members
    Color                           mColor;

    // Alliance singleton
    @IgnoreConfigurable
    public static Alliance INSTANCE = new Alliance();

    // Feed the alliance color to the Panels alliance navlet, called by the SDK at robot startup
    @OnCreateEventLoop
    public static void registerPanelsAlliance(Context context, FtcEventLoop eventLoop) {
        PanelsAlliance.getProvider().setColorSupplier(() -> INSTANCE.getColor().name());
        RobotLog.ii("Alliance", "Alliance color supplier registered to Panels alliance navlet");
        RobotLog.ii("Alliance", "Registered with class loader " + Alliance.class.getClassLoader());
    }

    // Private constructor
    private Alliance()              { mColor = Color.NONE; }

    // Alliance accessors
    public  Color               getColor()     {
        update();
        return mColor;
    }
    public  void                setColor(Color color)  {
        RobotLog.ii("Alliance", "setColor " + color + " with class loader " + Alliance.class.getClassLoader());
        mColor = color;
        COLOR = color;
        PanelsConfigurables.refreshClass(this);
    }
    public  void                reset()                { setColor(Color.NONE); }
    private void                update()               { if(COLOR != mColor) { mColor = COLOR; } }

    // Alliance feedbacks
    public  void                feedback(GamepadEx gamepad, int duration) {
        update();
        if(mColor == Color.BLUE)      { gamepad.gamepad.setLedColor(0,0,1, duration); }
        else if(mColor == Color.RED)  { gamepad.gamepad.setLedColor(1,0,0, duration); }
        else  { gamepad.gamepad.setLedColor(0,0,0, duration); }
    }
    public   void               feedback(GamepadEx gamepad) { feedback(gamepad,sFeedbackDefaultDuration);}

}

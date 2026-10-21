/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   GamepadEx component mapped on SolversLib one but designed to
   be compatible with Panels Gamepads
   ------------------------------------------------------- */
package org.firstinspires.ftc.teamcode.control;

// Qualcomm includes
import com.qualcomm.robotcore.hardware.Gamepad;

// Solverslib includes
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.gamepad.SlewRateLimiter;

// Panels includes
import com.bylazar.gamepad.GamepadManager;

public class GamepadCombinedEx extends GamepadEx {

    GamepadManager          mVirtual;

    private SlewRateLimiter mLX = null;
    private SlewRateLimiter mLY = null;
    private SlewRateLimiter mRX = null;
    private SlewRateLimiter mRY = null;

    public GamepadCombinedEx(Gamepad real, GamepadManager virtual) {
        super(real);
        mVirtual = virtual;
    }

    /**
     * @param button the button object
     * @return the boolean value as to whether the button is active or not
     */
    public boolean getButton(GamepadKeys.Button button) {

        boolean buttonValue = false;
        Gamepad combined = gamepad;
        if(mVirtual != null) { combined = mVirtual.asCombinedFTCGamepad(gamepad); }
        switch (button) {
            case A:
            case CROSS:
                buttonValue = combined.a;
                break;
            case B:
            case CIRCLE:
                buttonValue = combined.b;
                break;
            case SQUARE:
            case X:
                buttonValue = combined.x;
                break;
            case TRIANGLE:
            case Y:
                buttonValue = combined.y;
                break;
            case LEFT_BUMPER:
                buttonValue = combined.left_bumper;
                break;
            case RIGHT_BUMPER:
                buttonValue = combined.right_bumper;
                break;
            case DPAD_UP:
                buttonValue = combined.dpad_up;
                break;
            case DPAD_DOWN:
                buttonValue = combined.dpad_down;
                break;
            case DPAD_LEFT:
                buttonValue = combined.dpad_left;
                break;
            case DPAD_RIGHT:
                buttonValue = combined.dpad_right;
                break;
            case BACK:
                buttonValue = combined.back;
                break;
            case START:
                buttonValue = combined.start;
                break;
            case OPTIONS:
                buttonValue = combined.options;
                break;
            case LEFT_STICK_BUTTON:
                buttonValue = combined.left_stick_button;
                break;
            case RIGHT_STICK_BUTTON:
                buttonValue = combined.right_stick_button;
                break;
            case PS:
                buttonValue = combined.ps;
                break;
            case SHARE:
                buttonValue = combined.share;
                break;
            case TOUCHPAD:
                buttonValue = combined.touchpad;
                break;
            case TOUCHPAD_FINGER_1:
                buttonValue = combined.touchpad_finger_1;
                break;
            case TOUCHPAD_FINGER_2:
                buttonValue = combined.touchpad_finger_2;
                break;
            default:
                buttonValue = false;
                break;
        }
        return buttonValue;
    }

    /**
     * @param trigger the trigger object
     * @return the value returned by the trigger in question
     */
    public double getTrigger(GamepadKeys.Trigger trigger) {
        double triggerValue = 0;
        Gamepad combined = mVirtual.asCombinedFTCGamepad(gamepad);
        switch (trigger) {
            case LEFT_TRIGGER:
                triggerValue = combined.left_trigger;
                break;
            case RIGHT_TRIGGER:
                triggerValue = combined.right_trigger;
                break;
            default:
                break;
        }
        return triggerValue;
    }

    /**
     * Enables and sets the slew rate limiting for the joysticks.
     * Parameters are labelled by L/R for left/right joystick on the controller and X/Y for axis of joystick movement.
     * Set any parameters not to be enabled/used as null.
     * @return this object for chaining purposes
     */
    public GamepadEx setJoystickSlewRateLimiters(SlewRateLimiter LX, SlewRateLimiter LY, SlewRateLimiter RX, SlewRateLimiter RY) {
        this.mLX = LX;
        this.mLY = LY;
        this.mRX = RX;
        this.mRY = RY;
        return this;
    }

    /**
     * @return the y-value on the left analog stick
     */
    public double getLeftY() {
        Gamepad combined = mVirtual.asCombinedFTCGamepad(gamepad);
        if (mLY == null) {
            return -combined.left_stick_y;
        } else {
            return mLY.calculate(-combined.left_stick_y);
        }
    }

    /**
     * @return the y-value on the right analog stick
     */
    public double getRightY() {
        Gamepad combined = mVirtual.asCombinedFTCGamepad(gamepad);
        if (mRY == null) {
            return combined.right_stick_y;
        } else {
            return mRY.calculate(combined.right_stick_y);
        }
    }

    /**
     * @return the x-value on the left analog stick
     */
    public double getLeftX() {
        Gamepad combined = mVirtual.asCombinedFTCGamepad(gamepad);
        if (mLX == null) {
            return combined.left_stick_x;
        } else {
            return mLX.calculate(combined.left_stick_x);
        }
    }

    /**
     * @return the x-value on the right analog stick
     */
    public double getRightX() {
        Gamepad combined = mVirtual.asCombinedFTCGamepad(gamepad);
        if (mRX == null) {
            return combined.right_stick_x;
        } else {
            return mRX.calculate(combined.right_stick_x);
        }
    }


}

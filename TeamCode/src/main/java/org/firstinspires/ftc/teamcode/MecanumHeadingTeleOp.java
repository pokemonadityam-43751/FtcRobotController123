package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/*
 * Mecanum TeleOp with heading control.
 *
 *   Left joystick  -> ORIENTATION. Point the stick where you want the robot to face
 *                     (up = the direction the robot faced at the last yaw reset).
 *                     The robot turns to that heading and holds it when you let go.
 *   Right joystick -> MOVEMENT. Field-centric: pushing up always drives away from the
 *                     driver, no matter which way the robot is facing.
 *   A button       -> reset IMU yaw (sets the current facing as "up" on the left stick).
 *
 * Wheel speeds are closed-loop velocity commands (DcMotorEx.setVelocity), computed from the
 * 312 RPM motor spec, so speeds are in real units (inches per second). Acceleration limits
 * are applied to the drive vector and the turn rate so the robot ramps smoothly.
 *
 * Motor names and wiring match RobotTeleopMecanumFieldRelativeDrive in the samples:
 *   front_left_drive, front_right_drive, back_left_drive, back_right_drive, and an IMU named "imu".
 * The motors must have encoders connected (goBILDA 312 RPM motors include them).
 */
@TeleOp(name = "Mecanum Heading Drive", group = "Robot")
public class MecanumHeadingTeleOp extends OpMode {

    // ======================================================================
    // ROBOT / MOTOR CONSTANTS -- set these to match your hardware
    // ======================================================================

    // goBILDA 5203 Yellow Jacket, 312 RPM: 537.7 encoder counts per output shaft revolution.
    private static final double MOTOR_MAX_RPM = 312.0;
    private static final double TICKS_PER_REV = 537.7;

    // Mecanum wheel diameter in inches (96 mm = 3.78 in, 75 mm = 2.95 in, 100 mm = 3.94 in).
    private static final double WHEEL_DIAMETER_IN = 3.78;

    // Distance between left and right wheel centers, and between front and back wheel centers.
    private static final double TRACK_WIDTH_IN = 12.0;
    private static final double WHEELBASE_IN = 11.0;

    // Derived values -- do not edit.
    private static final double WHEEL_CIRCUMFERENCE_IN = Math.PI * WHEEL_DIAMETER_IN;
    // Top linear speed of a wheel at 312 RPM, in inches per second (about 61.7 in/s here).
    private static final double MAX_WHEEL_SPEED_IN_PER_S =
            (MOTOR_MAX_RPM / 60.0) * WHEEL_CIRCUMFERENCE_IN;
    // Effective radius used to convert robot rotation (rad/s) into wheel surface speed (in/s).
    private static final double ROTATION_RADIUS_IN = (TRACK_WIDTH_IN + WHEELBASE_IN) / 2.0;

    // ======================================================================
    // TUNABLE VARIABLES -- adjust these to change speed, acceleration, and feel.
    // (Public static so they can be edited from FTC Dashboard or changed here directly.)
    // ======================================================================

    // --- Speed ---
    /** Top translation speed (right stick at full deflection), inches per second.
     *  Must stay below the wheel limit (~61.7 in/s at 312 RPM). Lower it for beginners. */
    public static double MAX_DRIVE_SPEED_IN_PER_S = 40.0;
    /** Top turning speed while tracking the left-stick heading, degrees per second. */
    public static double MAX_TURN_SPEED_DEG_PER_S = 180.0;

    // --- Acceleration ---
    /** How fast the robot speeds up when driving, inches per second squared. */
    public static double DRIVE_ACCEL_IN_PER_S2 = 100.0;
    /** How fast the robot slows down / stops when driving, inches per second squared.
     *  Usually set higher than the acceleration so the robot stops crisply. */
    public static double DRIVE_DECEL_IN_PER_S2 = 150.0;
    /** How fast the robot can change its turn rate, degrees per second squared. */
    public static double TURN_ACCEL_DEG_PER_S2 = 540.0;

    // --- Heading control ---
    /** Proportional gain: turn rate (rad/s) commanded per radian of heading error.
     *  Raise for a snappier turn, lower if the robot overshoots or wobbles. */
    public static double HEADING_KP = 4.0;
    /** Heading errors smaller than this are ignored, so the robot doesn't jitter. Degrees. */
    public static double HEADING_DEADBAND_DEG = 1.0;

    // --- Joystick handling ---
    /** Stick deflection below this is treated as zero on the right (movement) stick. */
    public static double STICK_DEADBAND = 0.05;
    /** The left stick must be pushed past this magnitude to set a new target heading.
     *  Keeps the robot holding its last heading when the stick springs back to center. */
    public static double LEFT_STICK_ENGAGE_THRESHOLD = 0.5;

    // ======================================================================
    // HARDWARE
    // ======================================================================
    private DcMotorEx frontLeftDrive;
    private DcMotorEx frontRightDrive;
    private DcMotorEx backLeftDrive;
    private DcMotorEx backRightDrive;
    private IMU imu;

    // ======================================================================
    // STATE
    // ======================================================================
    private final ElapsedTime loopTimer = new ElapsedTime();

    /** Desired robot heading, radians, counter-clockwise positive, in (-pi, pi]. */
    private double targetHeadingRad = 0.0;
    /** Current (rate-limited) field-frame translation velocity, in/s.
     *  "F" = forward from the reset direction, "R" = right of the reset direction. */
    private double fieldForwardIn = 0.0;
    private double fieldRightIn = 0.0;
    /** Current (rate-limited) turn rate, rad/s, clockwise positive. */
    private double turnRateCwRad = 0.0;

    @Override
    public void init() {
        frontLeftDrive = hardwareMap.get(DcMotorEx.class, "front_left_drive");
        frontRightDrive = hardwareMap.get(DcMotorEx.class, "front_right_drive");
        backLeftDrive = hardwareMap.get(DcMotorEx.class, "back_left_drive");
        backRightDrive = hardwareMap.get(DcMotorEx.class, "back_right_drive");

        // Left motors are reversed, as in the FTC samples, so that positive power drives forward.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        // Velocity control needs RUN_USING_ENCODER. Brake when power is zero so the robot holds.
        for (DcMotorEx motor : allMotors()) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        imu = hardwareMap.get(IMU.class, "imu");
        // Change these to match how the Control/Expansion Hub is mounted on your robot.
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
        imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(logoDirection, usbDirection)));
    }

    @Override
    public void start() {
        loopTimer.reset();
    }

    @Override
    public void loop() {
        // Time since the last loop, capped so a long hiccup doesn't cause a big velocity jump.
        double dt = Math.min(loopTimer.seconds(), 0.1);
        loopTimer.reset();

        // --- A: reset yaw. The current facing becomes "up" on the left stick. ---
        if (gamepad1.a) {
            imu.resetYaw();
            targetHeadingRad = 0.0;
        }
        double yawRad = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        // ------------------------------------------------------------------
        // 1. LEFT STICK -> target heading
        // ------------------------------------------------------------------
        double lx = gamepad1.left_stick_x;
        double ly = gamepad1.left_stick_y;
        if (Math.hypot(lx, ly) > LEFT_STICK_ENGAGE_THRESHOLD) {
            // Clockwise angle from "stick up". Stick up is ly = -1 on the gamepad.
            double cwFromUp = Math.atan2(lx, -ly);
            // Our heading convention is counter-clockwise positive, so flip the sign.
            targetHeadingRad = AngleUnit.normalizeRadians(-cwFromUp);
        }

        // Heading error (counter-clockwise positive), wrapped to (-pi, pi].
        double headingErrorRad = AngleUnit.normalizeRadians(targetHeadingRad - yawRad);

        // Proportional controller, clamped to the maximum turn speed.
        double maxTurnRad = Math.toRadians(MAX_TURN_SPEED_DEG_PER_S);
        double desiredTurnCcw = 0.0;
        if (Math.abs(headingErrorRad) > Math.toRadians(HEADING_DEADBAND_DEG)) {
            desiredTurnCcw = clamp(HEADING_KP * headingErrorRad, -maxTurnRad, maxTurnRad);
        }
        // Positive "clockwise" is the convention used by the wheel formulas below.
        double desiredTurnCw = -desiredTurnCcw;

        // Limit how quickly the turn rate can change.
        double maxTurnStep = Math.toRadians(TURN_ACCEL_DEG_PER_S2) * dt;
        turnRateCwRad += clamp(desiredTurnCw - turnRateCwRad, -maxTurnStep, maxTurnStep);

        // ------------------------------------------------------------------
        // 2. RIGHT STICK -> field-centric translation
        // ------------------------------------------------------------------
        double forwardInput = applyDeadband(-gamepad1.right_stick_y); // stick up = forward
        double rightInput = applyDeadband(gamepad1.right_stick_x);    // stick right = right
        // Keep diagonals from exceeding full speed.
        double inputMag = Math.hypot(forwardInput, rightInput);
        if (inputMag > 1.0) {
            forwardInput /= inputMag;
            rightInput /= inputMag;
        }
        double desiredForwardIn = forwardInput * MAX_DRIVE_SPEED_IN_PER_S;
        double desiredRightIn = rightInput * MAX_DRIVE_SPEED_IN_PER_S;

        // Limit how quickly the translation velocity can change, in any direction.
        // Use the decel rate when the target speed is lower than the current speed.
        double deltaF = desiredForwardIn - fieldForwardIn;
        double deltaR = desiredRightIn - fieldRightIn;
        double deltaDist = Math.hypot(deltaF, deltaR);
        double desiredSpeed = Math.hypot(desiredForwardIn, desiredRightIn);
        double currentSpeed = Math.hypot(fieldForwardIn, fieldRightIn);
        double driveRate = (desiredSpeed >= currentSpeed) ? DRIVE_ACCEL_IN_PER_S2 : DRIVE_DECEL_IN_PER_S2;
        double maxDriveStep = driveRate * dt;
        if (deltaDist > maxDriveStep && deltaDist > 0) {
            double scale = maxDriveStep / deltaDist;
            deltaF *= scale;
            deltaR *= scale;
        }
        fieldForwardIn += deltaF;
        fieldRightIn += deltaR;

        // ------------------------------------------------------------------
        // 3. Field frame -> robot frame, using the current heading.
        // ------------------------------------------------------------------
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        double robotForwardIn = fieldForwardIn * cos - fieldRightIn * sin;
        double robotRightIn = fieldForwardIn * sin + fieldRightIn * cos;
        double rotationIn = turnRateCwRad * ROTATION_RADIUS_IN; // wheel surface speed from turning

        // ------------------------------------------------------------------
        // 4. Mecanum mixing (same convention as RobotTeleopMecanumFieldRelativeDrive).
        // Each value is a wheel surface speed in inches per second.
        // ------------------------------------------------------------------
        double fl = robotForwardIn + robotRightIn + rotationIn;
        double fr = robotForwardIn - robotRightIn - rotationIn;
        double bl = robotForwardIn - robotRightIn + rotationIn;
        double br = robotForwardIn + robotRightIn - rotationIn;

        // If any wheel would exceed its 312 RPM limit, scale all four together so the
        // direction of motion is preserved.
        double maxAbs = Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                                 Math.max(Math.abs(bl), Math.abs(br)));
        if (maxAbs > MAX_WHEEL_SPEED_IN_PER_S) {
            double scale = MAX_WHEEL_SPEED_IN_PER_S / maxAbs;
            fl *= scale;
            fr *= scale;
            bl *= scale;
            br *= scale;
        }

        // ------------------------------------------------------------------
        // 5. Send velocities to the motors (ticks per second).
        // ------------------------------------------------------------------
        frontLeftDrive.setVelocity(inchesPerSecToTicksPerSec(fl));
        frontRightDrive.setVelocity(inchesPerSecToTicksPerSec(fr));
        backLeftDrive.setVelocity(inchesPerSecToTicksPerSec(bl));
        backRightDrive.setVelocity(inchesPerSecToTicksPerSec(br));

        // ------------------------------------------------------------------
        // Telemetry
        // ------------------------------------------------------------------
        telemetry.addLine("A: reset yaw | Left stick: aim heading | Right stick: drive");
        telemetry.addData("Heading (deg)", "%.1f", Math.toDegrees(yawRad));
        telemetry.addData("Target heading (deg)", "%.1f", Math.toDegrees(targetHeadingRad));
        telemetry.addData("Heading error (deg)", "%.1f", Math.toDegrees(headingErrorRad));
        telemetry.addData("Drive speed (in/s)", "%.1f", Math.hypot(fieldForwardIn, fieldRightIn));
        telemetry.addData("Turn rate (deg/s)", "%.1f", Math.toDegrees(turnRateCwRad));
        telemetry.addData("Wheel speeds (in/s) FL FR BL BR", "%.1f  %.1f  %.1f  %.1f", fl, fr, bl, br);
        telemetry.addData("Max wheel speed (in/s)", "%.1f", MAX_WHEEL_SPEED_IN_PER_S);
    }

    @Override
    public void stop() {
        for (DcMotorEx motor : allMotors()) {
            motor.setVelocity(0);
        }
    }

    // ======================================================================
    // HELPERS
    // ======================================================================

    private DcMotorEx[] allMotors() {
        return new DcMotorEx[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
    }

    /** Converts a wheel surface speed (in/s) into encoder ticks per second for the motor. */
    private static double inchesPerSecToTicksPerSec(double inchesPerSec) {
        return (inchesPerSec / WHEEL_CIRCUMFERENCE_IN) * TICKS_PER_REV;
    }

    private static double applyDeadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

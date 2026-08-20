package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

/*
 * ONE FILE to copy into:
 *   TeamCode/src/main/java/org/firstinspires/ftc/teamcode/IntakeTeleOp.java
 *
 * Robot config names (CR servos for the rollers):
 *   conveyor1, conveyor2, topRoller, leftRoller, rightRoller
 *
 * A = all forward    B = all reverse    X = stop
 *
 * Hold to test one part:
 *   D-pad up/down     top roller
 *   D-pad left/right  left roller
 *   Y / Start         right roller
 *   LB / LT           conveyor 1
 *   RB / RT           conveyor 2
 */
@TeleOp(name = "Intake TeleOp", group = "Intake")
public class IntakeTeleOp extends LinearOpMode {

    // Different conveyor speeds (0 to 1)
    static final double CONVEYOR_1_SPEED = 0.90;
    static final double CONVEYOR_2_SPEED = 0.70;
    static final double TOP_ROLLER_SPEED = 1.00;
    static final double LEFT_ROLLER_SPEED = 1.00;
    static final double RIGHT_ROLLER_SPEED = 1.00;

    // Tune these so different-sized rollers match (0 to 1)
    static final double CONVEYOR_1_COEFF = 1.00;
    static final double CONVEYOR_2_COEFF = 1.00;
    static final double TOP_ROLLER_COEFF = 1.00;
    static final double LEFT_ROLLER_COEFF = 1.00;
    static final double RIGHT_ROLLER_COEFF = 1.00;

    // Flip to -1 if a device is backwards
    static final double CONVEYOR_1_SIGN = 1.0;
    static final double CONVEYOR_2_SIGN = 1.0;
    static final double TOP_ROLLER_SIGN = 1.0;
    static final double LEFT_ROLLER_SIGN = 1.0;
    static final double RIGHT_ROLLER_SIGN = -1.0; // opposite of left

    DcMotorEx conveyor1;
    DcMotorEx conveyor2;
    CRServo topRoller;
    CRServo leftRoller;
    CRServo rightRoller;

    @Override
    public void runOpMode() {
        conveyor1 = hardwareMap.get(DcMotorEx.class, "conveyor1");
        conveyor2 = hardwareMap.get(DcMotorEx.class, "conveyor2");
        topRoller = hardwareMap.get(CRServo.class, "topRoller");
        leftRoller = hardwareMap.get(CRServo.class, "leftRoller");
        rightRoller = hardwareMap.get(CRServo.class, "rightRoller");

        conveyor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        conveyor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        conveyor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        conveyor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        conveyor1.setDirection(DcMotorSimple.Direction.FORWARD);
        conveyor2.setDirection(DcMotorSimple.Direction.FORWARD);
        topRoller.setDirection(DcMotorSimple.Direction.FORWARD);
        leftRoller.setDirection(DcMotorSimple.Direction.FORWARD);
        rightRoller.setDirection(DcMotorSimple.Direction.FORWARD);

        stopIntake();

        telemetry.addLine("A/B/X = all fwd / all rev / stop");
        telemetry.addLine("Hold D-pad, Y/Start, bumpers, triggers to test one part");
        telemetry.update();
        waitForStart();

        boolean testingPart = false;

        while (opModeIsActive()) {
            if (gamepad1.x) {
                stopIntake();
                testingPart = false;
            } else if (gamepad1.dpad_up) {
                runOnlyTop(1.0);
                testingPart = true;
            } else if (gamepad1.dpad_down) {
                runOnlyTop(-1.0);
                testingPart = true;
            } else if (gamepad1.dpad_left) {
                runOnlyLeft(1.0);
                testingPart = true;
            } else if (gamepad1.dpad_right) {
                runOnlyLeft(-1.0);
                testingPart = true;
            } else if (gamepad1.y) {
                runOnlyRight(1.0);
                testingPart = true;
            } else if (gamepad1.start) {
                runOnlyRight(-1.0);
                testingPart = true;
            } else if (gamepad1.left_bumper) {
                runOnlyConveyor1(1.0);
                testingPart = true;
            } else if (gamepad1.left_trigger > 0.5) {
                runOnlyConveyor1(-1.0);
                testingPart = true;
            } else if (gamepad1.right_bumper) {
                runOnlyConveyor2(1.0);
                testingPart = true;
            } else if (gamepad1.right_trigger > 0.5) {
                runOnlyConveyor2(-1.0);
                testingPart = true;
            } else if (gamepad1.a) {
                runAll(1.0);
                testingPart = false;
            } else if (gamepad1.b) {
                runAll(-1.0);
                testingPart = false;
            } else if (testingPart) {
                stopIntake();
                testingPart = false;
            }

            telemetry.addData("C1", "%.2f", conveyor1.getPower());
            telemetry.addData("C2", "%.2f", conveyor2.getPower());
            telemetry.addData("Top", "%.2f", topRoller.getPower());
            telemetry.addData("Left", "%.2f", leftRoller.getPower());
            telemetry.addData("Right", "%.2f", rightRoller.getPower());
            telemetry.update();
        }

        stopIntake();
    }

    void runAll(double dir) {
        setPowers(
                power(dir, CONVEYOR_1_SPEED, CONVEYOR_1_COEFF, CONVEYOR_1_SIGN),
                power(dir, CONVEYOR_2_SPEED, CONVEYOR_2_COEFF, CONVEYOR_2_SIGN),
                power(dir, TOP_ROLLER_SPEED, TOP_ROLLER_COEFF, TOP_ROLLER_SIGN),
                power(dir, LEFT_ROLLER_SPEED, LEFT_ROLLER_COEFF, LEFT_ROLLER_SIGN),
                power(dir, RIGHT_ROLLER_SPEED, RIGHT_ROLLER_COEFF, RIGHT_ROLLER_SIGN));
    }

    void runOnlyConveyor1(double dir) {
        setPowers(power(dir, CONVEYOR_1_SPEED, CONVEYOR_1_COEFF, CONVEYOR_1_SIGN), 0, 0, 0, 0);
    }

    void runOnlyConveyor2(double dir) {
        setPowers(0, power(dir, CONVEYOR_2_SPEED, CONVEYOR_2_COEFF, CONVEYOR_2_SIGN), 0, 0, 0);
    }

    void runOnlyTop(double dir) {
        setPowers(0, 0, power(dir, TOP_ROLLER_SPEED, TOP_ROLLER_COEFF, TOP_ROLLER_SIGN), 0, 0);
    }

    void runOnlyLeft(double dir) {
        setPowers(0, 0, 0, power(dir, LEFT_ROLLER_SPEED, LEFT_ROLLER_COEFF, LEFT_ROLLER_SIGN), 0);
    }

    void runOnlyRight(double dir) {
        setPowers(0, 0, 0, 0, power(dir, RIGHT_ROLLER_SPEED, RIGHT_ROLLER_COEFF, RIGHT_ROLLER_SIGN));
    }

    void stopIntake() {
        setPowers(0, 0, 0, 0, 0);
    }

    void setPowers(double c1, double c2, double top, double left, double right) {
        conveyor1.setPower(c1);
        conveyor2.setPower(c2);
        topRoller.setPower(top);
        leftRoller.setPower(left);
        rightRoller.setPower(right);
    }

    double power(double direction, double speed, double coeff, double sign) {
        if (direction == 0) {
            return 0;
        }
        return Range.clip(direction * sign * speed * Range.clip(coeff, 0, 1), -1, 1);
    }
}

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

public class Teleop extends LinearOpMode
{
	DcMotor motorLeft;
	DcMotor motorRight;

	boolean a_pressed = false;
	boolean up_pressed = false;
	boolean down_pressed = false;

	final int AALENGTH = 20;
	double[] aaleft = new double[AALENGTH];
	double[] aaright = new double[AALENGTH];

	final double THRESHOLD = 0.9;

	NumberFormat speedformat = new DecimalFormat("#0.00");

	public boolean isLeft(DcMotor motor) { return motor.getDirection() == DcMotor.Direction.REVERSE; }

	public double speedavg(double[] avgs)
	{
		double sum = 0.0;
		for(double i : avgs) sum += i;
		return sum/avgs.length;
	}

	public void runMotor(DcMotor motor)
	{
		if(Global.acceleration) { motor.setPower(Range.clip(isLeft(motor) ? speedavg(aaleft) : speedavg(aaright), -Global.maxspeed, Global.maxspeed)); }
		else motor.setPower(Range.clip(isLeft(motor) ? gamepad1.left_stick_y : gamepad1.right_stick_y, -Global.maxspeed, Global.maxspeed));
	}

	@Override
	public void runOpMode() throws InterruptedException
	{
		motorLeft = hardwareMap.dcMotor.get("left");
		motorRight = hardwareMap.dcMotor.get("right");
		motorLeft.setDirection(DcMotor.Direction.REVERSE);

		for(int i = 0; i < AALENGTH; i++) { aaleft[i] = aaright[i] = 0; }

		waitForStart();

		while (opModeIsActive())
		{
			for(int i = 0; i < AALENGTH - 1; i++)
			{
				aaleft[i] = aaleft[i+1];
				aaright[i] = aaright[i+1];
			}
			aaleft[AALENGTH-1] = gamepad1.left_stick_y;
			aaright[AALENGTH-1] = gamepad1.right_stick_y;

			motorLeft.setPower(Range.clip(gamepad1.left_stick_y, -Global.maxspeed, Global.maxspeed));
			motorRight.setPower(Range.clip(gamepad1.right_stick_y, -Global.maxspeed, Global.maxspeed));

			if(gamepad1.left_trigger >= THRESHOLD)
			{
				telemetry.addData("", "ADMINISTRATOR MODE ACTIVE");
				if(gamepad1.a && !a_pressed) Global.acceleration ^= true;
				if(gamepad1.dpad_up && !up_pressed) Global.maxspeed = Range.clip(Global.maxspeed += 0.05, 0.0, 1.0);
				if(gamepad1.dpad_down && !down_pressed) Global.maxspeed = Range.clip(Global.maxspeed -= 0.05, 0.0, 1.0);
			}

			telemetry.addData("Acceleration: ", Global.acceleration ? "On" : "Off");
			telemetry.addData("Max Speed: ", speedformat.format(Global.maxspeed));

			a_pressed = gamepad1.a;
			up_pressed = gamepad1.dpad_up;
			down_pressed = gamepad1.dpad_down;

			waitOneFullHardwareCycle();
		}
	}
}
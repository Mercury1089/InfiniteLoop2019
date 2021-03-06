package frc.robot.commands;

import edu.wpi.first.wpilibj.command.PIDCommand;
import edu.wpi.first.wpilibj.filters.LinearDigitalFilter;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import frc.robot.Robot;
import frc.util.DelayableLogger;
import frc.util.Recallable;
import frc.util.config.DriveTrainSettings;
import frc.util.config.SensorsSettings;

import java.util.concurrent.TimeUnit;

/**
 * Turns the robot towards a target found with the PixyCam
 */
public class RotateToTarget extends PIDCommand implements Recallable<Double> {
	private final Logger LOG = LogManager.getLogger(RotateToTarget.class);
	private DelayableLogger SLOW_LOG = new DelayableLogger(LOG, 5, TimeUnit.SECONDS);
	private int counter;

	private final double MIN_PERCENT_VBUS;
	private final int ONTARGET_THRESHOLD = 3;

	private LinearDigitalFilter filter;
	private Gyro gyro;

	private double finalRotation = Double.NEGATIVE_INFINITY;

	/**
	 * Constructs this command with a setClawState degree to rotate.
	 */
	public RotateToTarget() {
		super( // Sloppy, but configurable
				DriveTrainSettings.getPIDValues("rotateToTarget")[0],
				DriveTrainSettings.getPIDValues("rotateToTarget")[1],
				DriveTrainSettings.getPIDValues("rotateToTarget")[2]
		);

		LOG.info(getName() + " Beginning constructor");
		requires(Robot.driveTrain);

		filter = LinearDigitalFilter.movingAverage(Robot.claw.getPixyCam(), 5);
		gyro = Robot.driveTrain.getGyro();

		MIN_PERCENT_VBUS = DriveTrainSettings.getRotMinPVBus();

		LOG.info("RotateToTarget constructed");
	}


	// Called just before this Command runs the first time
	protected void initialize() {
		filter.reset();

		double[] outputRange = DriveTrainSettings.getOutputRange("rotateRelative");

		int camResWidth = SensorsSettings.getCameraResolution().width;
		getPIDController().setInputRange(-camResWidth / 2, camResWidth / 2);
		getPIDController().setOutputRange(outputRange[0], outputRange[1]);

		//Set the controller to continuous AFTER setInputRange()
		getPIDController().setContinuous(true);
		getPIDController().setAbsoluteTolerance(5);

		getPIDController().setSetpoint(0);

		LOG.info("RotateToTarget initialized");
	}

	// Make this return true when this Command no longer needs to run execute()
	protected boolean isFinished() {
		if (getPIDController().onTarget()) {
			counter++;
		} else {
			counter = 0;
		}
		return counter > ONTARGET_THRESHOLD;
	}

	// Called once after isFinished returns true
	protected void end() {
		LOG.info("RotateToTarget ended");
		Robot.driveTrain.stop();
		finalRotation = gyro.getAngle();
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	protected void interrupted() {
		LOG.info("RotateToTarget interrupted");
		end();
	}

	@Override
	protected double returnPIDInput() {
		return filter.pidGet();
	}

	@Override
	protected void usePIDOutput(double output) {
		final double OUTPUT_LOG = output;
		if (getPIDController().onTarget())
			output = 0;
		else if (Math.abs(output) < MIN_PERCENT_VBUS)
			output = Math.signum(output) * MIN_PERCENT_VBUS;

		Robot.driveTrain.pidWrite(-output);
	}

	@Override
	public Double recall() {
		if (finalRotation > Double.NEGATIVE_INFINITY)
			return finalRotation;

		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.ROTATION;

	}
}


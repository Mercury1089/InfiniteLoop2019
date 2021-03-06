package frc.robot.commands;

import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.command.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import frc.robot.Robot;
import frc.robot.RobotMap;
import frc.util.DelayableLogger;

import java.util.concurrent.TimeUnit;

/**
 * Command that gives direct control of the elevator using the gamepad's joysticks
 */
public class ManualElevator extends Command {
    private final Logger LOG = LogManager.getLogger(ManualElevator.class);
    private final DelayableLogger SLOW_LOG = new DelayableLogger(LOG, 1, TimeUnit.SECONDS);

    private int counter = 0;

    public ManualElevator() {
        requires(Robot.elevator);

        LOG.info(getName() + " Constructed");
    }

    @Override
    protected void initialize() {
        LOG.info(getName() + " initialized");
    }

    @Override
    protected void execute() {
        SLOW_LOG.run(log -> log.debug(getName() + " executing"));

        Robot.elevator.getElevatorTalon().set(ControlMode.PercentOutput, Robot.oi.getY(RobotMap.DS_USB.GAMEPAD));
    }

    @Override
    protected void interrupted() {
        LOG.info(getName() + " interrupted");
    }

    @Override
    protected void end() {
        LOG.info(getName() + "elevator ended");
    }

    @Override
    protected boolean isFinished() {
        return false;
    }
}

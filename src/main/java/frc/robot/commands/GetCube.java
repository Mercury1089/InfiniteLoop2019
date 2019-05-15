package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import frc.robot.Robot;
import frc.robot.subsystems.Claw;
import frc.util.Recallable;

/**
 * Command group that calls both the AutoAlign command
 * and the DriveWithLIDAR command to autonomously target and
 * approach the cube.
 */
public class GetCube extends CommandGroup {
        private static Logger log = LogManager.getLogger(GetCube.class);
        private Command angleOriginator, distanceOriginator;

        public GetCube() {
            angleOriginator = new RotateToTarget();
            distanceOriginator = new DriveWithLIDAR(8, 0.7);
            log.info(getName() + " Beginning constructor");
            addSequential(angleOriginator);
            addParallel(new UseClaw(Claw.ClawState.GRAB));
            addSequential(distanceOriginator);
            log.info(getName() + " Created");
        }

    public Recallable<Double> getAngleOriginator() {
        return (Recallable<Double>) angleOriginator;
    }

    public Recallable<Double> getDistanceOriginator() {
        return (Recallable<Double>) distanceOriginator;
    }
}

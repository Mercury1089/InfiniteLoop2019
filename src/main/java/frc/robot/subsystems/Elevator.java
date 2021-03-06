package frc.robot.subsystems;

import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import frc.robot.RobotMap;
import frc.robot.commands.ManualElevator;
import frc.util.config.ManipulatorSettings;

/**
 * Subsystem encapsulating elevator mechanism to move claw up and down.
 */
public class Elevator extends Subsystem {
    private static Logger log = LogManager.getLogger(Elevator.class);

    private WPI_TalonSRX elevatorTalon;
    private WPI_VictorSPX elevatorVictorFollower;

    private DigitalInput limitSwitch;

    public static final double NORMAL_P_VAL = 0.1;
    public static final double CLIMBING_P_VAL = 0.15;

    /**
     * Enumeration of positions that the elevator can have.
     * This is more a representation of the target positions, and does not reflect
     * the exact height of the claw at any precise moment.
     */
    public enum ElevatorPosition {
        // TODO: Temporary Values
        SCALE_HIGH(80000.0, NORMAL_P_VAL, 0, 0),        // Scale at its highest point
        SCALE_LOW(65000.0, NORMAL_P_VAL, 0, 0),         // Scale at its lowest point
        INNER_STAGE(38000.0, NORMAL_P_VAL, 0, 0),       // Height of the inner stage
        SWITCH(25000.0, NORMAL_P_VAL, 0, 0),            // Above switch fence
        CUBE_LEVEL_3(25000, NORMAL_P_VAL, 0, 0),        // Top cube of three stacked
        CUBE_LEVEL_2(14000.0, NORMAL_P_VAL, 0, 0),      // Top cube of two stacked
        DRIVE_CUBE(7000.0, NORMAL_P_VAL, 0, 0),         // Height for driving around cube
        CLIMB(5000.0, CLIMBING_P_VAL, 0, 0),            // Position to raise to when climbing
        FLOOR(-2000.0, NORMAL_P_VAL, 0, 0);             // Elevator bottomed out

        public final double encPos;
        public final double pVal;
        public final double iVal;
        public final double dVal;


        /**
         * Creates an elevator position, storing the encoder ticks
         * representing the height that the elevator should be at,
         * as well as the P value to use to reach that level.
         *
         * @param ep encoder position, in ticks
         * @param kp p value between 0 and 1
         */
        ElevatorPosition(double ep, double kp, double ki, double kd) {
            encPos = ep;
            pVal = kp;
            iVal = ki;
            dVal = kd;
        }
    }

    private ElevatorPosition position;

    public static final double MAX_HEIGHT = ElevatorPosition.SCALE_HIGH.encPos;
    private double curHeight;

    /**
     * Creates a new elevator, using the specified CAN IDs for the
     * leader controller (Talon SRX) and follower controller (Victor SPX).

     *
     * @param talonID  Leader (Talon SRX) CAN ID
     * @param victorID Follower (Victor SPX) CAN ID
     */
    public Elevator(int talonID, int victorID) {
        elevatorTalon = new WPI_TalonSRX(talonID);
        elevatorTalon.setNeutralMode(NeutralMode.Brake);
        elevatorVictorFollower = new WPI_VictorSPX(victorID);
        elevatorVictorFollower.setNeutralMode(NeutralMode.Brake);

        elevatorVictorFollower.follow(elevatorTalon);
/*
        double[] pid = ManipulatorSettings.getElevatorPID();

        // TODO: get proper values when elevator is made.
        elevatorTalon.config_kP(DriveTrain.PRIMARY_PID_LOOP, pid[0], 10);
        elevatorTalon.config_kI(DriveTrain.PRIMARY_PID_LOOP, pid[1], 10);
        elevatorTalon.config_kD(DriveTrain.PRIMARY_PID_LOOP, pid[2], 10);
*/
        elevatorTalon.setSensorPhase(false);

        elevatorTalon.configNominalOutputForward(.125, 10);
        elevatorTalon.configNominalOutputReverse(-.125, 10);
        elevatorTalon.configPeakOutputForward(1, 10);
        elevatorTalon.configPeakOutputReverse(-1,10);

        elevatorTalon.configAllowableClosedloopError(0, 5, 10);

        elevatorTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, RobotMap.PID.PRIMARY_PID_LOOP, DriveTrain.TIMEOUT_MS);

        elevatorTalon.configSetParameter(ParamEnum.eClearPositionOnLimitR, 1, 0, 0, 10);
    }

    @Override
    protected void initDefaultCommand() {
        // setDefaultCommand(new UseElevator(ElevatorPosition.FLOOR));
        setDefaultCommand(new ManualElevator());
    }

    public WPI_TalonSRX getElevatorTalon() {
        return elevatorTalon;
    }

    public boolean isLimitSwitchClosed() {
        return elevatorTalon.getSensorCollection().isRevLimitSwitchClosed();
    }

    /**
     * Gets the current {@link ElevatorPosition} for the elevator.
     *
     * @return the current ElevatorPosition
     */
    public ElevatorPosition getPosition() {
        return position;
    }

    /**
     * Sets the {@link ElevatorPosition} for the elevator.
     *
     * @param ep the new ElevatorPosition to set
     * @param pid the pid values
     */
    public void setPosition(ElevatorPosition ep) {
        elevatorTalon.config_kP(DriveTrain.PRIMARY_PID_LOOP, ep.pVal, 10);
        elevatorTalon.config_kI(DriveTrain.PRIMARY_PID_LOOP, ep.iVal, 10);
        elevatorTalon.config_kD(DriveTrain.PRIMARY_PID_LOOP, ep.dVal, 10);

        position = ep;
    }

    /**
     * Get current height of claw on elevator.
     *
     * @return height of claw as read by the encoder, in ticks
     */
    public int getCurrentHeight() {
        return elevatorTalon.getSelectedSensorPosition(RobotMap.PID.PRIMARY_PID_LOOP);
    }


    /*public void toggleClimbState() {
        double[] pid = ManipulatorSettings.getClimbingPID();

        elevatorTalon.config_kP(DriveTrain.PRIMARY_PID_LOOP, pid[0], 10);
        elevatorTalon.config_kI(DriveTrain.PRIMARY_PID_LOOP, pid[1], 10);
        elevatorTalon.config_kD(DriveTrain.PRIMARY_PID_LOOP, pid[2], 10);
    }*/
}
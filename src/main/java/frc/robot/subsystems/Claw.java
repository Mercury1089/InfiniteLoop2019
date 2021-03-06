package frc.robot.subsystems;

import com.ctre.phoenix.CANifier;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.filters.LinearDigitalFilter;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import frc.robot.Robot;
import frc.robot.commands.UseClaw;
import frc.robot.sensors.LIDAR;
import frc.robot.sensors.PixyI2C;
import frc.robot.sensors.Ultrasonic;
import frc.util.MercMath;
import frc.util.config.SensorsSettings;

/**
 * Subsystem that encapsulates actuators and sensors for
 * claw mechanism
 */
public class Claw extends Subsystem {
    private static Logger log = LogManager.getLogger(Claw.class);
    public final double MIN_INCHES = 10.0;

    private WPI_VictorSPX
        clawMotor_M,
        clawMotor_S;

    // All sensors belong to the claw, I guess?
    private LIDAR lidar;
    private CANifier canifier;
    private PixyI2C pixyCam;
    private Ultrasonic ultrasonic;

    private int[] currentLEDOutput = new int[3];
    private LinearDigitalFilter linearDF;

    private boolean hasCube;
    private boolean hasCubeUltrasonic;
    private boolean ejecting;

    public enum ClawState {
        GRAB(1.0), EJECT(-1.0), STOP(0.0), SLOW_EJECT(-0.4);
        public final double SPEED;

        ClawState(double speed) {
            SPEED = speed;
        }
    }

    /**
     * Constructs a new claw, specifying the ports for the ledaer SPX, follower SPX,
     * and all the sensors connected to it.
     *
     * @param usPort    Ultrasonic port
     * @param cfID      CANifier CAN ID
     * @param lidarPort LIDAR port
     * @param leadID    Leader (Victor SPX) CAN ID
     * @param folID     Follower (Victor SPX) CAN ID
     */
    public Claw(int usPort, int cfID, int lidarPort, int leadID, int folID) {
        canifier = new CANifier(cfID);

        clawMotor_S = new WPI_VictorSPX(folID);
        clawMotor_M = new WPI_VictorSPX(leadID);

        pixyCam = new PixyI2C();

        ultrasonic = new Ultrasonic(usPort);

        // Clamp pwm id between 0 and 3
        LIDAR.PWMOffset offset = SensorsSettings.getLidarEquation();

        hasCube = false;

        this.lidar = new LIDAR(canifier, CANifier.PWMChannel.valueOf(lidarPort), offset);

        clawMotor_M.configPeakOutputForward(1.0, 0);
        clawMotor_S.configPeakOutputForward(1.0, 0);

        setName("Claw");
        log.info("Initalized claw");
        clawMotor_M.setInverted(false);
        clawMotor_S.setInverted(true);
        clawMotor_S.follow(clawMotor_M);
    }

    @Override
    public void periodic() {
        lidar.updatePWMInput();
        updateState();
    }

    /**
     * Updates the states of the LEDs and the gamepad rumble
     * based on whether or not the cube is in range or is being held.
     */
    private void updateState() {
        double rumbleVal = 0.0;

        if (RobotState.isDisabled()) { // Turn off LEDs when disabled
            currentLEDOutput[0] = 0;
            currentLEDOutput[1] = 0;
            currentLEDOutput[2] = 0;
        } else if (hasCube()) { // Have cube?
            // Listen from SmartDash

            // Fun colors to note:
            // Orange (In range): 255, 30, 0
            // Purple (Something about a cube): 255, 0, 255
            // Cyan (Very nice color): 0, 255, 255
            //int r = (int) SmartDashboard.getNumber("LED Color (R)", 255);
            //int g = (int) SmartDashboard.getNumber("LED Color (G)", 161);
            //int b = (int) SmartDashboard.getNumber("LED Color (B)", 0);
            currentLEDOutput[0] = 0;
            currentLEDOutput[1] = 255;
            currentLEDOutput[2] = 0;
        } else if (Math.abs(pixyCam.pidGet()) <= 20) { // If displacement is within 3 pixels of center
            currentLEDOutput[0] = 255;
            currentLEDOutput[1] = 0;
            currentLEDOutput[2] = 255;
            rumbleVal = lidar.getRumbleVal();
        } else if (pixyCam.inRange()) { // Cube is in range to auto pickup
            // Yellow
            currentLEDOutput[0] = 255;
            currentLEDOutput[1] = 161;
            currentLEDOutput[2] = 0;
            rumbleVal = lidar.getRumbleVal();
        } else {
            // None
            currentLEDOutput[0] = 0;
            currentLEDOutput[1] = 0;
            currentLEDOutput[2] = 0;
        }

        colorLED(currentLEDOutput[0], currentLEDOutput[1], currentLEDOutput[2]);
        Robot.oi.rumbleController(rumbleVal);
    }

    /**
     * Sets the color of the LED based on RBG int values
     *
     * @param r red value [0 - 255]
     * @param g green value [0 - 255]
     * @param b blue value [0 - 255]
     */
    private void colorLED(int r, int g, int b) {
        canifier.setLEDOutput((double) b / 255.0, CANifier.LEDChannel.LEDChannelA);
        canifier.setLEDOutput((double) r / 255.0, CANifier.LEDChannel.LEDChannelB);
        canifier.setLEDOutput((double) g / 255.0, CANifier.LEDChannel.LEDChannelC);
    }

    public int[] getCurrentLEDOutput() {
        return currentLEDOutput;
    }

    @Override
    protected void initDefaultCommand() {
        setDefaultCommand(null);
    }

    public void setClawState(ClawState state) {
        clawMotor_M.set(state.SPEED);
    }

    public CANifier getCanifier() {
        return canifier;
    }

    public LIDAR getLidar() {
        return lidar;
    }

    public PixyI2C getPixyCam() {
        return pixyCam;
    }

    public Ultrasonic getUltrasonic() {
        return ultrasonic;
    }

    public WPI_VictorSPX getClawMotor_M() {
        return clawMotor_M;
    }

    public WPI_VictorSPX getClawMotor_S() {
        return clawMotor_S;
    }

    public boolean hasCube() {
        return lidar.getDistance() <= 6;
    }

    public boolean getEjecting() {
        return ejecting;
    }

    public void setEjecting(boolean b) {
        ejecting = b;
    }
}

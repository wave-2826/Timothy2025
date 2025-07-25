package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * This class defines the runtime mode used by AdvantageKit. The mode is always "real" when running on a roboRIO. Change
 * the value of "simMode" to switch between "sim" (physics sim) and "replay" (log replay from a file).
 */
public final class Constants {
    public static final Mode simMode = Mode.SIM;

    /** If the robot should log data in simulation. */
    public static final boolean logInSimulation = false;
    /**
     * Whether to use NetworkTables instead of RLog for AdvantageScope logging. RLog _significantly_ reduces lag in
     * AdvantageScope.
     */
    public static final boolean useNTLogs = false;

    public static final boolean tuningMode = true;

    public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

    public static boolean isSim = currentMode == Mode.SIM;

    public static final boolean twoDriverMode = false;

    public static final double voltageCompensation = 12.0;

    public static enum Mode {
        /** Running on a real robot. */
        REAL,

        /** Running a physics simulator. */
        SIM,

        /** Replaying from a log file. */
        REPLAY
    }
}

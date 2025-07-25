package frc.robot.util;

import com.revrobotics.REVLibError;
import com.revrobotics.spark.SparkBase;

import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Timer;

import static edu.wpi.first.units.Units.Seconds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.ironmaple.simulation.SimulatedArena;

public class SparkUtil {
    private static final boolean ENABLE_ALERT_TRACKING = false;

    /** Stores whether any error was has been detected by other utility methods. */
    public static boolean sparkStickyFault = false;

    /** Processes a value from a Spark only if the value is valid. */
    public static void ifOk(SparkBase spark, DoubleSupplier supplier, DoubleConsumer consumer) {
        double value = supplier.getAsDouble();
        if(spark.getLastError() == REVLibError.kOk) {
            consumer.accept(value);
        } else {
            sparkStickyFault = true;
        }
    }

    /** Processes a value from a Spark only if the value is valid. */
    public static void ifOk(SparkBase spark, DoubleSupplier[] suppliers, Consumer<double[]> consumer) {
        double[] values = new double[suppliers.length];
        for(int i = 0; i < suppliers.length; i++) {
            values[i] = suppliers[i].getAsDouble();
            if(spark.getLastError() != REVLibError.kOk) {
                sparkStickyFault = true;
                return;
            }
        }
        consumer.accept(values);
    }

    /** Attempts to run the command until no error is produced. */
    public static void tryUntilOk(SparkBase spark, int maxAttempts, Supplier<REVLibError> command) {
        for(int i = 0; i < maxAttempts; i++) {
            var error = command.get();
            if(error == REVLibError.kOk) {
                break;
            } else {
                sparkStickyFault = true;
            }
        }
    }

    private static class SparkFaultAlerts {
        private final SparkBase spark;
        private final Alert[] faultAlerts;
        private final Alert[] warningAlerts;

        public SparkFaultAlerts(SparkBase spark, String name) {
            this.spark = spark;
            this.faultAlerts = new Alert[] {
                // public final boolean other;
                // public final boolean motorType;
                // public final boolean sensor;
                // public final boolean can;
                // public final boolean temperature;
                // public final boolean gateDriver;
                // public final boolean escEeprom;
                // public final boolean firmware;

                new Alert(name + " other fault", Alert.AlertType.kError),
                new Alert(name + " motor type fault", Alert.AlertType.kError),
                new Alert(name + " sensor fault", Alert.AlertType.kError),
                new Alert(name + " can fault", Alert.AlertType.kError),
                new Alert(name + " temperature fault", Alert.AlertType.kError),
                new Alert(name + " gate driver fault", Alert.AlertType.kError),
                new Alert(name + " esc eeprom fault", Alert.AlertType.kError),
                new Alert(name + " firmware fault", Alert.AlertType.kError)
            };
            this.warningAlerts = new Alert[] {
                // public final boolean brownout;
                // public final boolean overcurrent;
                // public final boolean escEeprom;
                // public final boolean extEeprom;
                // public final boolean sensor;
                // public final boolean stall;
                // public final boolean hasReset;
                // public final boolean other;

                null, //
                new Alert(name + " brownout warning", Alert.AlertType.kWarning),
                new Alert(name + " overcurrent warning", Alert.AlertType.kWarning),
                new Alert(name + " esc eeprom warning", Alert.AlertType.kWarning),
                new Alert(name + " ext eeprom warning", Alert.AlertType.kWarning),
                new Alert(name + " sensor warning", Alert.AlertType.kWarning),
                new Alert(name + " stall warning", Alert.AlertType.kWarning), //
                null, // new Alert(name + " has reset warning", Alert.AlertType.kWarning),
                new Alert(name + " other warning", Alert.AlertType.kWarning)
            };
        }

        public void updateAlerts() {
            var faults = spark.getFaults();
            int faultBits = faults.rawBits;
            int warningBits = spark.getWarnings().rawBits;

            if(faults.can) {
                faultBits = 1 >> 3;
                warningBits = 0;
            }

            for(int i = 0; i < faultAlerts.length; i++) {
                if(faultAlerts[i] != null) faultAlerts[i].set(((faultBits >> i) & 1) == 1);
            }
            for(int i = 0; i < warningAlerts.length; i++) {
                if(warningAlerts[i] != null) warningAlerts[i].set(((warningBits >> i) & 1) == 1);
            }
        }
    }

    private static final List<SparkFaultAlerts> sparkFaultAlerts = new ArrayList<>();

    /** Registers a spark to show alerts if there are any active motor controller faults or warnings. */
    public static void registerSparkFaultAlerts(SparkBase spark, String name) {
        sparkFaultAlerts.add(new SparkFaultAlerts(spark, name));
    }

    private static Timer updateFaultsTimer = new Timer();
    static {
        updateFaultsTimer.start();
    }

    /** Updates all registered spark fault alerts. This should probably be called relatively infrequently. */
    @SuppressWarnings("unused")
    public static void updateSparkFaultAlerts() {
        if(updateFaultsTimer.advanceIfElapsed(0.25) && ENABLE_ALERT_TRACKING) {
            for(SparkFaultAlerts sparkFaultAlert : sparkFaultAlerts) {
                sparkFaultAlert.updateAlerts();
            }
        }
    }

    public static double[] getSimulationOdometryTimestamps() {
        final double[] odometryTimeStamps = new double[SimulatedArena.getSimulationSubTicksIn1Period()];
        for(int i = 0; i < odometryTimeStamps.length; i++) {
            odometryTimeStamps[i] = Timer.getFPGATimestamp() - 0.02 + i * SimulatedArena.getSimulationDt().in(Seconds);
        }
        return odometryTimeStamps;
    }
}

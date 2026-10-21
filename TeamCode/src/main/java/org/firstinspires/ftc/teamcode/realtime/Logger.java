/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Realtime logger to deal with :
   - Logging in realtime in logcat
   - Logging in panels
   - Logging on driver station
   ------------------------------------------------------- */

package org.firstinspires.ftc.teamcode.realtime;

/* System includes */
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

/* Android includes */
import android.util.Log;

/* Json includes */
import org.json.JSONException;
import org.json.JSONObject;

/* FTC Controller includes */
import org.firstinspires.ftc.robotcore.external.Telemetry;

/* Panels includes */
import com.bylazar.configurables.PanelsConfigurables;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.TelemetryManager;

/* Configuration includes */
import org.firstinspires.ftc.teamcode.configuration.ConfigurableItem;

@Configurable
public class Logger implements ConfigurableItem {

    static final int  sStackLevel = 3;

    static public Level LEVEL = Level.VERBOSE;

    public enum Target {
        SYSTEM,
        DASHBOARD,
        DRIVER_STATION
    }

    public enum Level {
        ERROR(Log.ERROR,"error"),
        WARNING(Log.WARN,"warning"),
        INFO(Log.INFO,"info"),
        DEBUG(Log.DEBUG,"debug"),
        VERBOSE(Log.VERBOSE,"verbose");

        private int     mValue;

        private String  mName;

        private Level(int value, String name) {
            mValue = value;
            mName = name;
        }

        int     getValue() { return mValue; }
        String  getName()  { return mName;  }

    }


    private static final Map<String, Integer > sConfToLevel = Map.of(
            "error",    Log.ERROR,
            "warning",  Log.WARN,
            "info",     Log.INFO,
            "debug",    Log.DEBUG,
            "verbose",  Log.VERBOSE
    );

    // Json keys
    static  final   String          sDriverStationKey  = "driver-station";
    static  final   String          sDashboardKey      = "dashboard";
    static  final   String          sSystemKey         = "system";
    static  final   String          sLevelKey          = "level";


    // Status
    boolean                                 mConfigurationValid;
    final int                               mStackLevel;

    // Change detection (Panels mutates LEVEL static — hasChanged() compares
    // against this cached value and updates it)
    Level                                   mLastKnownLevel;

    // Loggers
    Telemetry                               mDriverStation;
    TelemetryManager                        mDashboard;
    boolean                                 mShallUseSystemLogs;

    // Persistence
    final Map<Target,List<StringBuilder>>   mErrors;
    final Map<Target,List<StringBuilder>>   mWarnings;
    final Map<Target,Map<String,String>>    mMetrics;

    // Temporary
    final Map<Target,List<StringBuilder>>   mInfos;
    final Map<Target,List<StringBuilder>>   mDebugs;
    final Map<Target,List<StringBuilder>>   mVerboses;

    /**
     * Builds a log manager from parameters
     *
     * @param station the driver station telemetry from opmode (may be null if shall not be used for logging)
     * @param dashboard the panels telemetry instance (may be null if shall not be used for logging)
     * @param shallUseSystemLog the boolean value stating if system logs should be used
     */
    public Logger(Telemetry station, TelemetryManager dashboard, boolean shallUseSystemLog) {
        this(station, dashboard, shallUseSystemLog, sStackLevel);
    }

    /**
     * Builds a log manager from parameters
     *
     * @param station the driver station telemetry from opmode (may be null if shall not be used for logging)
     * @param dashboard the panels telemetry instance (may be null if shall not be used for logging)
     * @param shallUseSystemLog the boolean value stating if system logs should be used
     * @param stackLevel the stack level to get the function name
     */
    public Logger(Telemetry station, TelemetryManager dashboard, boolean shallUseSystemLog, int stackLevel) {

        mConfigurationValid = true;
        LEVEL       = Level.VERBOSE;
        mStackLevel = stackLevel;

        mErrors   = new LinkedHashMap<>();
        mWarnings = new LinkedHashMap<>();
        mInfos    = new LinkedHashMap<>();
        mDebugs   = new LinkedHashMap<>();
        mVerboses = new LinkedHashMap<>();
        mMetrics  = new LinkedHashMap<>();
        for(Target target : Target.values()) {

            mErrors.put(target,new ArrayList<>());
            mWarnings.put(target,new ArrayList<>());
            mInfos.put(target,new ArrayList<>());
            mDebugs.put(target,new ArrayList<>());
            mVerboses.put(target,new ArrayList<>());
            mMetrics.put(target, new LinkedHashMap<>());

        }

        mDriverStation = station;
        if(mDriverStation != null) { mDriverStation.setAutoClear(true); }

        mDashboard = dashboard;
        if(mDashboard != null)     { mDashboard.setUpdateInterval(10); }

        mShallUseSystemLogs = shallUseSystemLog;

        mLastKnownLevel = LEVEL;
    }


    /**
     * Minimal level setter
     *
     * @param level minimal severity to log
     */
    public void level(Level level) {
        LEVEL = level;
        PanelsConfigurables.INSTANCE.refreshClass(this);
    }

    /**
     * Configuration checking
     *
     * @return true if object is correctly configured, false otherwise
     */
    public boolean isConfigured() {
        return mConfigurationValid;
    }

    /**
     * Configuration logging into text
     *
     * @return configuration as basic string
     */
    public String  logConfiguration(String header) {

        return header +
                "> " +
                sDriverStationKey +
                ((mDriverStation == null) ? " : false" : " : true") +
                "\n" +
                header +
                "> " +
                sDashboardKey +
                ((mDashboard == null) ? " : false" : " : true") +
                "\n" +
                header +
                "> " +
                sSystemKey + " : " +
                mShallUseSystemLogs +
                "\n" +
                header +
                "> " +
                sLevelKey + " : " +
                LEVEL.getName() +
                "\n";
    }

    /**
     * Reads log manager configuration
     *
     * @param reader : JSON object containing configuration
     */
    public void read(JSONObject reader) {

        mConfigurationValid = true;

        if(reader.has(sSystemKey)) {
            try {
                mShallUseSystemLogs = reader.getBoolean(sSystemKey);
            }
            catch(JSONException  e) {
                this.error("Error in system logging configuration");
                mConfigurationValid = false;
            }
        }
        else { mShallUseSystemLogs = false; }

        if(reader.has(sDriverStationKey)) {
            try {
                boolean shallUseStation = reader.getBoolean(sDriverStationKey);
                if(shallUseStation && mDriverStation == null) {
                    this.warning("Telemetry not provided so can't log to driver station");
                    mConfigurationValid = false;
                }
                if(!shallUseStation) { mDriverStation = null; }
            }
            catch(JSONException e) {
                this.error("Error in driver station logging configuration");
                mDriverStation = null;
                mConfigurationValid = false;
            }
        }
        else { mDriverStation = null; }

        if(reader.has(sDashboardKey)) {
            try {
                boolean shallUseDashboard = reader.getBoolean(sDashboardKey);
                if(shallUseDashboard && mDashboard == null) {
                    this.warning("Dashboard not provided so can't log to dashboard");
                    mConfigurationValid = false;
                }
                if(!shallUseDashboard) { mDashboard = null; }
            }
            catch(JSONException e) {
                this.error("Error in dashboard logging configuration");
                mDashboard = null;
                mConfigurationValid = false;
            }
        }
        else { mDashboard = null; }

        if(reader.has(sLevelKey)) {
            try {
                String level = reader.getString(sLevelKey);
                if(sConfToLevel.containsKey(level)) {
                    if(level.equals(Level.VERBOSE.getName())) { LEVEL = Level.VERBOSE; }
                    if(level.equals(Level.DEBUG.getName()))   { LEVEL = Level.DEBUG;   }
                    if(level.equals(Level.INFO.getName()))    { LEVEL = Level.INFO;    }
                    if(level.equals(Level.WARNING.getName())) { LEVEL = Level.WARNING; }
                    if(level.equals(Level.ERROR.getName()))   { LEVEL = Level.ERROR;   }
                    PanelsConfigurables.INSTANCE.refreshClass(this);
                }
                else { this.warning("Level " + level + " is not managed"); }
            }
            catch(JSONException e) {
                this.error("Error in dashboard logging configuration");
                mDashboard = null;
                mConfigurationValid = false;
            }
        }

    }

    /**
     * Writes log manager configuration
     *
     * @param writer : JSON object to store configuration
     */
    public void write(JSONObject writer) {

        try {
            if(mConfigurationValid) {
                writer.put(sSystemKey, mShallUseSystemLogs);
                if (mDriverStation != null) {
                    writer.put(sDriverStationKey, true);
                }
                if (mDashboard != null) {
                    writer.put(sDashboardKey, true);
                }
                // Always serialize the current (possibly Panels-mutated) level
                writer.put(sLevelKey, LEVEL.getName());
            }
        }
        catch(JSONException e ) { this.error("Error writing configuration"); }
    }

    /**
     * Logger has no natural periodic hook, so it does change detection
     * directly inside hasChanged(): compares Panels-mutable LEVEL static
     * against the cached value, updates the cache, reports the diff.
     */
    @Override
    public boolean hasChanged() {
        if (LEVEL != mLastKnownLevel) {
            mLastKnownLevel = LEVEL;
            return true;
        }
        return false;
    }

    /**
     * Add an error to a specific log sink
     *
     * @param target the log sink
     * @param message the error message
     */
    public void error(Target target, String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        this.error(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
    }

    /**
     * Add an error to all log sinks
     *
     * @param message the error message
     */
    public void error(String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        for(Target target : Target.values()) {
            this.error(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
        }
    }

    /**
     * Add a warning to a specific log sink
     *
     * @param target the log sink
     * @param message the warning message
     */
    public void warning(Target target, String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        this.warning(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
    }

    /**
     * Add a warning to all log sinks
     *
     * @param message the warning message
     */
    public void warning(String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        for(Target target : Target.values()) {
            this.warning(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
        }
    }

    /**
     * Add a metric to all log sinks
     *
     * @param metric the metric topic
     * @param value the metric value
     */
    public void metric(String metric, String value) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        for(Target target : Target.values()) {
            this.metric(target, metric, value, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
        }
    }

    /**
     * Add a metric to a specific log sink
     *
     * @param target the log sink
     * @param metric the metric topic
     * @param value the metric value
     */
    public void metric(Target target, String metric, String value) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        this.metric(target, metric, value, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
    }

    /**
     * Add an info to all log sinks
     *
     * @param message the info message
     */
    public void info(String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        for(Target target : Target.values()) {
            this.info(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
        }
    }

    /**
     * Add an info to a specific log sink
     *
     * @param target the log sink
     * @param message the info message
     */
    public void info(Target target, String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        this.info(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
    }

    /**
     * Add a debug to all log sinks
     *
     * @param message the debug message
     */
    public void debug(String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        for(Target target : Target.values()) {
            this.debug(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
        }
    }

    /**
     * Add a debug to a specific log sink
     *
     * @param target the log sink
     * @param message the debug message
     */
    public void debug(Target target, String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        this.debug(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
    }

    /**
     * Add a verbose to all log sinks
     *
     * @param message the verbose message
     */
    public void verbose(String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        for(Target target : Target.values()) {
            this.verbose(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
        }
    }

    /**
     * Add a verbose to a specific log sink
     *
     * @param target the log sink
     * @param message the verbose message
     */
    public void verbose(Target target, String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[mStackLevel];
        this.verbose(target, message, element.getFileName().substring(0, element.getFileName().lastIndexOf(".")), element.getMethodName(), element.getLineNumber());
    }

    /**
     * Write current log text to a specific log sink
     *
     * @param target the target log sink
     */
    public void write(Target target) {
        if (target == Target.DRIVER_STATION && mDriverStation != null) {
            mDriverStation.addLine("---------- ERRORS ----------");
            mDriverStation.addLine(Objects.requireNonNull(mErrors.get(Target.DRIVER_STATION)).toString());
            mDriverStation.addLine("--------- WARNINGS ---------");
            mDriverStation.addLine(Objects.requireNonNull(mWarnings.get(Target.DRIVER_STATION)).toString());
            mDriverStation.addLine("---------- METRICS ---------");
            for (Map.Entry<String, String> metric : Objects.requireNonNull(mMetrics.get(target)).entrySet()) {
                mDriverStation.addLine(metric.getKey() + " : " + metric.getValue());
            }
            mDriverStation.addLine("");
            mDriverStation.addLine("---------- INFOS ----------");
            mDriverStation.addLine(Objects.requireNonNull(mInfos.get(Target.DRIVER_STATION)).toString());
            mDriverStation.addLine("---------- DEBUGS ---------");
            mDriverStation.addLine(Objects.requireNonNull(mDebugs.get(Target.DRIVER_STATION)).toString());
            mDriverStation.addLine("---------- VERBOSES ----------");
            mDriverStation.addLine(Objects.requireNonNull(mVerboses.get(Target.DRIVER_STATION)).toString());

        } else if (target == Target.DASHBOARD && mDashboard != null) {
            StringBuilder persistent = new StringBuilder();

            mDashboard.addLine("ERRORS");
            for (StringBuilder element : Objects.requireNonNull(mErrors.get(Target.DASHBOARD))) {
                mDashboard.addLine(element.toString());
            }

            mDashboard.addLine("WARNINGS");
            for (StringBuilder element : Objects.requireNonNull(mWarnings.get(Target.DASHBOARD))) {
                mDashboard.addLine(element.toString());
            }

            mDashboard.addLine("INFOS");
            for (StringBuilder element : Objects.requireNonNull(mInfos.get(Target.DASHBOARD))) {
                mDashboard.addLine(element.toString());
            }

            mDashboard.addLine("DEBUG");
            for (StringBuilder element : Objects.requireNonNull(mDebugs.get(Target.DASHBOARD))) {
                mDashboard.addLine(element.toString());
            }

            mDashboard.addLine("VERBOSE");
            for (StringBuilder element : Objects.requireNonNull(mVerboses.get(Target.DASHBOARD))) {
                mDashboard.addLine(element.toString());
            }

        }
    }

    public void raw(Target target, String raw) {
        if (target == Target.DRIVER_STATION && mDriverStation != null) {
            Objects.requireNonNull(mInfos.get(target)).add(new StringBuilder(raw));
        } else if (target == Target.DASHBOARD && mDashboard != null) {
            Objects.requireNonNull(mInfos.get(target)).add(new StringBuilder(raw));
        } else if (target == Target.SYSTEM && mShallUseSystemLogs) {
            Log.i("",raw);
        }
    }

    /**
     * Write logs accumulated by a specific log sink
     *
     * @param target the target log sink
     */
    public void update(Target target) {
        if (target == Target.DRIVER_STATION && mDriverStation != null) {
            this.write(target);
            mDriverStation.update();
        } else if (target == Target.DASHBOARD && mDashboard != null) {
            this.write(target);
            mDashboard.update();
        }
        mInfos.put(target,new ArrayList<>());
        mDebugs.put(target,new ArrayList<>());
        mVerboses.put(target,new ArrayList<>());
    }

    /**
     * Write logs accumulated by all log sinks. Called each loop tick.
     */
    public void periodic() {
        for(Target target : Target.values()) {
            this.update(target);
        }
    }

    /**
     * Clear all logs for a specific sink - persisted ones are not lost
     *
     * @param target the target log sink
     */
    public void clear(Target target) {
        if (target == Target.DRIVER_STATION && mDriverStation != null) {
            mDriverStation.clear();
        } else if (target == Target.DASHBOARD && mDashboard != null) {
            mDashboard.getWrapper().clear();
        }

    }

    /**
     * Clear logs for all sinks - persisted ones are not lost
     */
    public void clear() {
        for(Target target : Target.values()) {
            this.clear(target);
        }
    }
    /**
     * Reset all logs for a specific sink - persisted ones are lost
     *
     * @param target the target log sink
     */
    public void reset(Target target) {
        mErrors.put(target,new ArrayList<>());
        mWarnings.put(target,new ArrayList<>());
        mInfos.put(target,new ArrayList<>());
        mDebugs.put(target,new ArrayList<>());
        mVerboses.put(target,new ArrayList<>());
        mMetrics.put(target, new LinkedHashMap<>());
    }

    /**
     * Reset logs for all sinks - persisted ones are lost
     */
    public void reset() {
        for(Target target : Target.values()) {
            this.reset(target);
        }
    }


    /**
     * stop logging for all sinks - last ones are flushed
     */
    public void stop() {
        for(Target target : Target.values()) {
            this.write(target);
        }
    }

    /**
     * Add an error to a log sink
     *
     * @param target the log sink
     * @param message the error description
     * @param className the name of the class from which log is issued
     * @param methodName the name of the method issuing the log
     * @param line the line from which log is issued
     */
    private void error(Target target, String message, String className, String methodName, int line) {

        String line_header = (line < 1000 ? (line < 100 ? (line < 10 ? "000" : "00") : "0") : "");
        if( LEVEL.getValue() <= Log.ERROR) {

            switch (target) {
                case DASHBOARD:
                    if (mDashboard != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(className)
                                .append(".")
                                .append(methodName)
                                .append(":")
                                .append(line_header).append(line)
                                .append(" - ")
                                .append(message);
                        Objects.requireNonNull(mErrors.get(target)).add(temp);
                    }
                    break;
                case DRIVER_STATION:
                    if (mDriverStation != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(className)
                                .append(".")
                                .append(methodName)
                                .append(":")
                                .append(line_header).append(line)
                                .append(" - ")
                                .append(message);
                        Objects.requireNonNull(mErrors.get(target)).add(temp);
                    }
                    break;
                case SYSTEM:
                    if(mShallUseSystemLogs) {
                        Log.e(className + "." + methodName + ":" + line_header + line,message);
                    }
                    break;
            }
        }

    }

    /**
     * Add a warning to a log sink
     *
     * @param target the log sink
     * @param message the warning description
     * @param className the name of the class from which log is issued
     * @param methodName the name of the method issuing the log
     * @param line the line from which log is issued
     */
    private void warning(Target target, String message, String className, String methodName, int line) {

        String line_header = (line < 1000 ? (line < 100 ? (line < 10 ? "000" : "00") : "0") : "");
        if( LEVEL.getValue() <= Log.WARN) {

            switch (target) {
                case DASHBOARD:
                    if (mDashboard != null) {

                        StringBuilder temp = new StringBuilder();
                        temp.append(className)
                                .append(".")
                                .append(methodName)
                                .append(":")
                                .append(line_header).append(line)
                                .append(" - ")
                                .append(message);
                        Objects.requireNonNull(mWarnings.get(target)).add(temp);
                    }
                    break;
                case DRIVER_STATION:
                    if (mDriverStation != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(className)
                                .append(".")
                                .append(methodName)
                                .append(":")
                                .append(line_header).append(line)
                                .append(" - ")
                                .append(message);
                        Objects.requireNonNull(mWarnings.get(target)).add(temp);
                    }
                    break;
                case SYSTEM:
                    if(mShallUseSystemLogs) {
                        Log.w(className + "." + methodName + ":" + line_header + line,message);
                    }
                    break;
            }
        }

    }

    /**
     * Add a metric to a log sink
     *
     * @param target the log sink
     * @param metric the metric topic
     * @param value the metric value
     * @param className the name of the class from which log is issued
     * @param methodName the name of the method issuing the log
     * @param line the line from which log is issued
     */

    private void metric(Target target, String metric, String value, String className, String methodName, int line) {

        String line_header = (line < 1000 ? (line < 100 ? (line < 10 ? "000" : "00") : "0") : "");
        if( LEVEL.getValue() <= Log.INFO) {

            switch (target) {
                case DASHBOARD:
                    Objects.requireNonNull(mMetrics.get(target)).put(metric, value);
                    if (mDashboard != null) {
                        mDashboard.getWrapper().addData(metric, value);
                    }
                    break;
                case DRIVER_STATION:
                    Objects.requireNonNull(mMetrics.get(target)).put(metric, value);
                    break;

                case SYSTEM:
                    if(mShallUseSystemLogs) {
                        Log.i(className + "." + methodName + ":" + line_header + line, metric + " " + value);
                    }
                    break;
            }
        }
    }

    /**
     * Add an info to a log sink
     *
     * @param target the log sink
     * @param message the info message
     * @param className the name of the class from which log is issued
     * @param methodName the name of the method issuing the log
     * @param line the line from which log is issued
     */
    private void info(Target target, String message, String className, String methodName, int line) {

        String line_header = (line < 1000 ? (line < 100 ? (line < 10 ? "000" : "00") : "0") : "");
        if( LEVEL.getValue() <= Log.INFO) {
            switch (target) {
                case DASHBOARD:
                    if (mDashboard != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(message);
                        Objects.requireNonNull(mInfos.get(target)).add(temp);
                    }
                    break;
                case DRIVER_STATION:
                    if (mDriverStation != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(message);
                        Objects.requireNonNull(mInfos.get(target)).add(temp);
                    }
                    break;
                case SYSTEM:
                    if(mShallUseSystemLogs) {
                        Log.i(className + "." + methodName + ":" + line_header + line,message);
                    }
                    break;
            }
        }
    }

    /**
     * Add a debug to a log sink
     *
     * @param target the log sink
     * @param message the info message
     * @param className the name of the class from which log is issued
     * @param methodName the name of the method issuing the log
     * @param line the line from which log is issued
     */
    private void debug(Target target, String message, String className, String methodName, int line) {

        String line_header = (line < 1000 ? (line < 100 ? (line < 10 ? "000" : "00") : "0") : "");
        if( LEVEL.getValue() <= Log.DEBUG) {

            switch (target) {
                case DASHBOARD:
                    if (mDashboard != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(message);
                        Objects.requireNonNull(mDebugs.get(target)).add(temp);
                    }
                    break;
                case DRIVER_STATION:
                    if (mDriverStation != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(message);
                        Objects.requireNonNull(mDebugs.get(target)).add(temp);
                    }
                    break;
                case SYSTEM:
                    if(mShallUseSystemLogs) {
                        Log.d(className + "." + methodName + ":" + line_header + line,message);
                    }
                    break;
            }
        }
    }

    /**
     * Add a verbose to a log sink
     *
     * @param target the log sink
     * @param message the info message
     * @param className the name of the class from which log is issued
     * @param methodName the name of the method issuing the log
     * @param line the line from which log is issued
     */
    private void verbose(Target target, String message, String className, String methodName, int line) {

        String line_header = (line < 1000 ? (line < 100 ? (line < 10 ? "000" : "00") : "0") : "");
        if( LEVEL.getValue() <= Log.VERBOSE) {

            switch (target) {
                case DASHBOARD:
                    if (mDashboard != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(message);
                        Objects.requireNonNull(mVerboses.get(target)).add(temp);
                    }
                    break;
                case DRIVER_STATION:
                    if (mDriverStation != null) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(message);
                        Objects.requireNonNull(mVerboses.get(target)).add(temp);
                    }
                    break;
                case SYSTEM:
                    if(mShallUseSystemLogs) {
                        Log.v(className + "." + methodName + ":" + line_header + line,message);
                    }
                    break;
            }
        }
    }
}
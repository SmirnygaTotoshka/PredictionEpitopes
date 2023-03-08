import org.json.simple.JSONObject;

import java.io.File;
import java.security.PublicKey;

public class CommonSettings {


    public static final String WINDOWS_PASS_PATH = "C:/Users/SmirnygaTotoshka/Desktop/OLMPASS";
    public static String WINDOWS_HOME_PATH = "C:/Users/SmirnygaTotoshka/Desktop";
    private String workDir;
    private String dataSourceDir;
    private String dataDestDir;
    private String SARpath;
    private String scriptsPath;
    private String outputPath;
    private String configs;
    private String logPath;
    private long num_threads;
    private boolean overwrite;

    public boolean isOverwrite() {
        return overwrite;
    }

    public CommonSettings(JSONObject jo) {
        this.workDir = (String) jo.get("work_dir");
        WINDOWS_HOME_PATH = WINDOWS_HOME_PATH + File.separator + this.workDir.substring(this.workDir.indexOf(",share=")+7);
        this.dataSourceDir = (String) jo.get("data_source_dir");
        this.dataDestDir = (String) jo.get("data_dest_dir");
        this.SARpath = (String) jo.get("sar");
        this.scriptsPath = (String) jo.get("scripts");
        this.outputPath = (String) jo.get("output");
        this.logPath = (String) jo.get("log");
        this.configs = (String) jo.get("configs");
        this.num_threads = (long) jo.get("threads");
        this.overwrite = (boolean) jo.get("overwrite");
    }

    public String getWorkDir() {
        return workDir;
    }

    public String getDataSourceDir() {
        return dataSourceDir;
    }

    public String getDataDestDir() {
        return dataDestDir;
    }

    public String getSARpath() {
        return SARpath;
    }

    public String getScriptsPath() {
        return scriptsPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getLogPath() {
        return logPath;
    }

    public long getNum_threads() {
        return num_threads;
    }

    public String getConfigs() {
        return configs;
    }

    public String getAbsoluteLogPath(){
        return workDir + File.separator + logPath;
    }

    public String getAbsoluteConfigPath(){
        return workDir + File.separator + configs;
    }

    public String getWinConfigPath(){
        return WINDOWS_HOME_PATH +  File.separator + configs;
    }

    public String getConverterWinPath(){
        return WINDOWS_HOME_PATH +  File.separator + scriptsPath + "/SeqToSDF.py";
    }

    public String[] getLaunchStructure(){
        return new String[]{
                workDir + File.separator + dataDestDir,
                workDir + File.separator + logPath,
                workDir + File.separator + outputPath,
                workDir + File.separator + SARpath,
                workDir + File.separator + scriptsPath,
                workDir + File.separator + configs,
        };
    }

    @Override
    public String toString() {
        return "CommonSettings{" +
                "workDir='" + workDir + '\'' +
                ", dataSourceDir='" + dataSourceDir + '\'' +
                ", dataDestDir='" + dataDestDir + '\'' +
                ", SARpath='" + SARpath + '\'' +
                ", scriptsPath='" + scriptsPath + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", configs='" + configs + '\'' +
                ", logPath='" + logPath + '\'' +
                ", num_threads=" + num_threads +
                ", overwrite=" + overwrite +
                '}';
    }
}

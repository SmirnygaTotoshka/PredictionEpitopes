package ru.smirnygatotoshka.pass;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

public class Config {

    public static final String WINDOWS_PASS_PATH = "C:\\Users\\SmirnygaTotoshka\\Desktop\\OLMPASS";
    public static final String WINDOWS_HOME_PATH = "C:\\Users\\SmirnygaTotoshka\\Desktop";
    private String[] model_names;
    private String method;

    private String user, host, password;

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    private long min_desc_level, max_desc_level;
    private boolean fiveCV;
    private String activity;
    private String record_id;

    private String remote_exec_configs;

    public String getRecord_id() {
        return record_id;
    }

    public String getRemote_exec_configs() {
        return remote_exec_configs;
    }

    private String local_work_dir;
    private String csv_input;
    private String converter_output;
    private long converter_threads;
    private String converter_column;
    private String local_configs;
    private String remote_work_dir;
    private String remote_data;
    private String remote_SAR;
    private String remote_output;

    private long threads;

    private boolean overwrite;

    private String converter;
    private long timeout;

    public long getTimeout() {
        return timeout;
    }

    public String getConverter() {
        return converter;
    }

    public Config(JSONObject jo){
        JSONArray array = (JSONArray) jo.get("model_name");
        this.model_names = new String[array.size()];
        for (int i = 0; i < array.size();i++) {
            this.model_names[i] = (String) array.get(i);
        }

        this.method = (String) jo.get("method");
        this.min_desc_level = (long) jo.get("min_desc_level");
        this.max_desc_level = (long) jo.get("max_desc_level");
        this.fiveCV = (boolean) jo.get("fiveCV");
        this.activity = (String) jo.get("activity");
        this.local_work_dir = (String) jo.get("local_work_dir");
        this.csv_input = (String) jo.get("csv_input");
        this.converter_output = (String) jo.get("converter_output");
        this.converter_threads = (long) jo.get("converter_threads");
        this.converter_column = (String) jo.get("converter_column");
        this.local_configs = (String) jo.get("local_configs");
        this.remote_work_dir = (String) jo.get("remote_work_dir");
        this.remote_data = (String) jo.get("remote_data");
        this.remote_SAR = (String) jo.get("remote_SAR");
        this.remote_output = (String) jo.get("remote_output");
        this.converter = (String) jo.get("converter");
        this.threads = (long) jo.get("threads");
        this.timeout = (long) jo.get("timeout");
        this.overwrite = (boolean) jo.get("overwrite");
        this.record_id = (String) jo.get("record_id");
        this.remote_exec_configs = (String) jo.get("remote_exec_configs");
        this.user = (String) jo.get("user");
        this.host = (String) jo.get("host");
        this.password = (String) jo.get("password");
    }

    public String[] getModel_names() {
        return model_names;
    }

    public String getMethod() {
        return method;
    }

    public long getMin_desc_level() {
        return min_desc_level;
    }

    public long getMax_desc_level() {
        return max_desc_level;
    }

    public boolean isFiveCV() {
        return fiveCV;
    }

    public String getActivity() {
        return activity;
    }

    public String getLocal_work_dir() {
        return local_work_dir;
    }

    public String getCsv_input() {
        return csv_input;
    }

    public String getConverter_output() {
        return converter_output;
    }

    public long getConverter_threads() {
        return converter_threads;
    }

    public String getConverter_column() {
        return converter_column;
    }

    public String getLocal_configs() {
        return local_configs;
    }

    public String getRemote_work_dir() {
        return remote_work_dir;
    }


    public String getRemote_data() {
        return remote_data;
    }

    public String getRemote_SAR() {
        return remote_SAR;
    }

    public String getRemote_output() {
        return remote_output;
    }

    public int getThreads() {
        return Math.toIntExact(threads);
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public ObservableList<Model> prepare() throws IOException {
        String[] dirs = new String[]{
                local_work_dir,
                converter_output,
                local_configs,
                remote_work_dir,
                remote_data,
                remote_SAR,
                remote_output
        };
        for (String s: dirs) {
            File f = new File(s);
            if (!f.exists()){
                if (!f.mkdir()) throw new IOException("Couldn`t create dir " + s);
            }
        }
        ObservableList<Model> models = generateModelsDescription();
        for (Model m: models) {
            m.generateConverterConfigs(this);
            m.generatePASSConfigs(this);
        }
        return models;
    }

    private ObservableList<Model> generateModelsDescription() throws IOException {
        ObservableList<Model> models = FXCollections.observableArrayList();
        for (String model: model_names) {
            if (fiveCV){
                for (long level = min_desc_level; level <= max_desc_level ; level++) {
                    for (int i = 1; i < 6; i++) {
                        Model m = new Model(model, Model.VALIDATION_MODE,Integer.toString(i),level);
                        models.add(m);
                    }
                    Model m = new Model(model, Model.USUAL_MODE,"total",level);
                    models.add(m);
                }
            }
            else{
                for (long level = min_desc_level; level <= max_desc_level ; level++) {
                    Model m = new Model(model, Model.USUAL_MODE,"total",level);
                    models.add(m);
                }
            }
        }
        return models;
    }


    public String remotePathToWin(String path){
        String pattern = ",share=";
        return WINDOWS_HOME_PATH + File.separator + path.substring(path.indexOf(pattern)+pattern.length());
    }

    @Override
    public String toString() {
        return "Config{" +
                "model_names=" + Arrays.toString(model_names) +
                ", method='" + method + '\'' +
                ", min_desc_level=" + min_desc_level +
                ", max_desc_level=" + max_desc_level +
                ", fiveCV=" + fiveCV +
                ", activity='" + activity + '\'' +
                ", local_work_dir='" + local_work_dir + '\'' +
                ", csv_input='" + csv_input + '\'' +
                ", converter_output='" + converter_output + '\'' +
                ", converter_threads=" + converter_threads +
                ", converter_column='" + converter_column + '\'' +
                ", local_configs='" + local_configs + '\'' +
                ", remote_work_dir='" + remote_work_dir + '\'' +
                ", remote_data='" + remote_data + '\'' +
                ", remote_SAR='" + remote_SAR + '\'' +
                ", remote_output='" + remote_output + '\'' +
                ", threads=" + threads +
                ", overwrite=" + overwrite +
                ", converter='" + converter + '\'' +
                '}';
    }
}

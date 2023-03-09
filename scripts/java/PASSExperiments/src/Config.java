import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Config {

    public static final String WINDOWS_PASS_PATH = "C:\\Users\\SmirnygaTotoshka\\Desktop\\OLMPASS";
    public static final String WINDOWS_HOME_PATH = "C:\\Users\\SmirnygaTotoshka\\Desktop";
    private String[] model_names;
    private String method;
    private long min_desc_level, max_desc_level;
    private boolean fiveCV;
    private String activity;

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
        this.overwrite = (boolean) jo.get("overwrite");

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

    public long getThreads() {
        return threads;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void prepare() throws IOException {
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
        createConverterConfigs();
        createModelConfigs();
    }
    private void createConverterConfigs() throws IOException {
        for (String model: model_names) {
            if (fiveCV){
                String input_path = csv_input + File.separator + model + "_total_total.csv";
                String config_path = local_configs + File.separator + model + "_total_total.json";
                writeConverterConfig(input_path, config_path);
                for (int i = 1; i < 6; i++) {
                    String input_path_train = csv_input + File.separator + model + "_train_" + i + ".csv";
                    String config_path_train = local_configs + File.separator + model + "_train_" + i + ".json";
                    String input_path_test = csv_input + File.separator + model + "_test_" + i + ".csv";
                    String config_path_test = local_configs + File.separator + model + "_test_" + i + ".json";
                    writeConverterConfig(input_path_train, config_path_train);
                    writeConverterConfig(input_path_test, config_path_test);
                }

            }
            else{
                String input_path = csv_input + File.separator + model + ".csv";
                String config_path = local_configs + File.separator + model + ".json";
                writeConverterConfig(input_path, config_path);
            }
        }
    }

    private void writeConverterConfig(String input_path, String config_path) throws IOException {
        JSONObject jo = new JSONObject();
        jo.put("input", input_path);
        jo.put("output", converter_output);
        jo.put("column", converter_column);
        jo.put("threads", converter_threads);
        BufferedWriter writer = new BufferedWriter(new FileWriter(config_path));
        writer.write(jo.toJSONString().replaceAll("\\\\",""));
        System.out.println(jo.toJSONString().replaceAll("\\\\",""));
        writer.close();
    }
/**TODO train/test and for 2csv config*/
    private void createModelConfigs() throws IOException {
        for (String model: model_names) {
            if (fiveCV){
                for (long level = min_desc_level; level <= max_desc_level ; level++) {
                    for (int i = 1; i < 6; i++) {
                        writeExecutionScript(model, "triaan", String.valueOf(i),level);
                    }
                    writeExecutionScript(model, "train" , "total", level);
                }
            }
            else{
                for (long level = min_desc_level; level <= max_desc_level ; level++) {
                    writeExecutionScript(model,"total" ,"total", level);
                }
            }
        }
    }

    public void writeExecutionScript(String model,String mode , String fold,long level) throws IOException {
        String m_name = model + "_" + level + "_" + fold;
        String config_path = remote_work_dir + File.separator + m_name + ".txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(config_path));
        String win_sdf = (new File(remotePathToWin(remote_data)).getName() + File.separator + model + ".sdf").replaceAll("/","\\\\");
        String win_sar = (new File(remotePathToWin(remote_SAR)).getName() + File.separator + m_name).replaceAll("/","\\\\");
        writer.write("BaseCreate=" + level + ";" + model + "\n");
        writer.write("BaseAddNewData=" + win_sdf + ";" + activity + "\n");
        writer.write("BaseSave="+ win_sar + "\n");
        writer.write("BaseTraining"+ "\n");
        writer.write("BaseValidation"+ "\n");
        writer.write("BaseClose");
        writer.close();
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

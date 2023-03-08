import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

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
    private String remote_configs;
    private String remote_data;
    private String remote_SAR;
    private String remote_output;

    private long threads;

    private boolean overwrite;

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
        this.remote_configs = (String) jo.get("remote_configs");
        this.remote_data = (String) jo.get("remote_data");
        this.remote_SAR = (String) jo.get("remote_SAR");
        this.remote_output = (String) jo.get("remote_output");
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

    public String getRemote_configs() {
        return remote_configs;
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
                remote_configs,
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

            }
            else{
                String input_path = csv_input + File.separator + model + ".csv";
                JSONObject jo = new JSONObject();
                jo.put("input", input_path);
                jo.put("output", converter_output);
                jo.put("column", converter_column);
                jo.put("threads", converter_threads);
                String config_path = local_configs + File.separator + model + ".json";
                BufferedWriter writer = new BufferedWriter(new FileWriter(config_path));
                writer.write(jo.toJSONString().replaceAll("\\\\",""));
                System.out.println(jo.toJSONString().replaceAll("\\\\",""));
                writer.close();
            }
        }
    }
    private void createModelConfigs() {
    }


}

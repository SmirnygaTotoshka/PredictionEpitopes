import org.json.simple.JSONObject;

public class ModelConfig {
    private long min_base_level;
    private long max_base_level;
    private String model_name;
    private String method;
    private String base_name;
    private String activity;
    private String output;
    private boolean fiveCV;

    public ModelConfig(JSONObject jo) {
        this.min_base_level = (long) jo.get("min_base_level");
        this.max_base_level = (long) jo.get("max_base_level");
        this.model_name = (String) jo.get("model_name");
        this.method = (String) jo.get("method");
        this.base_name = (String) jo.get("base_name");
        this.activity = (String) jo.get("activity");
        this.output = (String) jo.get("output");
        this.fiveCV = (boolean) jo.get("fiveCV");
    }

    public long getMin_base_level() {
        return min_base_level;
    }

    public long getMax_base_level() {
        return max_base_level;
    }

    public String getModel_name() {
        return model_name;
    }

    public String getMethod() {
        return method;
    }

    public String getBase_name() {
        return base_name;
    }

    public String getActivity() {
        return activity;
    }

    public String getOutput() {
        return output;
    }

    public boolean isFiveCV() {
        return fiveCV;
    }

    @Override
    public String toString() {
        return "ModelConfig{" +
                "min_base_level=" + min_base_level +
                ", max_base_level=" + max_base_level +
                ", model_name='" + model_name + '\'' +
                ", method='" + method + '\'' +
                ", base_name='" + base_name + '\'' +
                ", activity='" + activity + '\'' +
                ", output='" + output + '\'' +
                ", fiveCV=" + fiveCV +
                '}';
    }
}

package databaseModels;

import java.sql.Connection;
import java.sql.SQLException;

public class ModelConfig implements DatabaseManagement{
    private int id;
    private String workdir;
    private String base_name;
    private String data;
    private String activity_column;
    private String exclude;
    private String output;
    private String act_list_name;

    public ModelConfig(int id, String workdir, String base_name, String data, String activity_column, String exclude, String output, String act_list_name) {
        this.id = id;
        this.workdir = workdir;
        this.base_name = base_name;
        this.data = data;
        this.activity_column = activity_column;
        this.exclude = exclude;
        this.output = output;
        this.act_list_name = act_list_name;
    }

    public int getId() {
        return id;
    }

    public String getWorkdir() {
        return workdir;
    }

    public void setWorkdir(String workdir) {
        this.workdir = workdir;
    }

    public String getBase_name() {
        return base_name;
    }

    public void setBase_name(String base_name) {
        this.base_name = base_name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getActivity_column() {
        return activity_column;
    }

    public void setActivity_column(String activity_column) {
        this.activity_column = activity_column;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getAct_list_name() {
        return act_list_name;
    }

    public void setAct_list_name(String act_list_name) {
        this.act_list_name = act_list_name;
    }

    @Override
    public void insert(Connection con) throws SQLException {

    }

    @Override
    public int getID(Connection con) {
        return 0;
    }
}

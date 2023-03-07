package databaseModels;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

public class Model implements DatabaseManagement{
    private int id;
    private Date experiment_date;
    private int desc_level;
    private String method;
    private String model_name;
    private int converter_config;
    private int model_config;

    public Model(int id, Date experiment_date, int desc_level, String method, String model_name, int converter_config, int model_config) {
        this.id = id;
        this.experiment_date = experiment_date;
        this.desc_level = desc_level;
        this.method = method;
        this.model_name = model_name;
        this.converter_config = converter_config;
        this.model_config = model_config;
    }

    public int getId() {
        return id;
    }


    public Date getExperiment_date() {
        return experiment_date;
    }

    public void setExperiment_date(Date experiment_date) {
        this.experiment_date = experiment_date;
    }

    public int getDesc_level() {
        return desc_level;
    }

    public void setDesc_level(int desc_level) {
        this.desc_level = desc_level;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public int getConverter_config() {
        return converter_config;
    }

    public int getModel_config() {
        return model_config;
    }


    @Override
    public void insert(Connection con) throws SQLException {

    }

    @Override
    public int getID(Connection con) {
        return 0;
    }
}

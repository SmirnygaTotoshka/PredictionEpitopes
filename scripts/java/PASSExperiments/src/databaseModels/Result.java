package databaseModels;

import java.sql.Connection;
import java.sql.SQLException;

public class Result implements DatabaseManagement{
    private int id;
    private int model_id;
    private boolean success;
    private int train_size;
    private String activity;
    private float iap;
    private float fiveCV;
    private float twentyCV;
    private float looCV;

    public Result(int id, int model_id, boolean success, int train_size, String activity, float iap, float fiveCV, float twentyCV, float looCV) {
        this.id = id;
        this.model_id = model_id;
        this.success = success;
        this.train_size = train_size;
        this.activity = activity;
        this.iap = iap;
        this.fiveCV = fiveCV;
        this.twentyCV = twentyCV;
        this.looCV = looCV;
    }

    public int getId() {
        return id;
    }


    public int getModel_id() {
        return model_id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTrain_size() {
        return train_size;
    }

    public void setTrain_size(int train_size) {
        this.train_size = train_size;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public float getIap() {
        return iap;
    }

    public void setIap(float iap) {
        this.iap = iap;
    }

    public float getFiveCV() {
        return fiveCV;
    }

    public void setFiveCV(float fiveCV) {
        this.fiveCV = fiveCV;
    }

    public float getTwentyCV() {
        return twentyCV;
    }

    public void setTwentyCV(float twentyCV) {
        this.twentyCV = twentyCV;
    }

    public float getLooCV() {
        return looCV;
    }

    public void setLooCV(float looCV) {
        this.looCV = looCV;
    }

    @Override
    public void insert(Connection con) throws SQLException {

    }

    @Override
    public int getID(Connection con) {
        return 0;
    }
}

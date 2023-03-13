package ru.smirnygatotoshka.pass;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Model {
    private String model_name, mode, fold;
    private long level;

    public static final String USUAL_MODE = "total";
    public static final String VALIDATION_MODE = "train";

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getId(){
        return this.id.get();
    }

    private SimpleStringProperty id;

    enum NEEDED_FILE{
        CONVERTER_CONFIG,
        EXECUTION_CONFIG,
        VALIDATION_CONFIG,
        SAMPLE,
        MODEL,
        CSV_OUTPUT,
        CSV_INPUT
    }

    enum STATUS{
        WAITING_CONVERT,
        CONVERT,
        COPY_SAMPLE,
        WAITING_EXECUTION,
        EXECUTION,
        WAITING_VALIDATION,
        VALIDATION,
        SUCCESS,
        FAILED;

    }

    private STATUS status;

    private SimpleStringProperty statusNameProperty;

    public String getStatusNameProperty() {
        return statusNameProperty.get();
    }

    public SimpleStringProperty statusNamePropertyProperty() {
        return statusNameProperty;
    }

    public void setStatusNameProperty(String statusNameProperty) {
        this.statusNameProperty.set(statusNameProperty);
    }

    public String getExceptionMessage() {
        if (exception == null){
            return "Нет";
        }
        else{
            return exception.getMessage();
        }
    }

    public SimpleStringProperty exceptionMessageProperty() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        if (exception == null){
            this.exceptionMessage.set("Нет");
        }
        else{
            this.exceptionMessage.set(exceptionMessage);
        }

    }

    private SimpleStringProperty exceptionMessage;


    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
        this.statusNameProperty.set(status.name());
    }

    private boolean success;

    private Exception exception;

    public Model(String model_name, String mode, String fold, long level) throws IOException {
        this.model_name = model_name;
        this.mode = mode;
        this.fold = fold;
        this.level = level;
        this.success = true;
        this.exception = null;
        this.status = STATUS.WAITING_CONVERT;
        this.statusNameProperty = new SimpleStringProperty(STATUS.WAITING_CONVERT.name());
        this.exceptionMessage = new SimpleStringProperty("Нет");
        this.id = new SimpleStringProperty(this.generateID());
        if (!mode.contentEquals(USUAL_MODE) && !mode.contentEquals(VALIDATION_MODE)) throw new IOException("Invalid model mode");
    }

    public boolean isSuccess() throws Exception {
        if (success)
            return success;
        else {
            if (exception != null)
                throw exception;
            else
                return false;
        }
    }

    public String generateID(){
        return level + "_" + model_name + "_" + mode + "_" + fold;
    }

    public String getName(NEEDED_FILE file){
        String name, extension;
        switch (file){
            case CONVERTER_CONFIG:
                name = model_name + "_" + mode + "_" + fold;
                extension = ".json";
                break;
            case CSV_INPUT:
                name = model_name + "_" + mode + "_" + fold;
                extension = ".csv";
                break;
            case EXECUTION_CONFIG:
                name = generateID();
                extension = ".txt";
                break;
            case VALIDATION_CONFIG:
                name = generateID();
                extension = "_val.txt";
                break;
            case CSV_OUTPUT:
                name = generateID();
                extension = ".csv";
                break;
            case MODEL:
                name = generateID();
                extension = ".MSAR";
                break;
            case SAMPLE:
                name = model_name + "_" + mode + "_" + fold;
                extension = ".sdf";
                break;
            default:
                name = generateID();
                extension = "";
                break;
        }
        return name + extension;
    }

    public void generateConverterConfigs(Config config){
        try {
            String input_path = config.getCsv_input() + File.separator + getName(NEEDED_FILE.CSV_INPUT);
            String config_path = config.getLocal_configs() + File.separator + getName(NEEDED_FILE.CONVERTER_CONFIG);
            if (mode.contentEquals(USUAL_MODE)) {
                writeConverterConfig(input_path, config_path, config);
                System.out.println(config_path + " is created");
            }
            else {
                String input_path_test = config.getCsv_input() + File.separator + model_name + "_test_" + fold + ".csv";
                String config_path_test = config.getLocal_configs() + File.separator + model_name + "_test_" + fold + ".json";
                writeConverterConfig(input_path, config_path, config);
                writeConverterConfig(input_path_test, config_path_test, config);
                System.out.println(config_path + " is created");
                System.out.println(config_path_test + " is created");
            }
        }
        catch (IOException e) {
            fail(e);
        }
    }

    public void fail(Exception e){
        success = false;
        exception = e;
        exceptionMessage.set(e.getMessage());
        setStatus(STATUS.FAILED);
    }


    public void success(){
        success = true;
        exception = null;
        exceptionMessage.set("Нет");
        setStatus(STATUS.SUCCESS);
    }
    public void writeConverterConfig(String input_path, String config_path,Config config) throws IOException {
        JSONObject jo = new JSONObject();
        jo.put("input", input_path);
        jo.put("output", config.getConverter_output());
        jo.put("column", config.getConverter_column());
        jo.put("threads", config.getConverter_threads());
        //jo.put("delete_tmp", false);
        BufferedWriter writer = new BufferedWriter(new FileWriter(config_path));
        writer.write(jo.toJSONString().replaceAll("\\\\",""));
        System.out.println(jo.toJSONString().replaceAll("\\\\",""));
        writer.close();
    }

    public void generatePASSConfigs(Config config){
        try{
            writeExecutionScript(config);
            if (config.isFiveCV())
                writeValidationScript(config);
        }
        catch (IOException e){
            fail(e);
        }
    }

    private void writeExecutionScript(Config config) throws IOException {
        String config_path = config.getRemote_work_dir() + File.separator + getName(NEEDED_FILE.EXECUTION_CONFIG);
        BufferedWriter writer = new BufferedWriter(new FileWriter(config_path));
        String win_sdf = (new File(config.remotePathToWin(config.getRemote_data())).getName() + File.separator + getName(NEEDED_FILE.SAMPLE)).replaceAll("/","\\\\");
        String win_sar = (new File(config.remotePathToWin(config.getRemote_SAR())).getName() + File.separator + generateID()).replaceAll("/","\\\\");
        writer.write("BaseCreate=" + level + ";" + generateID() + "\n");
        writer.write("BaseAddNewData=" + win_sdf + ";" + config.getActivity() + "\n");
        writer.write("BaseSave="+ win_sar + "\n");
        writer.write("BaseTraining"+ "\n");
        writer.write("BaseValidation"+ "\n");
        writer.write("BaseClose");
        writer.close();
        System.out.println(config_path + " is created.");
    }

    public void writeValidationScript(Config config) throws IOException{
        String sdf_name = model_name + "_test_" + fold +  ".sdf";
        String config_path = config.getRemote_work_dir() + File.separator + getName(NEEDED_FILE.VALIDATION_CONFIG);
        BufferedWriter writer = new BufferedWriter(new FileWriter(config_path));
        String win_sdf = (new File(config.remotePathToWin(config.getRemote_data())).getName() + File.separator + sdf_name).replaceAll("/","\\\\");
        String win_sar = (new File(config.remotePathToWin(config.getRemote_SAR())).getName() + File.separator + getName(NEEDED_FILE.MODEL)).replaceAll("/","\\\\");
        String win_output = (new File(config.remotePathToWin(config.getRemote_output())).getName() + File.separator + getName(NEEDED_FILE.CSV_OUTPUT)).replaceAll("/","\\\\");
        writer.write("InputName=" + win_sdf + "\n");
        writer.write("IdKeyField=" + config.getRecord_id() + "\n");
        writer.write("BaseName="+ win_sar + "\n");
        writer.write("OutputName=output/"+ win_output+ "\n");
        writer.close();
        System.out.println(config_path + " is created.");
    }
}

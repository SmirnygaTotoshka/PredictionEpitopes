package ru.smirnygatotoshka.pass;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Properties;

public class Model {
    private String model_name, mode, fold;
    private long level;
    private Runtime runtime;
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

    private Config config;

    public void call() {
        Distributor.getInstance().send(STATUS.CONVERT, this);
        ArrayList<File> samples = convert();
        if (status.equals(STATUS.FAILED))
            return;
        else {
            Distributor.getInstance().send(STATUS.COPY_SAMPLE, this);
            try {
                copySDFToRemote(samples);
                Distributor.getInstance().send(STATUS.WAITING_EXECUTION, this);
            }
            catch (IOException e){
                fail(e);
            }
        }
        if (status.equals(STATUS.FAILED))
            return;
        else {
            Distributor.getInstance().send(STATUS.EXECUTION, this);
            buildModel();
            if (config.isFiveCV())
                Distributor.getInstance().send(STATUS.WAITING_VALIDATION, this);
        }
        if (config.isFiveCV()) {
            if (status.equals(STATUS.FAILED))
                return;
            else {
                Distributor.getInstance().send(STATUS.VALIDATION, this);
                validate();
                if (config.isFiveCV())
                    Distributor.getInstance().send(STATUS.WAITING_VALIDATION, this);
            }
        }
    }

    private ArrayList<File> convert(){
        ArrayList<File> files = new ArrayList<>(2);
        if (mode.contentEquals(USUAL_MODE)){
            File out = new File(config.getConverter_output() + File.separator + getName(Model.NEEDED_FILE.SAMPLE));
            String converter_config = config.getLocal_configs() + File.separator + getName(Model.NEEDED_FILE.CONVERTER_CONFIG);
            if (!out.exists() && !Distributor.getInstance().isExecute(converter_config)) {
                try {
                    Distributor.getInstance().register(converter_config);
                    boolean success = execConverter(converter_config);
                    if (!success) throw new InterruptedException("Convert code != 0");
                    files.add(out);
                }
                catch (IOException | InterruptedException  e) {
                    fail(e);
                }
                finally {
                    Distributor.getInstance().unregister(converter_config);
                }
            }
            else {
                System.out.println(out.getName() +  " is already exists!");
            }
        }
        else if (mode.contentEquals(VALIDATION_MODE)){
            File train_sample = new File(config.getConverter_output() + File.separator + getName(Model.NEEDED_FILE.SAMPLE));
            File test_sample = new File(config.getConverter_output() + File.separator + model_name + "_test_" + fold + ".sdf");
            String converter_train_config = config.getLocal_configs() + File.separator + getName(Model.NEEDED_FILE.CONVERTER_CONFIG);
            String converter_test_config = config.getLocal_configs() + File.separator + model_name + "_test_" + fold + ".json";
            if (!train_sample.exists() && !Distributor.getInstance().isExecute(converter_train_config)){
                try {
                    Distributor.getInstance().register(converter_train_config);
                    boolean success = execConverter(converter_train_config);
                    if (!success) throw new InterruptedException("Convert code != 0");
                    files.add(train_sample);
                }
                catch (IOException | InterruptedException  e) {
                    fail(e);
                }
                finally {
                    Distributor.getInstance().unregister(converter_train_config);
                }
            }
            else {
                System.out.println(train_sample.getName() +  " is already exists!");
            }
            if (!test_sample.exists() && !Distributor.getInstance().isExecute(converter_test_config)){
                try {
                    Distributor.getInstance().register(converter_test_config);
                    boolean success = execConverter(converter_test_config);
                    if (!success) throw new InterruptedException("Convert code != 0");
                    files.add(test_sample);
                }
                catch (IOException | InterruptedException  e) {
                    fail(e);
                }
                finally {
                    Distributor.getInstance().unregister(converter_test_config);
                }
            }
            else {
                System.out.println(test_sample.getName() +  " is already exists!");
            }
        }
        else{
            fail(new Exception("Invalid mode " + generateID() + " " + mode));
        }
        return files;
    }


    private boolean execConverter(String converter_config) throws IOException, InterruptedException {
        System.out.println("Convert from " + converter_config);
        Process converter = runtime.exec(new String[]{
                "/home/stotoshka/Soft/anaconda3/envs/research/bin/python",
                config.getConverter(),
                converter_config
        });
        BufferedReader r = new BufferedReader(new InputStreamReader(converter.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) {
            System.out.println(line);
        }
        return converter.waitFor() == 0;
    }

    private void copySDFToRemote(ArrayList<File> source) throws IOException {
        for (File src: source) {
            System.out.println("Copy " + src.getName());
            Path dst = Paths.get(config.getRemote_data() + File.separator + src.getName());
            File dstFile = dst.toFile();
            if (!dstFile.exists()) {
                Files.copy(src.toPath(), dst);
            }
            System.out.println(src.getAbsolutePath() + " is copied");
        }
    }

    private void buildModel(){
        String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\\\");
        String win_path = win_dir + "\\" + getName(Model.NEEDED_FILE.EXECUTION_CONFIG);
        String command = Config.WINDOWS_PASS_PATH + "\\" + "OLMPASSdoSAR.exe" + " " + win_path;
        boolean success = execWindows(command);
        boolean isParse = false;
        try {
            isParse = parseResult();
            if (isParse){
                if (mode.contentEquals(USUAL_MODE))
                    success();
            }
            else{
                String message = parseLog();
                fail(new Exception(success?message:"Execution code != 0;" + message));
            }
        }
        catch (IOException e) {
            fail(e);
        }
    }

    private boolean validate(){
        String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\\\");
        String win_path = win_dir + "\\" + getName(NEEDED_FILE.VALIDATION_CONFIG);
        String command = Config.WINDOWS_PASS_PATH + "\\" + "OLMPASS2CSV.exe" + " " + win_path;
        return execWindows(command);
    }

    private String parseLog() throws IOException{
        StringBuilder msg = new StringBuilder();
        String result_filepath = config.getRemote_work_dir() + File.separator + getName(Model.NEEDED_FILE.LOG);
        BufferedReader reader = new BufferedReader(new FileReader(result_filepath));
        String line;
        while ((line = reader.readLine()) != null) {
            msg.append(line);
        }
        return msg.toString();
    }

    public boolean parseResult()throws IOException {
        boolean flag = false;
        if (status.equals(Model.STATUS.EXECUTION)) {
            String result_filepath = config.getRemote_SAR() + File.separator + getName(Model.NEEDED_FILE.MODEL_OUTPUT);
            System.out.println(generateID() + "\t" + result_filepath);
            BufferedReader reader = new BufferedReader(new FileReader(result_filepath));
            String line;
            String header = "No\t Number\t IAP\t Activity Type";
            while ((line = reader.readLine()) != null) {
                if (line.contains(header)) {
                    flag = true;
                    continue;
                }
                if (flag) {
                    String[] elements = line.split("\t ");
                    for (String s:elements) {
                        System.out.print(s + " ");
                    }
                    System.out.println("");
                }
            }
        }
        else {
            flag = true;
        }
        return flag;
    }

    private boolean execWindows(String command){
        Channel channel = null;
        Session session = null;
        try{
            JSch jsch = new JSch();
            String host = config.getHost();
            String user = config.getUser();
            String password = config.getPassword();

            session = jsch.getSession(user, host, 22);
            Properties con = new Properties();
            con.put("StrictHostKeyChecking", "no");
            session.setConfig(con);
            session.setPassword(password);
            session.connect();
            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.out, true);

            InputStream input = channel.getInputStream();
            channel.connect();
            System.out.println("Channel Connected to machine 192.168.0.10 server with command:\n" + command + "\n");
            InputStreamReader inputReader = null;
            BufferedReader bufferedReader = null;
            try{
                inputReader = new InputStreamReader(input);
                bufferedReader = new BufferedReader(inputReader);
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    System.out.println(line);
                }

            }catch(IOException ex){
                System.out.println(ex);
                ex.printStackTrace();
            }
            finally {
                bufferedReader.close();
                inputReader.close();
            }
            channel.disconnect();
            session.disconnect();
            return channel.getExitStatus() == 0;
        }
        catch(Exception ex){
            System.out.println(ex);
            ex.printStackTrace();
            return false;
        }
        finally {
            if (channel.isConnected()) {
                channel.disconnect();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
            System.out.println("Disconnect");
        }
    }


    enum NEEDED_FILE{
        CONVERTER_CONFIG,
        EXECUTION_CONFIG,
        VALIDATION_CONFIG,
        SAMPLE,
        MODEL,
        LOG,
        MODEL_OUTPUT,
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

    public Model(String model_name, String mode, String fold, long level, Config config) throws IOException {
        this.model_name = model_name;
        this.mode = mode;
        this.fold = fold;
        this.level = level;
        this.config = config;
        this.success = true;
        this.exception = null;
        this.runtime = Runtime.getRuntime();
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
            case MODEL_OUTPUT:
                name = generateID();
                extension = ".LST";
                break;
            case LOG:
                name = generateID();
                extension = ".HST";
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

    public String getModel_name() {
        return model_name;
    }

    public String getMode() {
        return mode;
    }

    public long getLevel() {
        return level;
    }

    public String getFold() {
        return fold;
    }

    public void fail(Exception e){
        success = false;
        exception = e;
        exceptionMessage.set(e.getMessage());
        Distributor.getInstance().send(STATUS.FAILED,this);
    }


    public void success(){
        success = true;
        exception = null;
        exceptionMessage.set("Нет");
        Distributor.getInstance().send(STATUS.SUCCESS,this);
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

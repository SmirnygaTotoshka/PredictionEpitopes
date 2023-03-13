import com.jcraft.jsch.JSchException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static final String url = "jdbc:mysql://192.168.0.11:3306/experiments";
    private static final String user = "root";
    private static final String password = "meow_root";

    // JDBC variables for opening and managing connection
    private volatile static Connection con;

    public synchronized static Connection getConnection() {
        return con;
    }

    public static void printModelStatuses(HashMap<String, Model> all_models){
        for (Model m: all_models.values()) {
            try {
                System.out.println(m.getId() + "\t" + m.getStatus() + "\t" + m.isSuccess());
            } catch (Exception e) {
                System.out.println(m.getId() + "\t" + m.getStatus() + "\t" + e);
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        FileWriter writer = null;
        try {
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Success connection");
            File file = new File(args[0]);
            if (file.exists()){
                JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(file));
                Config settings = new Config(jo);
                System.out.println(settings);
                System.out.println("---------------------------------------------------");
                System.out.println("Preparing");
                System.out.println(new Date());
                HashMap<String, Model> all_models = settings.prepare();
                System.out.println("---------------------------------------------------");

                printModelStatuses(all_models);
              /*  System.out.println("Converting");
                Distributor d = new Distributor();
                ArrayList<File[]> converter_files = d.distribute(settings.getLocal_configs(),settings.getThreads(), ".json");
                ExecutorService service = Executors.newFixedThreadPool(converter_files.size());
                ArrayList<ConveterTask> converters = new ArrayList<>();
                for (int i = 0; i < converter_files.size(); i++) {
                    converters.add(new ConveterTask(i, settings, converter_files.get(i)));
                }
                List<Future<Boolean>> converterTasks = service.invokeAll(converters);
                for (Future<Boolean> res: converterTasks) {
                    if (!res.get()) throw new InterruptedException("Unsuccessful complete - Converting");
                }

                System.out.println("MODEL BUILDING!");

                Distributor d1 = new Distributor();
                ArrayList<File[]> execution_files = d1.distribute(settings.getRemote_work_dir(),settings.getThreads(), ".txt");
                service = Executors.newFixedThreadPool(execution_files.size());
                ArrayList<ExecutionTask> executors = new ArrayList<>();
                for (int i = 0; i < execution_files.size(); i++) {
                    executors.add(new ExecutionTask(i, settings, execution_files.get(i)));
                }
                List<Future<Boolean>> executionTask = service.invokeAll(executors);
                for (Future<Boolean> res: executionTask) {
                    if (!res.get()) throw new InterruptedException("Unsuccessful complete - Execution");
                }

                if (settings.isFiveCV()) {

                    System.out.println("MODEL 5CV!");
                    settings.prepareValidation();

                    Distributor d2 = new Distributor();
                    ArrayList<File[]> validationFiles = d2.distribute(settings.getRemote_work_dir(), settings.getThreads(), ".txt");
                    service = Executors.newFixedThreadPool(validationFiles.size());
                    ArrayList<ExecutionTask> validators = new ArrayList<>();
                    ExecutionTask.PROGRAM_NAME = "OLMPASS2CSV.exe";

                    for (int i = 0; i < validationFiles.size(); i++) {
                        validators.add(new ExecutionTask(i, settings, validationFiles.get(i)));
                    }
                    List<Future<Boolean>> validationTask = service.invokeAll(executors);
                    for (Future<Boolean> res: validationTask) {
                        if (!res.get()) throw new InterruptedException("Unsuccessful complete - validation");
                    }
                }

              /*  Task[] tasks = new Task[Math.toIntExact(settings.getThreads())];
                Thread[] threads = new Thread[tasks.length];
                ArrayList<File[]> files = distributor.distribute();
                for (int i = 0; i < tasks.length; i++) {
                    tasks[i].setFiles(files.get(i));
                    threads[i] = new Thread(tasks[i]);
                    threads[i].join();
                }*/

                /*JSONArray converter_array = (JSONArray) jo.get("converter_configs");
                JSONArray model_array = (JSONArray) jo.get("model_configs");
                if (converter_array.size() != model_array.size()) throw new IOException("Number converter tasks != Number model tasks");
                else {
                    System.out.println("Converters tasks");
                    ConverterConfig[] converterConfigs = new ConverterConfig[converter_array.size()];
                    for (int i = 0; i < converterConfigs.length; i++) {
                        converterConfigs[i] = new ConverterConfig((JSONObject) converter_array.get(i));
                        System.out.println(converterConfigs[i]);
                    }
                    System.out.println("---------------------------------------------------");
                    System.out.println("Modelling tasks");
                    ModelConfig[] modelConfigs = new ModelConfig[model_array.size()];
                    for (int i = 0; i < modelConfigs.length; i++) {
                        modelConfigs[i] = new ModelConfig((JSONObject) model_array.get(i));
                        System.out.println(modelConfigs[i]);
                    }
                    System.out.println("---------------------------------------------------");
                    Task task = new Task(0,settings, converterConfigs, modelConfigs);
                    task.execute();
                }*/
            }
            else throw new IOException("Config file doesn`t exist");
        }
        catch (SQLException e) {
            System.out.println("Failed connection " + e);
        }
        catch (IOException e) {
            System.out.println("Failed config " + e);
        }
        catch (ParseException e) {
            System.out.println("Failed parse " + e);
        } /*catch (InterruptedException e) {
            System.out.println("Failed process " + e);
        } catch (ExecutionException e) {
            System.out.println("Failed execution " + e);
        } */finally {
            con.close();
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("Failed close " + e);
            }
            System.exit(0);
        }
    }

}
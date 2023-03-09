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
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static final String url = "jdbc:mysql://192.168.0.11:3306/experiments";
    private static final String user = "root";
    private static final String password = "meow_root";

    // JDBC variables for opening and managing connection
    private volatile static Connection con;

    public synchronized static Connection getConnection() {
        return con;
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
                settings.prepare();
                System.out.println("---------------------------------------------------");
                Distributor distributor = new Distributor();
                ArrayList<File[]> converter_files = distributor.distribute(settings.getLocal_configs(),settings.getThreads(), ".json");
                ConveterTask[] conveterTasks = new ConveterTask[converter_files.size()];
                Thread[] converterThreads = new Thread[converter_files.size()];
                CountDownLatch latch = new CountDownLatch(converter_files.size());
                for (int i = 0; i < conveterTasks.length; i++) {
                    conveterTasks[i] = new ConveterTask(i, settings, converter_files.get(i), latch);
                    converterThreads[i] = new Thread(conveterTasks[i]);
                    converterThreads[i].start();
                }
                latch.await();
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
        } /*catch (JSchException e) {
            throw new RuntimeException(e);
        }*/ catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
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
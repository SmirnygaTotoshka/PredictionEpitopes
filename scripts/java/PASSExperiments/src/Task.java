import com.jcraft.jsch.*;
import databaseModels.Result;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public abstract class Task implements Runnable{

    protected File[] files;

    protected int id;
    protected Config config;
    //private Runtime runtime = Runtime.getRuntime();

    public Task(int id,Config config, File[] files){
        this.id = id;
        this.config = config;
        this.files = files;
    }
//  "work_dir": "/run/user/1000/gvfs/smb-share:server=192.168.0.10,share=diplom/combined"

 /*   public void convert(File converter_config) throws IOException, InterruptedException {
            File out = new File(config.getConverter_output() + File.separator + converter_config.getName().substring(0,converter_config.getName().indexOf('.')) + ".sdf");
            if (!out.exists()) {
                System.out.println(converter_config.getAbsolutePath());
                Process converter = runtime.exec(new String[]{
                        "/home/stotoshka/Soft/anaconda3/envs/research/bin/python",
                        config.getConverter(),
                        converter_config.getAbsolutePath()
                });
                BufferedReader r = new BufferedReader(new InputStreamReader(converter.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println(converter.waitFor());
            }
            else {
                System.out.println("Already converted.");
            }
    }

    public void copySDFToRemote() throws IOException {
        for (String model: config.getModel_names()) {
            File[] sdf = new File(config.getConverter_output()).listFiles((file, s) -> s.toLowerCase().endsWith(".sdf") && s.contains(model));
            for (File f: sdf) {
                Path dst = Paths.get(config.getRemote_data() + File.separator + f.getName());
                File dstFile = dst.toFile();
                if (!dstFile.exists()) {
                    Files.copy(f.toPath(), dst);
                }
                System.out.println(f.getAbsolutePath() + " is copied");
            }
        }
    }*/

    public void execute() throws IOException {
        for (String model: config.getModel_names()) {
            File[] pass_configs = new File(config.getRemote_work_dir()).listFiles((file, s) -> s.toLowerCase().endsWith(".txt") && s.contains(model));
            for (File cfg : pass_configs) {
                String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\");
                String win_path = win_dir + "\\" + cfg.getName();
                String command = Config.WINDOWS_PASS_PATH + "\\" + "OLMPASSdoSAR.exe " + win_path;
                boolean success = exec(command);
            }
        }

        /*for (int i = 0, j = 0; i < converter.length && j < model.length; i++,j++) {
            //registerModel(model[i]);
           //* generateConverterConfig(converter[i], i);
            /*for (long level = model[j].getMin_base_level(); level <= model[j].getMax_base_level();level++){
                generateModelConfig(model[j],j, level);
            }
            boolean f = exec("cd " + CommonSettings.WINDOWS_HOME_PATH);
            String converterConfigPath = settings.getWinConfigPath() + File.separator + id + "_" + i +"_"+ "converter_config.json";*/
            //boolean f1 = convert(converterConfigPath);
            //System.out.println(f1);

            /*buildModel(model[i]);
            ArrayList<Result> results = parse(model[i]);
            registerResults(results);*/
      //  }*/
        //session.disconnect();
    }


    private void registerResults(ArrayList<Result> results) {

    }

    private void buildModel() {

    }

    private ArrayList<Result> parse() {
        return null;
    }

    private boolean exec(String command){
        Channel channel = null;
        Session session = null;
        try{
            JSch jsch = new JSch();
            String host = "192.168.0.10";
            String user = "SmirnygaTotoshka";
            String password = "linageenddo1";

            session = jsch.getSession(user, host, 22);
            Properties con = new Properties();
            con.put("StrictHostKeyChecking", "no");
            session.setConfig(con);
            session.setPassword(password);
            session.connect();
            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.out);

            InputStream input = channel.getInputStream();
            channel.connect();

            System.out.println("Channel Connected to machine 192.168.0.10 server with command:\n" + command + "\n");

            try{
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    System.out.println(line);
                }
                bufferedReader.close();
                inputReader.close();
            }catch(IOException ex){
                System.out.println(ex);
                ex.printStackTrace();
            }
            return true;
        }
        catch(Exception ex){
            System.out.println(ex);
            ex.printStackTrace();
            return false;
        }
        finally {
            channel.disconnect();
            session.disconnect();
        }
    }


    private void registerModel() {
        /*synchronized (Main.getConnection()){

        }*/
    }

   /* @Override
    public void run() {
        int model_name = 0;
        int type = 1;
        int fold = 2;
        int level = 3;
        int extension = 4;
        for (File model: files) {
            String[] name_parts = model.getName().split("_");
            File convert_file = new File(config.getLocal_configs() + File.separator + name_parts[model_name] + name_parts[type] + name_parts[fold] + ".json");
            try {
                convert(convert_file);
                copySDFToRemote();
                execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }*/
}

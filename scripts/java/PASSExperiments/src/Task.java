import com.jcraft.jsch.*;
import databaseModels.Result;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public class Task {

    private int id;
    private Config config;
    private Runtime runtime = Runtime.getRuntime();

    private Session session;

    public Task(int id,Config config) throws JSchException {
        this.id = id;
        this.config = config;
        JSch jsch = new JSch();
        String host = "192.168.0.10";
        String user = "SmirnygaTotoshka";
        String password = "linageenddo1";

        this.session = jsch.getSession(user, host, 22);
        Properties con = new Properties();
        con.put("StrictHostKeyChecking", "no");
        this.session.setConfig(con);
        this.session.setPassword(password);
        this.session.connect();
    }
//  "work_dir": "/run/user/1000/gvfs/smb-share:server=192.168.0.10,share=diplom/combined"

    public void convert(File[] converter_configs_paths) throws IOException, InterruptedException {
        for (File c : converter_configs_paths) {
            File out = new File(config.getConverter_output() + File.separator + c.getName().substring(0,c.getName().indexOf('.')) + ".sdf");
            if (!out.exists()) {
                System.out.println(c.getAbsolutePath());
                Process converter = runtime.exec(new String[]{
                        "/home/stotoshka/Soft/anaconda3/envs/research/bin/python",
                        config.getConverter(),
                        c.getAbsolutePath()
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
    }

    public void execute() throws IOException {
        for (String model: config.getModel_names()) {
            File[] pass_configs = new File(config.getRemote_work_dir()).listFiles((file, s) -> s.toLowerCase().endsWith(".txt") && s.contains(model));
            for (File cfg : pass_configs) {
                String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\");
                String win_path = win_dir + "\\" + cfg.getName();
                String command = Config.WINDOWS_PASS_PATH + "\\" + "OLMPASSdoSAR.exe " + win_path;
                exec(command);
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
        try{
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
                ex.printStackTrace();
            }
            return true;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        finally {
            channel.disconnect();
        }
    }


    private void registerModel() {
        /*synchronized (Main.getConnection()){

        }*/
    }
}

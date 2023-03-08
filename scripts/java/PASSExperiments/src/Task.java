import com.jcraft.jsch.*;
import databaseModels.Result;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class Task {

    private int id;
    private CommonSettings settings;
    private ConverterConfig[] converter;
    private ModelConfig[] model;

    private Session session;

    public Task(int id,CommonSettings settings, ConverterConfig[] converter, ModelConfig[] model) throws JSchException {
        this.id = id;
        this.settings = settings;
        this.converter = converter;
        this.model = model;
        JSch jsch = new JSch();
        String host = "192.168.0.10";
        String user = "SmirnygaTotoshka";
        String password = "linageenddo1";

        this.session = jsch.getSession(user, host, 22);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        this.session.setConfig(config);
        this.session.setPassword(password);
        this.session.connect();
    }
//  "work_dir": "/run/user/1000/gvfs/smb-share:server=192.168.0.10,share=diplom/combined"

    public void execute() throws IOException {
        for (int i = 0, j = 0; i < converter.length && j < model.length; i++,j++) {
            //registerModel(model[i]);
            generateConverterConfig(converter[i], i);
            for (long level = model[j].getMin_base_level(); level <= model[j].getMax_base_level();level++){
                generateModelConfig(model[j],j, level);
            }
            boolean f = exec("cd " + CommonSettings.WINDOWS_HOME_PATH);
            String converterConfigPath = settings.getWinConfigPath() + File.separator + id + "_" + i +"_"+ "converter_config.json";
            //boolean f1 = convert(converterConfigPath);
            //System.out.println(f1);

            /*buildModel(model[i]);
            ArrayList<Result> results = parse(model[i]);
            registerResults(results);*/
        }
        session.disconnect();
    }

    private void generateConverterConfig(ConverterConfig converterConfig, int pos) throws IOException {
        String path = settings.getAbsoluteConfigPath() + File.separator + id + "_" + pos + "_converter_config.json";
        converterConfig.write(path, settings);
    }

    private String generateModelConfig(ModelConfig modelConfig, int pos, long level) throws IOException {
        String path = settings.getAbsoluteConfigPath() + File.separator + id + "_" + pos + "_" + "level" + "_model.txt";
        modelConfig.write(path,settings, level);
    }

    private void registerResults(ArrayList<Result> results) {

    }

    private void buildModel(ModelConfig modelConfig) {

    }

    private ArrayList<Result> parse(ModelConfig modelConfig) {
        return null;
    }

    private boolean exec(String command){
        Channel channel = null;
        try{
            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

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

    private boolean convert(String config) {
       String command = "python " + settings.getConverterWinPath() + " " + config;
       return exec(command);
    }

    private void registerModel(ModelConfig modelConfig) {
        /*synchronized (Main.getConnection()){

        }*/
    }
}

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

    public Task(CommonSettings settings, ConverterConfig[] converter, ModelConfig[] model) throws JSchException {
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

    public void execute(){
        for (int i = 0, j = 0; i < converter.length && j < model.length; i++,j++) {
            //registerModel(model[i]);
            boolean f = exec("cd");
            System.out.println(f);
            String converterConfig = generateConverterConfig(converter[i]);
            String logPath = settings.getAbsoluteLogPath() + File.separator + id +"_" + i +"_converter_log.txt";
            convert(converter[i],logPath, converterConfig);
            /*buildModel(model[i]);
            ArrayList<Result> results = parse(model[i]);
            registerResults(results);*/
        }
        session.disconnect();
    }

    private String generateConverterConfig(ConverterConfig converterConfig){
        return null;
    }

    private String generateModelConfig(ModelConfig modelConfig){
        return null;
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

    private void convert(ConverterConfig converterConfig, String logPath, String config) {
       /* try(BufferedWriter writer = new BufferedWriter(new FileWriter(logPath))){
            String command = "python ";
            String host = "192.168.0.10";
            String user = "SmirnygaTotoshka";
            String password = "linageenddo1";

            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream input = channel.getInputStream();
            channel.connect();

            writer.write("Channel Connected to machine " + host + " server with command:\n" + command + "\n");

            try{
                InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    writer.write(line + "\n");
                }
                bufferedReader.close();
                inputReader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            channel.disconnect();
            session.disconnect();
        }catch(Exception ex){
            ex.printStackTrace();
        }*/
    }

    private void registerModel(ModelConfig modelConfig) {
        /*synchronized (Main.getConnection()){

        }*/
    }
}

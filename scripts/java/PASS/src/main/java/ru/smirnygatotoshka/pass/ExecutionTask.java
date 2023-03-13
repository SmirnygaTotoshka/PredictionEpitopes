package ru.smirnygatotoshka.pass;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.concurrent.Task;

import java.io.*;
import java.util.Properties;

public class ExecutionTask extends Task<Void> {

   public static String PROGRAM_NAME = "OLMPASSdoSAR.exe";

   private Config config;
   private Model[] models;

    public ExecutionTask(Config config, Model[] models) {
        this.config = config;
        this.models = models;
    }


    private boolean exec(String command){
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
            return channel.getExitStatus() == 0;
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

    @Override
    public Void call(){
        for (Model m : models) {
            String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\\\");
            String win_path = win_dir + "\\" + (PROGRAM_NAME.contentEquals("OLMPASSdoSAR.exe") ? m.getName(Model.NEEDED_FILE.EXECUTION_CONFIG) :
                    m.getName(Model.NEEDED_FILE.VALIDATION_CONFIG));
            String command = Config.WINDOWS_PASS_PATH + "\\" + PROGRAM_NAME + " " + win_path;
            m.setStatus(Model.STATUS.EXECUTION);
            boolean success = exec(command);
            if (!config.isFiveCV() || (config.isFiveCV() && PROGRAM_NAME.contentEquals("OLMPASS2CSV.exe"))) {
                if (success) {
                    m.success();
                } else {
                    m.fail(new Exception("Execution code != 0"));
                }
            }
            else if (config.isFiveCV() && PROGRAM_NAME.contentEquals("OLMPASSdoSAR.exe")){
                m.setStatus(Model.STATUS.WAITING_VALIDATION);
            }
        }
        return null;
    }
}

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

    @Override
    public Void call(){
        String win_path;
        for (Model m : models) {
            String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\\\");
            if (PROGRAM_NAME.contentEquals("OLMPASSdoSAR.exe")){
                win_path = win_dir + "\\" + m.getName(Model.NEEDED_FILE.EXECUTION_CONFIG);
                Distributor.getInstance().send(Model.STATUS.EXECUTION, m);
            }
            else{
                win_path = win_dir + "\\" + m.getName(Model.NEEDED_FILE.VALIDATION_CONFIG);
                Distributor.getInstance().send(Model.STATUS.VALIDATION, m);
            }
            String command = Config.WINDOWS_PASS_PATH + "\\" + PROGRAM_NAME + " " + win_path;
            boolean success = exec(command);
            if (!config.isFiveCV() || (config.isFiveCV() && PROGRAM_NAME.contentEquals("OLMPASS2CSV.exe"))) {
                try {
                    boolean isParse = parseResult(m);
                    if (isParse){
                        m.success();
                    }
                    else {
                        String message = parseLog(m);
                        m.fail(new Exception(success?message:"Execution code != 0;" + message));
                    }
                } catch (IOException e) {
                        m.fail(e);
                }
            }
            else if (config.isFiveCV() && PROGRAM_NAME.contentEquals("OLMPASSdoSAR.exe")){
                m.setStatus(Model.STATUS.WAITING_VALIDATION);
            }
        }
        return null;
    }

    private String parseLog(Model m) throws IOException{
        StringBuilder msg = new StringBuilder();
        String result_filepath = config.getRemote_work_dir() + File.separator + m.getName(Model.NEEDED_FILE.LOG);
        BufferedReader reader = new BufferedReader(new FileReader(result_filepath));
        String line;
        while ((line = reader.readLine()) != null) {
            msg.append(line);
        }
        return msg.toString();
    }

    public boolean parseResult(Model m)throws IOException {
        boolean flag = false;
        if (m.getStatus().equals(Model.STATUS.EXECUTION)) {
            String result_filepath = config.getRemote_SAR() + File.separator + m.getName(Model.NEEDED_FILE.MODEL_OUTPUT);
            System.out.println(m.generateID() + "\t" + result_filepath);
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
}

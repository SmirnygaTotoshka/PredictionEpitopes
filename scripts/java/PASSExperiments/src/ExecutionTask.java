import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ExecutionTask extends Task{

    public static String PROGRAM_NAME = "OLMPASSdoSAR.exe";

    public ExecutionTask(int id, Config config, File[] files) {
        super(id, config, files);
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

    @Override
    protected boolean check() {
        return true;
    }

    @Override
    public Boolean call(){
        for (File cfg : files) {
            String win_dir = config.remotePathToWin(config.getRemote_work_dir()).replaceAll("/","\\\\");
            String win_path = win_dir + "\\" + cfg.getName();
            String command = Config.WINDOWS_PASS_PATH + "\\" + PROGRAM_NAME + " " + win_path;
            boolean success = exec(command);
            System.out.println(id + " " + success + " " + cfg.getName());
        }
        return check();
    }
}

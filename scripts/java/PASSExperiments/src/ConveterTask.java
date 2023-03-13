import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConveterTask extends Task{

    private Runtime runtime;

    public ConveterTask(int id, Config config, File[] files) {
        super(id, config, files);
        this.runtime = Runtime.getRuntime();
    }
    private File convert(File converter_config) throws IOException, InterruptedException {
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
            if (converter.waitFor() != 0){
                System.out.println("Error - Converting");
                throw new InterruptedIOException("Convert code != 0");
            }
        }
        else {
            System.out.println("Already converted.");
        }
        return out;
    }

    private void copySDFToRemote(File src) throws IOException {
        Path dst = Paths.get(config.getRemote_data() + File.separator + src.getName());
        File dstFile = dst.toFile();
        if (!dstFile.exists()) {
            Files.copy(src.toPath(), dst);
        }
        System.out.println(src.getAbsolutePath() + " is copied");
    }

    @Override
    public Boolean call(){
        for (File f: files) {
            try {
                System.out.println("-----------------------------------------------");
                System.out.println("Convert " + f.getName());
                File sdf = convert(f);//TODO check existence
                System.out.println("Copy " + sdf.getName());
                copySDFToRemote(sdf);
            }
            catch (IOException | InterruptedException e) {
                return false;
            }
        }
        return check();
    }

    @Override
    protected boolean check() {
        File[] local = new File(config.getConverter_output()).listFiles((file, s) -> s.toLowerCase().endsWith(".sdf"));
        File[] remote = new File(config.getRemote_data()).listFiles((file, s) -> s.toLowerCase().endsWith(".sdf"));

        for (int i = 0; i < local.length;i++){
            String local_name = local[i].getName();
            long local_size = local[i].length();
            boolean find_exist = false;
            for (int j = 0; j < remote.length; j++) {
                String remote_name = remote[j].getName();
                long remote_size = remote[j].length();
                if (remote_name.contentEquals(local_name)){
                    find_exist = true;
                    if (remote_size != local_size){
                        return false;
                    }
                    else break;
                }
            }
            if (!find_exist) return false;
        }
        return true;
    }
}

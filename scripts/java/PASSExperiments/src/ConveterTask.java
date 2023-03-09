import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ConveterTask extends Task{

    private Runtime runtime;
    private CountDownLatch latch;
    public ConveterTask(int id, Config config, File[] files, CountDownLatch latch) {
        super(id, config, files);
        this.runtime = Runtime.getRuntime();
        this.latch = latch;
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
            System.out.println(converter.waitFor());
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
    public void run() {
        for (File f: files) {
            try {
                System.out.println("-----------------------------------------------");
                System.out.println("Convert " + f.getName());
                File sdf = convert(f);
                System.out.println("Copy " + sdf.getName());
                copySDFToRemote(sdf);
            }
            catch (IOException | InterruptedException e) {
                System.out.println(e);
            }
        }
        latch.countDown();
    }
}

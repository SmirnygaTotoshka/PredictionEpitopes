package ru.smirnygatotoshka.pass;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConveterTask extends Task<Void> {

    private Runtime runtime;
    private Config config;
    private Model[] models;

    public ConveterTask(Config config, Model[] models) {
        this.models = models;
        this.runtime = Runtime.getRuntime();
        this.config = config;
    }
    private File convert(Model model) throws IOException, InterruptedException {
        File out = new File(config.getConverter_output() + File.separator + model.getName(Model.NEEDED_FILE.SAMPLE));
        if (!out.exists()) {
            System.out.println("Convert for " + model.getName(Model.NEEDED_FILE.SAMPLE));
            Process converter = runtime.exec(new String[]{
                    "/home/stotoshka/Soft/anaconda3/envs/research/bin/python",
                    config.getConverter(),
                    config.getLocal_configs() + File.separator + model.getName(Model.NEEDED_FILE.CONVERTER_CONFIG)
            });
            BufferedReader r = new BufferedReader(new InputStreamReader(converter.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
            }
            if (converter.waitFor() != 0){
                System.out.println("Error - Converting");
                InterruptedIOException e = new InterruptedIOException("Convert code != 0");
                model.fail(e);
                throw e;
            }
        }
        else {
            System.out.println(model.generateID() + " already converted.");
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
    public Void call(){
        for (Model m: models) {
            try {
                System.out.println("-----------------------------------------------");
                System.out.println("Convert " + m.generateID());
                m.setStatus(Model.STATUS.CONVERT);
                File sdf = convert(m);
                System.out.println("Copy " + sdf.getName());
                m.setStatus(Model.STATUS.COPY_SAMPLE);
                copySDFToRemote(sdf);
                m.setStatus(Model.STATUS.WAITING_EXECUTION);
            }
            catch (IOException | InterruptedException e) {
                m.fail(e);
                return null;
            }
        }
        return null;
    }
}

package ru.smirnygatotoshka.pass;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConveterTask extends Task<Void> {

    private Runtime runtime;
    private Config config;
    private Model[] models;

    public ConveterTask(Config config, Model[] models) {
        this.models = models;
        this.runtime = Runtime.getRuntime();
        this.config = config;
    }
    private ArrayList<File> convert(Model model) throws IOException, InterruptedException {
        ArrayList<File> files = new ArrayList<>(2);
        File out = new File(config.getConverter_output() + File.separator + model.getName(Model.NEEDED_FILE.SAMPLE));
        File out_test = new File(config.getConverter_output() + File.separator + model.getModel_name() + "_test_" + model.getFold() + ".sdf");
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
            else {
                files.add(out);
            }
        }
        else {
            System.out.println(model.generateID() + " already converted.");
        }
        if (config.isFiveCV() && model.getMode().contentEquals(Model.VALIDATION_MODE)) {
            if (!out_test.exists()) {
                System.out.println("Convert for " + model.getModel_name() + "_test_" + model.getFold() + ".sdf");
                Process converter = runtime.exec(new String[]{
                        "/home/stotoshka/Soft/anaconda3/envs/research/bin/python",
                        config.getConverter(),
                        config.getLocal_configs() + File.separator + model.getModel_name() + "_test_" + model.getFold() + ".json"
                });
                BufferedReader r = new BufferedReader(new InputStreamReader(converter.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    System.out.println(line);
                }
                if (converter.waitFor() != 0) {
                    System.out.println("Error - Converting");
                    InterruptedIOException e = new InterruptedIOException("Convert code != 0");
                    model.fail(e);
                    throw e;
                } else {
                    files.add(out_test);
                }
            } else {
                System.out.println(model.generateID() + " already converted.");
            }
        }
        return files;
    }

    private void copySDFToRemote(ArrayList<File> source) throws IOException {
        for (File src: source) {
            System.out.println("Copy " + src.getName());
            Path dst = Paths.get(config.getRemote_data() + File.separator + src.getName());
            File dstFile = dst.toFile();
            if (!dstFile.exists()) {
                Files.copy(src.toPath(), dst);
            }
            System.out.println(src.getAbsolutePath() + " is copied");
        }
    }

    private void buildModel(){

    }


    @Override
    public Void call(){
        for (Model m: models) {
            try {
                System.out.println("-----------------------------------------------");
                Distributor.getInstance().send(Model.STATUS.CONVERT, m);
                System.out.println("Convert " + m.generateID());
                ArrayList<File> sdf = convert(m);
                Distributor.getInstance().send(Model.STATUS.COPY_SAMPLE, m);
                copySDFToRemote(sdf);
                Distributor.getInstance().send(Model.STATUS.WAITING_EXECUTION, m);
            }
            catch (IOException | InterruptedException e) {
                m.fail(e);
                return null;
            }
        }
        return null;
    }
}

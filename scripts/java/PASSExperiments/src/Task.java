import com.jcraft.jsch.*;
import databaseModels.Result;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public abstract class Task implements Callable<Boolean> {

    protected File[] files;

    protected int id;
    protected Config config;


    public Task(int id,Config config, File[] files){
        this.id = id;
        this.config = config;
        this.files = files;
    }


    private void registerResults(ArrayList<Result> results) {

    }

    private void buildModel() {

    }

    private ArrayList<Result> parse() {
        return null;
    }


    private void registerModel() {
        /*synchronized (Main.getConnection()){

        }*/
    }

    protected abstract boolean check();
}

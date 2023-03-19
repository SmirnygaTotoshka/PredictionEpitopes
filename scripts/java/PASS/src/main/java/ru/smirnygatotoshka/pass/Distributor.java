package ru.smirnygatotoshka.pass;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Distributor {

    private static volatile Distributor instance;
    private static Object mutex = new Object();

    private Distributor() {
    }

    public static Distributor getInstance() {
        Distributor result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new Distributor();
            }
        }
        return result;
    }
    private volatile ObservableList<Model> all_models;

    public void setAllModels(ObservableList<Model> all_models){
        this.all_models = all_models;
    }

    public ObservableList<Model> getAllModels(){
        return this.all_models;
    }

    public synchronized void send(Model.STATUS signal, Model model){
        if (signal.equals(Model.STATUS.WAITING_CONVERT) || signal.equals(Model.STATUS.CONVERT) ||
            signal.equals(Model.STATUS.COPY_SAMPLE) || signal.equals(Model.STATUS.WAITING_EXECUTION)){
            for (int i = 0; i < all_models.size(); i++) {
                if (all_models.get(i).getName(Model.NEEDED_FILE.CONVERTER_CONFIG).contentEquals(model.getName(Model.NEEDED_FILE.CONVERTER_CONFIG))){
                    all_models.get(i).setStatus(signal);
                }
            }
        }
        else {
            model.setStatus(signal);
        }
    }


    public ArrayList<Model[]> distributeConverterTasks(long threads)throws IOException{
        ObservableList<Model> tmp = FXCollections.observableArrayList();
        tmp.add(all_models.get(0));
        for (int i = 1; i < all_models.size(); i++) {
            boolean not_in = true;
            for (int j = 0; j < tmp.size(); j++) {
                if (tmp.get(j).getName(Model.NEEDED_FILE.CONVERTER_CONFIG).contentEquals(all_models.get(i).getName(Model.NEEDED_FILE.CONVERTER_CONFIG))){
                    not_in = false;
                }
            }
            if (not_in)
                tmp.add(all_models.get(i));
        }
        return distribute(tmp, threads);
    }

    public ArrayList<Model[]> distribute(ObservableList<Model> models, long threads) throws IOException {
        int number_threads = Math.toIntExact(threads);
        if (models.size() < number_threads)
            number_threads = models.size();
        int part_size = models.size() / number_threads;
        int last_part_size = part_size + (models.size() - part_size * number_threads);
        ArrayList<Model[]> distr_models = new ArrayList<>(number_threads);
        Model[] part;
        for (int i = 0, start = 0; i < number_threads; i++, start += part_size) {
            if (i == number_threads - 1)
                part = slice(models, start, start + last_part_size);
            else
                part =  slice( models, start, start + part_size);
            distr_models.add(part);
        }
        return distr_models;
    }

    private Model[] slice(ObservableList<Model> input, int start, int end){
        Model[] slicedArray = new Model[end - start];
        for (int i = 0; i < slicedArray.length; i++) {
            slicedArray[i] = input.get(start + i);
        }
        return slicedArray;
    }

}

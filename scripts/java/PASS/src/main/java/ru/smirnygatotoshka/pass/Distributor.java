package ru.smirnygatotoshka.pass;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Distributor {

    public ArrayList<Model[]> distributeConverterTasks(ObservableList<Model> models, long threads)throws IOException{
        ObservableList<Model> tmp = FXCollections.observableArrayList();
        tmp.add(models.get(0));
        for (int i = 1; i < models.size(); i++) {
            boolean not_in = true;
            for (int j = 0; j < tmp.size(); j++) {
                if (tmp.get(j).getName(Model.NEEDED_FILE.CONVERTER_CONFIG).contentEquals(models.get(i).getName(Model.NEEDED_FILE.CONVERTER_CONFIG))){
                    not_in = false;
                }
            }
            if (not_in)
                tmp.add(models.get(i));
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

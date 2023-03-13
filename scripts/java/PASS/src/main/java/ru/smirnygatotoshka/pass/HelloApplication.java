package ru.smirnygatotoshka.pass;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class HelloApplication extends Application {

    public static File config;
    @Override
    public void start(Stage stage) throws IOException, ParseException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("PASS Experiments");
        stage.setScene(scene);
        stage.show();
       /* if (config.exists()) {
            Program program = new Program(config);
            program.start(scene);
            //printModelStatuses(all_models);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Alert");
            alert.setHeaderText("Can not find config");
            alert.setContentText("The config does not exists!");

            alert.show();
            Platform.exit();
        }*/
    }

    public static void main(String[] args) {
        config = new File(args[0]);
        if (config.exists())
            launch();
        else {
            System.out.println("Config doesnt exist!");
        }
    }
}
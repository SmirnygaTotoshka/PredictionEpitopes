package ru.smirnygatotoshka.pass;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HelloController {
    @FXML
    private TableView<Model> status;

    @FXML
    private Button start;

    @FXML
    private ComboBox<String> task_type;

    private PrintStream ps;
    @FXML
    private TextArea console;

    public void initialize() {
        ps = new PrintStream(new Console(console)) ;
    }

    public class Console extends OutputStream {
        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
    }
    private static final String url = "jdbc:mysql://192.168.0.11:3306/experiments";
    private static final String user = "root";
    private static final String password = "meow_root";

    private volatile static Connection con;

    public synchronized static Connection getConnection() {
        return con;
    }

    public static void printModelStatuses(ObservableList<Model> all_models){
        for (Model m: all_models) {
            try {
                System.out.println(m.getId() + "\t" + m.getStatus() + "\t" + m.isSuccess());
            } catch (Exception e) {
                System.out.println(m.getId() + "\t" + m.getStatus() + "\t" + e);
            }
        }
    }
    @FXML
    private void startProgram() throws SQLException {
        System.setOut(ps);
        System.setErr(ps);
        System.out.println("Starting at " + new Date());
        start.setDisable(true);
        try {
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Success connection");
            JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(HelloApplication.config));
            Config settings = new Config(jo);
            System.out.println(settings);
            System.out.println("---------------------------------------------------");
            System.out.println("Preparing");
            Distributor distributor = Distributor.getInstance();
            distributor.setAllModels(settings.prepare());
            System.out.println(new Date());
            System.out.println("---------------------------------------------------");

            printModelStatuses(distributor.getAllModels());
            status.getColumns().removeAll(status.getColumns());
            status.setItems(FXCollections.observableArrayList(distributor.getAllModels()));
            TableColumn<Model, String> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(new PropertyValueFactory<Model, String>("id"));
            status.getColumns().add(idColumn);
            TableColumn<Model, Model.STATUS> statusColumn = new TableColumn<>("Статус");
            statusColumn.setCellValueFactory(new PropertyValueFactory<Model, Model.STATUS>("statusNameProperty"));
            status.getColumns().add(statusColumn);
            TableColumn<Model, Model.STATUS> exceptionColumn = new TableColumn<>("Ошибки");
            exceptionColumn.setCellValueFactory(new PropertyValueFactory<Model, Model.STATUS>("exceptionMessage"));
            exceptionColumn.setPrefWidth(500);
            status.getColumns().add(exceptionColumn);

            status.setRowFactory(tv -> new TableRow<Model>() {
                @Override
                public void updateItem(Model item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (item == null) {
                        setStyle("");
                    }
                    else if (item.getStatusNameProperty().contentEquals(Model.STATUS.SUCCESS.name())){
                        setStyle("-fx-background-color: green;");
                    }
                    else if (item.getStatusNameProperty().contentEquals(Model.STATUS.FAILED.name())) {
                        setStyle("-fx-background-color: red;");
                    }
                    else {
                        setStyle("-fx-background-color: yellow;");
                    }
                }
            });
            System.out.println("---------------------------------------------------");
            System.out.println("Converting");

            ArrayList<Model[]> converter_files = distributor.distributeConverterTasks(settings.getThreads());
            ArrayList<ConveterTask> converters = new ArrayList<>();
            for (int i = 0; i < converter_files.size(); i++) {
                converters.add(new ConveterTask(settings, converter_files.get(i)));
                converters.get(i).setOnSucceeded(workerStateEvent -> {
                    ObservableList<Model> after_convert = distributor.getAllModels().stream().filter(model -> {
                        try {
                            return model.isSuccess();
                        } catch (Exception e) {
                            return false;
                        }
                    }).collect(Collectors.toCollection(FXCollections::observableArrayList));
                    try {
                        status.refresh();
                        ArrayList<Model[]> execution_files = distributor.distribute(after_convert,settings.getThreads());
                        ArrayList<ExecutionTask> executionTasks = new ArrayList<>();
                        for (int j = 0; j < execution_files.size(); j++) {
                            executionTasks.add(new ExecutionTask(settings, execution_files.get(j)));
                            executionTasks.get(j).setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent workerStateEvent) {
                                        status.refresh();
                                        if (settings.isFiveCV()) {
                                            ObservableList<Model> after_execute = after_convert.stream().filter(model -> {
                                                try {
                                                    return model.isSuccess();
                                                } catch (Exception e) {
                                                    return false;
                                                }
                                            }).collect(Collectors.toCollection(FXCollections::observableArrayList));
                                            try {

                                                ArrayList<Model[]> validation_files = distributor.distribute(after_execute, settings.getThreads());
                                                ArrayList<ExecutionTask> validationTasks = new ArrayList<>();
                                                ExecutionTask.PROGRAM_NAME = "OLMPASS2CSV.exe";
                                                for (int j = 0; j < validation_files.size(); j++) {
                                                    validationTasks.add(new ExecutionTask(settings, validation_files.get(j)));
                                                }
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                });
                            new Thread(executionTasks.get(j)).start();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                new Thread(converters.get(i)).start();
                //threadsConverter.get(i).join();
            }

        }
        catch (SQLException | IOException | ParseException e) {
            System.out.println("Failed " + e);
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Alert");
            alert.setHeaderText("Error");
            alert.setContentText(e.getMessage());
            alert.show();
        }
        finally {
            status.refresh();
            con.close();
            start.setDisable(false);
        }
    }

}
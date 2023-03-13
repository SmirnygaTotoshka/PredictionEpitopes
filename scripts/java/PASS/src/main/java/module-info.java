module ru.smirnygatotoshka.pass {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.sql;
    requires jsch;


    opens ru.smirnygatotoshka.pass to javafx.fxml;
    exports ru.smirnygatotoshka.pass;
}
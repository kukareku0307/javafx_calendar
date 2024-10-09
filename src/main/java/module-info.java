module com.example.kursach {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.apache.logging.log4j;


    opens com.example.kursach to javafx.fxml;
    exports com.example.kursach;
}
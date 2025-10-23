module org.example.appbanquee {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports org.example.appbanquee;
    opens org.example.appbanquee to javafx.fxml;
}
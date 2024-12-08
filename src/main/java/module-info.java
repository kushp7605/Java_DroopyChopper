module com.example.droopychopper {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.droopychopper to javafx.fxml;
    exports com.example.droopychopper;
}
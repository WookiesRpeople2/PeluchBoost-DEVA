module com.example.spring_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.boot;
    requires spring.webflux;
    requires java.sql;
    requires reactor.core;
    requires spring.beans;
    requires spring.core;
    requires jdk.unsupported;

    opens com.example.spring_frontend to javafx.fxml, spring.core, spring.beans, spring.context, java.sql;
    opens com.example.spring_frontend.models to spring.core, spring.beans, spring.context;
    opens com.example.spring_frontend.controllers to spring.core, spring.beans, spring.context, javafx.fxml;
    opens com.example.spring_frontend.services to spring.core, spring.beans, spring.context;
    opens com.example.spring_frontend.repositorys to spring.core, spring.beans, spring.context;
    exports com.example.spring_frontend;
}

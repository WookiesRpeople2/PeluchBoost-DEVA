package packages.ORM.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import packages.ORM.SchemaManager;

public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Connection connection;
    private final String url;
    private final String username;
    private final String password;
    private final String basePackage;
    private final Properties properties;

    private static final int RETRY_CONNECTION = 20;
    private static final int INITIAL_SLEEP_TIME = 6000;
    private static final int CONNECTION_TIMEOUT = 5;


//    public DatabaseConfig() {
//        if (isSpringAvailable()) {
//            Properties springProps = loadFromSpring();
//            this.url = springProps.getProperty("url");
//            this.username = springProps.getProperty("username");
//            this.password = springProps.getProperty("password");
//            this.basePackage = springProps.getProperty("basePackage");
//            this.properties = springProps;
//        } else {
//            throw new IllegalStateException("Spring environment not available. Please use the builder pattern for manual configuration.");
//        }
//        initConnection();
//    }


    private DatabaseConfig(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.basePackage = builder.basePackage;
        this.properties = builder.properties;
        initConnection();
    }

    public static class Builder {
        private String url;
        private String username;
        private String password;
        private String basePackage;
        private final Properties properties = new Properties();


        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder basePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }

        public Builder property(String key, String value) {
            this.properties.setProperty(key, value);
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }



    private void initConnection() {
        Exception lastException = null;

        for (int i = 0; i < RETRY_CONNECTION; i++) {
            try {
                this.connection = DriverManager.getConnection(url, username, password);
                this.connection.setAutoCommit(true);

                if (this.connection.isValid(CONNECTION_TIMEOUT)) {
                    SchemaManager.initialize(connection, basePackage);
                    return;
                }
            } catch (SQLException e) {
                try {
                    Thread.sleep(INITIAL_SLEEP_TIME);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Connection retry interrupted", ie);
                }
            }
        }
        throw new RuntimeException("Failed to initialize database connection after " +
                RETRY_CONNECTION + " attempts", lastException);
    }

    public static void initialize(DatabaseConfig config) {
        if (instance == null) {
            instance = config;
        }
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseConfig not initialized. Call initialize() first.");
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initConnection();
            }
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close database connection", e);
        }
    }
}

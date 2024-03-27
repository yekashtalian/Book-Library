package dao;

import exception.DbUtilOperationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
  private static final Properties properties = loadProperties();

  private static Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream inputStream = new FileInputStream("src/main/resources/db/db.properties")) {
      properties.load(inputStream);
    } catch (IOException e) {
      System.err.println("Cannot find database properties file " + e.getMessage());
    }
    return properties;
  }

  public static Connection getConnection() {
    String url = properties.getProperty("db.conn.url");
    String userName = properties.getProperty("db.username");
    String password = properties.getProperty("db.password");

    try {
      return DriverManager.getConnection(url, userName, password);
    } catch (SQLException e) {
      throw new DbUtilOperationException(
          "Unable to establish database connection, please come later", e);
    }
  }
}

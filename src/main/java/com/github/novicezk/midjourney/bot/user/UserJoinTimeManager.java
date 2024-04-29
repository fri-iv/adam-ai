package com.github.novicezk.midjourney.bot.user;

import java.sql.*;
import java.time.ZonedDateTime;

public class UserJoinTimeManager {

    private static final String DATABASE_URL = "jdbc:sqlite:generations_count.db";

    static {
        initializeDatabase();
    }

    public static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS join_times (user_id VARCHAR(255) PRIMARY KEY, join_time TIMESTAMP)");
        } catch (SQLException e) {
            System.out.println("Failed to initialize database: " + e.getMessage());
        }
    }

    public static void addUserJoinTime(String userId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM join_times WHERE user_id = ?");
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO join_times (user_id, join_time) VALUES (?, ?)");
            // Clear all the data for this userId
            deleteStatement.setString(1, userId);
            deleteStatement.executeUpdate();

            insertStatement.setString(1, userId);
            insertStatement.setTimestamp(2, Timestamp.from(ZonedDateTime.now().toInstant()));
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to add or update join time for user " + userId + ": " + e.getMessage());
        }
    }

    public static ZonedDateTime getUserJoinTime(String userId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT join_time FROM join_times WHERE user_id = ?")) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp("join_time");
                return timestamp.toInstant().atZone(ZonedDateTime.now().getOffset());
            } else {
                System.out.println("No join time found for user " + userId);
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve join time for user " + userId + ": " + e.getMessage());
        }
        return null;
    }
}

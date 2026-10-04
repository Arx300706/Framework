package com.app.repository;

import com.app.model.Personne;
import com.app.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonneRepository {
    public void initializeDemoData() throws Exception {
        try (Connection connection = DatabaseConnection.getConnection()) {
            createTableIfNeeded(connection);
            insertDemoDataIfEmpty(connection);
        }
    }

    public List<Personne> findAll() throws Exception {
        List<Personne> personnes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, nom, age FROM personne ORDER BY id"
             );
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Personne personne = new Personne();
                personne.setId(resultSet.getInt("id"));
                personne.setNom(resultSet.getString("nom"));
                personne.setAge(resultSet.getInt("age"));
                personnes.add(personne);
            }
        }

        return personnes;
    }

    public int count() throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT COUNT(*) AS total FROM personne"
             );
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }

        return 0;
    }

    private void createTableIfNeeded(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS personne ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nom VARCHAR(100) NOT NULL, "
                    + "age INT NOT NULL)"
            );
        }
    }

    private void insertDemoDataIfEmpty(Connection connection) throws Exception {
        try (PreparedStatement countStatement = connection.prepareStatement(
                 "SELECT COUNT(*) AS total FROM personne"
             );
             ResultSet resultSet = countStatement.executeQuery()) {

            if (resultSet.next() && resultSet.getInt("total") > 0) {
                return;
            }
        }

        try (PreparedStatement insertStatement = connection.prepareStatement(
                 "INSERT INTO personne(nom, age) VALUES (?, ?)"
             )) {
            insertPerson(insertStatement, "Aina", 21);
            insertPerson(insertStatement, "Miora", 24);
            insertPerson(insertStatement, "Tiana", 19);
        }
    }

    private void insertPerson(PreparedStatement statement, String nom, int age) throws Exception {
        statement.setString(1, nom);
        statement.setInt(2, age);
        statement.executeUpdate();
    }
}

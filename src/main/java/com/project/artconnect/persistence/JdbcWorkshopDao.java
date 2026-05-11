package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = "SELECT w.title, w.date_, w.durationMinutes, w.maxParticipants, w.description, w.price, w.level, "
                + "ar.Email, ar.Name, ar.surname, ar.City, ar.Birth_Year, ar.bio, ar.phone, ar.website, ar.socialMedia, ar.isActive "
                + "FROM workshops w "
                + "JOIN Artist ar ON w.Email = ar.Email "
                + "WHERE w.id_workshops = ?";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(creerWorkshop(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'atelier", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> workshops = new ArrayList<>();
        String sql = "SELECT w.title, w.date_, w.durationMinutes, w.maxParticipants, w.description, w.price, w.level, "
                + "ar.Email, ar.Name, ar.surname, ar.City, ar.Birth_Year, ar.bio, ar.phone, ar.website, ar.socialMedia, ar.isActive "
                + "FROM workshops w "
                + "JOIN Artist ar ON w.Email = ar.Email";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                workshops.add(creerWorkshop(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des ateliers", e);
        }

        return workshops;
    }

    private Workshop creerWorkshop(ResultSet resultSet) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setTitle(resultSet.getString("title"));

        Timestamp date = resultSet.getTimestamp("date_");
        if (date != null) {
            workshop.setDate(date.toLocalDateTime());
        }

        workshop.setDurationMinutes(resultSet.getInt("durationMinutes"));
        workshop.setMaxParticipants(resultSet.getInt("maxParticipants"));
        workshop.setDescription(resultSet.getString("description"));
        workshop.setPrice(resultSet.getDouble("price"));
        workshop.setLevel(resultSet.getString("level"));
        workshop.setInstructor(creerArtist(resultSet));
        return workshop;
    }

    private Artist creerArtist(ResultSet resultSet) throws SQLException {
        Artist artist = new Artist();
        artist.setContactEmail(resultSet.getString("Email"));
        artist.setName(resultSet.getString("Name") + " " + resultSet.getString("surname"));
        artist.setCity(resultSet.getString("City"));
        artist.setBio(resultSet.getString("bio"));

        int birthYear = resultSet.getInt("Birth_Year");
        if (resultSet.wasNull()) {
            artist.setBirthYear(null);
        } else {
            artist.setBirthYear(birthYear);
        }

        artist.setPhone(resultSet.getString("phone"));
        artist.setWebsite(resultSet.getString("website"));
        artist.setSocialMedia(resultSet.getString("socialMedia"));
        artist.setActive(resultSet.getBoolean("isActive"));
        return artist;
    }
}

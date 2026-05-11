package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtistDao.
 * TODO: Students must implement this using JDBC and SQL.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM Artist";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Artist artist = creerArtist(resultSet);
                chargerDisciplines(connection, artist);
                artists.add(artist);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des artistes", e);
        }

        return artists;
    }

    @Override
    public void save(Artist artist) {
        throw new UnsupportedOperationException("Ajout non implemente pour le moment.");
    }

    @Override
    public void update(Artist artist) {
        throw new UnsupportedOperationException("Modification non implementee pour le moment.");
    }

    @Override
    public void delete(String artistName) {
        throw new UnsupportedOperationException("Suppression non implementee pour le moment.");
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM Artist WHERE City = ?";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, city);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Artist artist = creerArtist(resultSet);
                    chargerDisciplines(connection, artist);
                    artists.add(artist);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des artistes", e);
        }

        return artists;
    }

    public List<Discipline> findAllDisciplines() {
        List<Discipline> disciplines = new ArrayList<>();
        String sql = "SELECT name FROM Discipline ORDER BY name";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                disciplines.add(new Discipline(resultSet.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des disciplines", e);
        }

        return disciplines;
    }

    private Artist creerArtist(ResultSet resultSet) throws SQLException {
        Artist artist = new Artist();
        String prenom = resultSet.getString("Name");
        String nom = resultSet.getString("surname");
        artist.setName(prenom + " " + nom);
        artist.setBio(resultSet.getString("bio"));

        int birthYear = resultSet.getInt("Birth_Year");
        if (resultSet.wasNull()) {
            artist.setBirthYear(null);
        } else {
            artist.setBirthYear(birthYear);
        }

        artist.setContactEmail(resultSet.getString("Email"));
        artist.setPhone(resultSet.getString("phone"));
        artist.setCity(resultSet.getString("City"));
        artist.setWebsite(resultSet.getString("website"));
        artist.setSocialMedia(resultSet.getString("socialMedia"));
        artist.setActive(resultSet.getBoolean("isActive"));
        return artist;
    }

    private void chargerDisciplines(Connection connection, Artist artist) throws SQLException {
        String sql = "SELECT name FROM practice WHERE Email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, artist.getContactEmail());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Discipline discipline = new Discipline(resultSet.getString("name"));
                    artist.getDisciplines().add(discipline);
                }
            }
        }
    }
}

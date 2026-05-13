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
        String sql = "INSERT INTO Artist(Email, Name, surname, City, Birth_Year, bio, phone, website, socialMedia, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            String[] names = artist.getName().split(" ", 2);
            String firstName = names[0];
            String surname = "";
            if (names.length > 1) {
                surname = names[1];
            }

            statement.setString(1, artist.getContactEmail());
            statement.setString(2, firstName);
            statement.setString(3, surname);
            statement.setString(4, artist.getCity());
            if (artist.getBirthYear() == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, artist.getBirthYear());
            }
            statement.setString(6, artist.getBio());
            statement.setString(7, artist.getPhone());
            statement.setString(8, artist.getWebsite());
            statement.setString(9, artist.getSocialMedia());
            statement.setBoolean(10, artist.isActive());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'artiste", e);
        }
    }

    @Override
    public void update(Artist artist) {
        throw new UnsupportedOperationException("Modification non implementee pour le moment.");
    }

    @Override
    public void delete(String artistEmail) {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                supprimerDependances(connection, artistEmail);

                String deleteSql = "DELETE FROM Artist WHERE Email = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                    statement.setString(1, artistEmail);
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'artiste", e);
        }
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

    private void supprimerDependances(Connection connection, String artistEmail) throws SQLException {
        String deleteTagsSql = "DELETE t FROM tags t "
                + "JOIN Artworks aw ON t.id_artworks = aw.id_artworks "
                + "WHERE aw.Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteTagsSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }

        String deleteExhibitionsSql = "DELETE ip FROM is_part_of ip "
                + "JOIN Artworks aw ON ip.id_artworks = aw.id_artworks "
                + "WHERE aw.Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteExhibitionsSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }

        String deleteReviewsSql = "DELETE r FROM Review r "
                + "JOIN Artworks aw ON r.id_artworks = aw.id_artworks "
                + "WHERE aw.Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteReviewsSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }

        String deletePracticeSql = "DELETE FROM practice WHERE Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deletePracticeSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }

        String deleteBookingsSql = "DELETE b FROM booking b "
                + "JOIN workshops w ON b.id_workshops = w.id_workshops "
                + "WHERE w.Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteBookingsSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }

        String deleteWorkshopsSql = "DELETE FROM workshops WHERE Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteWorkshopsSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }

        String deleteArtworksSql = "DELETE FROM Artworks WHERE Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteArtworksSql)) {
            statement.setString(1, artistEmail);
            statement.executeUpdate();
        }
    }
}

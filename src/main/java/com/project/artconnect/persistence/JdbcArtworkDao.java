package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.Title, aw.creationYear, aw.description, aw.type, aw.price, aw.status, "
                + "ar.Email, ar.Name, ar.surname, ar.City, ar.Birth_Year, ar.bio, ar.phone, ar.website, ar.socialMedia, ar.isActive "
                + "FROM Artworks aw "
                + "JOIN Artist ar ON aw.Email = ar.Email";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                artworks.add(creerArtwork(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des oeuvres", e);
        }

        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        throw new UnsupportedOperationException("Ajout non implemente pour le moment.");
    }

    @Override
    public void update(Artwork artwork) {
        throw new UnsupportedOperationException("Modification non implementee pour le moment.");
    }

    @Override
    public void delete(String title) {
        throw new UnsupportedOperationException("Suppression non implementee pour le moment.");
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.Title, aw.creationYear, aw.description, aw.type, aw.price, aw.status, "
                + "ar.Email, ar.Name, ar.surname, ar.City, ar.Birth_Year, ar.bio, ar.phone, ar.website, ar.socialMedia, ar.isActive "
                + "FROM Artworks aw "
                + "JOIN Artist ar ON aw.Email = ar.Email "
                + "WHERE ar.Name LIKE ? OR ar.surname LIKE ?";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + artistName + "%");
            statement.setString(2, "%" + artistName + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    artworks.add(creerArtwork(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des oeuvres", e);
        }

        return artworks;
    }

    private Artwork creerArtwork(ResultSet resultSet) throws SQLException {
        Artwork artwork = new Artwork();
        artwork.setTitle(resultSet.getString("Title"));

        int creationYear = resultSet.getInt("creationYear");
        if (resultSet.wasNull()) {
            artwork.setCreationYear(null);
        } else {
            artwork.setCreationYear(creationYear);
        }

        artwork.setDescription(resultSet.getString("description"));
        artwork.setType(resultSet.getString("type"));
        artwork.setPrice(resultSet.getDouble("price"));

        String status = resultSet.getString("status");
        if (status != null) {
            artwork.setStatus(Artwork.Status.valueOf(status));
        }

        artwork.setArtist(creerArtist(resultSet));
        return artwork;
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

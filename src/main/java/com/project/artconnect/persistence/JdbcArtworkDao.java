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
        String sql = "SELECT aw.id_artworks, aw.Title, aw.creationYear, aw.description, aw.type, aw.price, aw.status, "
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
        String sql = "INSERT INTO Artworks(id_artworks, Title, creationYear, description, type, price, status, Email) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, getNextId(connection));
            statement.setString(2, artwork.getTitle());
            if (artwork.getCreationYear() == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, artwork.getCreationYear());
            }
            statement.setString(4, artwork.getDescription());
            statement.setString(5, artwork.getType());
            statement.setDouble(6, artwork.getPrice());
            statement.setString(7, artwork.getStatus().name());
            statement.setString(8, artwork.getArtist().getContactEmail());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'oeuvre", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        throw new UnsupportedOperationException("Modification non implementee pour le moment.");
    }

    @Override
    public void delete(int idArtworks) {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                supprimerDependances(connection, idArtworks);

                String deleteSql = "DELETE FROM Artworks WHERE id_artworks = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setInt(1, idArtworks);
                    deleteStatement.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'oeuvre", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();
        String sql = "SELECT aw.id_artworks, aw.Title, aw.creationYear, aw.description, aw.type, aw.price, aw.status, "
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
        artwork.setIdArtworks(resultSet.getInt("id_artworks"));
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

    private int getNextId(Connection connection) throws SQLException {
        String sql = "SELECT MAX(id_artworks) AS max_id FROM Artworks";

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("max_id") + 1;
            }
        }

        return 1;
    }

    private void supprimerDependances(Connection connection, int idArtwork) throws SQLException {
        String deleteTagsSql = "DELETE FROM tags WHERE id_artworks = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteTagsSql)) {
            statement.setInt(1, idArtwork);
            statement.executeUpdate();
        }

        String deleteExhibitionsSql = "DELETE FROM is_part_of WHERE id_artworks = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteExhibitionsSql)) {
            statement.setInt(1, idArtwork);
            statement.executeUpdate();
        }

        String deleteReviewsSql = "DELETE FROM Review WHERE id_artworks = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteReviewsSql)) {
            statement.setInt(1, idArtwork);
            statement.executeUpdate();
        }
    }
}

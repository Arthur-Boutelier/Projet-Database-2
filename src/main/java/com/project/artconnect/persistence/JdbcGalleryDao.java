package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT g.*, AVG(r.rating) AS average_rating "
                + "FROM Gallery g "
                + "LEFT JOIN Review r ON g.id_gallery = r.id_gallery "
                + "WHERE g.id_gallery = ? "
                + "GROUP BY g.id_gallery, g.Name, g.adress_number, g.adress_street, g.adress_city, "
                + "g.ownerName, g.OpeningHour, g.ClosingHour";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Gallery gallery = creerGallery(resultSet);
                    chargerExpositions(connection, gallery, id);
                    return Optional.of(gallery);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la galerie", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        String sql = "SELECT g.*, AVG(r.rating) AS average_rating "
                + "FROM Gallery g "
                + "LEFT JOIN Review r ON g.id_gallery = r.id_gallery "
                + "GROUP BY g.id_gallery, g.Name, g.adress_number, g.adress_street, g.adress_city, "
                + "g.ownerName, g.OpeningHour, g.ClosingHour";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Gallery gallery = creerGallery(resultSet);
                chargerExpositions(connection, gallery, resultSet.getLong("id_gallery"));
                galleries.add(gallery);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des galeries", e);
        }

        return galleries;
    }

    private Gallery creerGallery(ResultSet resultSet) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(resultSet.getString("Name"));

        String address = resultSet.getString("adress_number") + " "
                + resultSet.getString("adress_street") + ", "
                + resultSet.getString("adress_city");
        gallery.setAddress(address);

        gallery.setOwnerName(resultSet.getString("ownerName"));
        gallery.setOpeningHours(resultSet.getString("OpeningHour") + " - " + resultSet.getString("ClosingHour"));

        double rating = resultSet.getDouble("average_rating");
        if (resultSet.wasNull()) {
            gallery.setRating(-1);
        } else {
            gallery.setRating(rating);
        }

        return gallery;
    }

    private void chargerExpositions(Connection connection, Gallery gallery, Long idGallery) throws SQLException {
        String sql = "SELECT * FROM Exhibitions WHERE id_gallery = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idGallery);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Exhibition exhibition = creerExhibition(resultSet, gallery);
                    gallery.addExhibition(exhibition);
                }
            }
        }
    }

    private Exhibition creerExhibition(ResultSet resultSet, Gallery gallery) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(resultSet.getString("title"));

        Date startDate = resultSet.getDate("start_date");
        if (startDate != null) {
            exhibition.setStartDate(startDate.toLocalDate());
        }

        Date endDate = resultSet.getDate("endDate");
        if (endDate != null) {
            exhibition.setEndDate(endDate.toLocalDate());
        }

        exhibition.setDescription(resultSet.getString("description"));
        exhibition.setCuratorName(resultSet.getString("curatorName"));
        exhibition.setTheme(resultSet.getString("theme"));
        exhibition.setGallery(gallery);
        return exhibition;
    }
}

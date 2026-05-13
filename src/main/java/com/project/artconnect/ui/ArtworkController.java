package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtworkController {
    @FXML
    private TextField searchField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField artistEmailField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField priceField;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        refreshTable();
    }

    @FXML
    private void handleAddArtwork() {
        String title = titleField.getText();
        String artistEmail = artistEmailField.getText();
        String type = typeField.getText();
        String yearText = yearField.getText();
        String priceText = priceField.getText();

        if (title.isBlank() || artistEmail.isBlank()) {
            messageLabel.setText("Title and artist email are required.");
            return;
        }

        Artwork artwork = new Artwork();
        artwork.setTitle(title);
        artwork.setType(type);
        artwork.setStatus(Artwork.Status.FOR_SALE);

        Artist artist = new Artist();
        artist.setContactEmail(artistEmail);
        artwork.setArtist(artist);

        if (!yearText.isBlank()) {
            try {
                artwork.setCreationYear(Integer.parseInt(yearText));
            } catch (NumberFormatException e) {
                messageLabel.setText("Year must be a number.");
                return;
            }
        }

        if (!priceText.isBlank()) {
            try {
                artwork.setPrice(Double.parseDouble(priceText));
            } catch (NumberFormatException e) {
                messageLabel.setText("Price must be a number.");
                return;
            }
        }

        try {
            artworkService.createArtwork(artwork);
            clearAddForm();
            refreshTable();
            messageLabel.setText("Artwork added.");
        } catch (RuntimeException e) {
            messageLabel.setText("Error while adding artwork.");
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            refreshTable();
            return;
        }

        artworkTable.setItems(FXCollections.observableArrayList(
                artworkService.getAllArtworks().stream()
                        .filter(a -> a.getTitle().toLowerCase().contains(query.toLowerCase()))
                        .toList()));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    private void handleDeleteArtwork() {
        Artwork selectedArtwork = artworkTable.getSelectionModel().getSelectedItem();

        if (selectedArtwork == null) {
            messageLabel.setText("Select an artwork first.");
            return;
        }

        try {
            artworkService.deleteArtwork(selectedArtwork.getIdArtworks());
            refreshTable();
            messageLabel.setText("Artwork deleted.");
        } catch (RuntimeException e) {
            messageLabel.setText("Error while deleting artwork.");
        }
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private void clearAddForm() {
        titleField.clear();
        artistEmailField.clear();
        typeField.clear();
        yearField.clear();
        priceField.clear();
    }
}

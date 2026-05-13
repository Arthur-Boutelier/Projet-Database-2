package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TextField emailField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField birthYearField;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAddArtist() {
        String email = emailField.getText();
        String firstName = firstNameField.getText();
        String surname = surnameField.getText();
        String city = cityField.getText();
        String birthYearText = birthYearField.getText();

        if (email.isBlank() || firstName.isBlank() || surname.isBlank()) {
            messageLabel.setText("Email, first name and surname are required.");
            return;
        }

        Artist artist = new Artist();
        artist.setContactEmail(email);
        artist.setName(firstName + " " + surname);
        artist.setCity(city);
        artist.setActive(true);

        if (!birthYearText.isBlank()) {
            try {
                artist.setBirthYear(Integer.parseInt(birthYearText));
            } catch (NumberFormatException e) {
                messageLabel.setText("Birth year must be a number.");
                return;
            }
        }

        try {
            artistService.createArtist(artist);
            clearAddForm();
            refreshTable();
            messageLabel.setText("Artist added.");
        } catch (RuntimeException e) {
            messageLabel.setText("Error while adding artist.");
        }
    }

    @FXML
    private void handleDeleteArtist() {
        Artist selectedArtist = artistTable.getSelectionModel().getSelectedItem();

        if (selectedArtist == null) {
            messageLabel.setText("Select an artist first.");
            return;
        }

        try {
            artistService.deleteArtist(selectedArtist.getContactEmail());
            refreshTable();
            messageLabel.setText("Artist deleted.");
        } catch (RuntimeException e) {
            messageLabel.setText("Error while deleting artist.");
        }
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private void clearAddForm() {
        emailField.clear();
        firstNameField.clear();
        surnameField.clear();
        cityField.clear();
        birthYearField.clear();
    }
}

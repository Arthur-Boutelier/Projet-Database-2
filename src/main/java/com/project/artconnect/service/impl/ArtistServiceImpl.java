package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.service.ArtistService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtistServiceImpl implements ArtistService {
    private final JdbcArtistDao artistDao = new JdbcArtistDao();

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        for (Artist artist : artistDao.findAll()) {
            if (artist.getName().equalsIgnoreCase(name)) {
                return Optional.of(artist);
            }
        }
        return Optional.empty();
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(String name) {
        artistDao.delete(name);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        return artistDao.findAllDisciplines();
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        List<Artist> result = new ArrayList<>();

        for (Artist artist : artistDao.findAll()) {
            boolean ok = true;

            if (query != null && !query.isEmpty()
                    && !artist.getName().toLowerCase().contains(query.toLowerCase())) {
                ok = false;
            }

            if (city != null && !city.isEmpty() && !artist.getCity().equalsIgnoreCase(city)) {
                ok = false;
            }

            if (disciplineName != null && !disciplineName.isEmpty()) {
                boolean found = false;
                for (Discipline discipline : artist.getDisciplines()) {
                    if (discipline.getName().equalsIgnoreCase(disciplineName)) {
                        found = true;
                    }
                }
                if (!found) {
                    ok = false;
                }
            }

            if (ok) {
                result.add(artist);
            }
        }

        return result;
    }
}

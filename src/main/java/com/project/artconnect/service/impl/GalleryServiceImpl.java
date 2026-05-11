package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.GalleryService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GalleryServiceImpl implements GalleryService {
    private final JdbcGalleryDao galleryDao = new JdbcGalleryDao();

    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        for (Gallery gallery : galleryDao.findAll()) {
            if (gallery.getName().equalsIgnoreCase(name)) {
                return Optional.of(gallery);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) {
            return Collections.emptyList();
        }
        return gallery.getExhibitions();
    }
}

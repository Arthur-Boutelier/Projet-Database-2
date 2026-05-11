package com.project.artconnect.service.impl;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.WorkshopService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WorkshopServiceImpl implements WorkshopService {
    private final JdbcWorkshopDao workshopDao = new JdbcWorkshopDao();

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        for (Workshop workshop : workshopDao.findAll()) {
            if (workshop.getTitle().equalsIgnoreCase(title)) {
                return Optional.of(workshop);
            }
        }
        return Optional.empty();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        throw new UnsupportedOperationException("Reservation non implementee pour le moment.");
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        return Collections.emptyList();
    }
}

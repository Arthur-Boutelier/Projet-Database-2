package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 */
public class ServiceProvider {
    private static final ArtistService artistService = new ArtistServiceImpl();
    private static final InMemoryArtistService inMemoryArtistService = new InMemoryArtistService();
    private static final ArtworkService artworkService = new ArtworkServiceImpl();
    private static final InMemoryArtworkService inMemoryArtworkService = new InMemoryArtworkService();
    private static final GalleryService galleryService = new GalleryServiceImpl();
    private static final InMemoryGalleryService inMemoryGalleryService = new InMemoryGalleryService();
    private static final WorkshopService workshopService = new WorkshopServiceImpl();
    private static final InMemoryWorkshopService inMemoryWorkshopService = new InMemoryWorkshopService();
    private static final CommunityService communityService = new CommunityServiceImpl();
    private static final InMemoryCommunityService inMemoryCommunityService = new InMemoryCommunityService();

    static {
        // Initialize services with their dependencies
        inMemoryArtworkService.initData(inMemoryArtistService);
        inMemoryGalleryService.initData(inMemoryArtworkService);
        inMemoryWorkshopService.initData(inMemoryArtistService);
        inMemoryCommunityService.initData(inMemoryArtworkService);
    }

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}

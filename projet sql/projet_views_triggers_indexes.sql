USE db_art_connect;

-- =========================
-- VUES
-- =========================

-- Vue simple pour afficher les expositions avec leur galerie
CREATE OR REPLACE VIEW v_exhibition_gallery AS
SELECT
    e.id_exhibitions,
    e.title AS exhibition_title,
    e.start_date,
    e.endDate,
    e.theme,
    e.curatorName,
    g.Name AS gallery_name,
    g.adress_number,
    g.adress_street,
    g.adress_city,
    g.OpeningHour,
    g.ClosingHour
FROM Exhibitions e
JOIN Gallery g ON e.id_gallery = g.id_gallery;

-- Vue pour afficher les workshops avec l'artiste qui les anime
CREATE OR REPLACE VIEW v_workshop_artist AS
SELECT
    w.id_workshops,
    w.title AS workshop_title,
    w.date_,
    w.durationMinutes,
    w.maxParticipants,
    w.description,
    w.price,
    w.level,
    ar.Name AS artist_name,
    ar.surname AS artist_surname,
    ar.City AS artist_city,
    ar.website,
    ar.socialMedia
FROM workshops w
JOIN Artist ar ON w.Email = ar.Email;

-- Vue pour relier reviews, œuvres, galeries et membres
CREATE OR REPLACE VIEW v_review_artwork_member AS
SELECT
    r.id_artworks,
    aw.Title AS artwork_title,
    aw.type AS artwork_type,
    ar.Name AS artist_name,
    ar.surname AS artist_surname,
    g.Name AS gallery_name,
    m.name AS member_name,
    m.surname AS member_surname,
    r.rating,
    r.comment,
    r.reviewDate
FROM Review r
JOIN Artworks aw ON r.id_artworks = aw.id_artworks
JOIN Artist ar ON aw.Email = ar.Email
JOIN Gallery g ON r.id_gallery = g.id_gallery
JOIN CommunityMember m ON r.email = m.email;

-- Vue pour afficher les réservations de workshops avec les membres
CREATE OR REPLACE VIEW v_booking_workshop_member AS
SELECT
    w.title AS workshop_title,
    w.date_,
    w.price AS workshop_price,
    w.level,
    m.name AS member_name,
    m.surname AS member_surname,
    b.paymentStatus,
    b.bookingDate
FROM booking b
JOIN workshops w ON b.id_workshops = w.id_workshops
JOIN CommunityMember m ON b.email = m.email;

-- Vue pour compter les œuvres d'un artiste selon leur statut
CREATE OR REPLACE VIEW v_artist_artwork_count AS
SELECT
    ar.Email,
    ar.Name,
    ar.surname,
    ar.City,
    ar.isActive,
    COUNT(aw.id_artworks) AS nb_artworks_total,
    COUNT(CASE WHEN aw.status = 'FOR_SALE' THEN 1 END) AS nb_for_sale,
    COUNT(CASE WHEN aw.status = 'SOLD' THEN 1 END) AS nb_sold,
    COUNT(CASE WHEN aw.status = 'EXHIBITED' THEN 1 END) AS nb_exhibited
FROM Artist ar
LEFT JOIN Artworks aw ON ar.Email = aw.Email
GROUP BY ar.Email, ar.Name, ar.surname, ar.City, ar.isActive;

-- Vue pour calculer la note moyenne d'un artiste
CREATE OR REPLACE VIEW v_artist_avg_rating AS
SELECT
    ar.Email,
    ar.Name,
    ar.surname,
    ar.City,
    ar.isActive,
    COUNT(DISTINCT CONCAT(r.id_artworks, r.id_gallery, r.email)) AS nb_reviews,
    ROUND(AVG(r.rating), 2) AS avg_rating,
    MIN(r.rating) AS min_rating,
    MAX(r.rating) AS max_rating
FROM Artist ar
LEFT JOIN Artworks aw ON ar.Email = aw.Email
LEFT JOIN Review r ON aw.id_artworks = r.id_artworks
GROUP BY ar.Email, ar.Name, ar.surname, ar.City, ar.isActive;

-- Vue pour compter les expositions passées, en cours et à venir d'une galerie
CREATE OR REPLACE VIEW v_gallery_exhibition_count AS
SELECT
    g.id_gallery,
    g.Name AS gallery_name,
    g.adress_city,
    g.ownerName,
    COUNT(e.id_exhibitions) AS nb_total,
    COUNT(CASE WHEN CURDATE() BETWEEN e.start_date AND e.endDate THEN 1 END) AS nb_active,
    COUNT(CASE WHEN e.endDate < CURDATE() THEN 1 END) AS nb_past,
    COUNT(CASE WHEN e.start_date > CURDATE() THEN 1 END) AS nb_upcoming
FROM Gallery g
LEFT JOIN Exhibitions e ON g.id_gallery = e.id_gallery
GROUP BY g.id_gallery, g.Name, g.adress_city, g.ownerName;

-- Vue pour afficher les œuvres exposées dans chaque galerie
CREATE OR REPLACE VIEW v_gallery_artworks AS
SELECT
    g.id_gallery,
    g.Name AS gallery_name,
    g.adress_city,
    e.id_exhibitions,
    e.title AS exhibition_title,
    e.start_date,
    e.endDate,
    aw.id_artworks,
    aw.Title AS artwork_title,
    aw.type,
    aw.price,
    aw.status,
    ar.Name AS artist_name,
    ar.surname AS artist_surname
FROM Gallery g
JOIN Exhibitions e ON g.id_gallery = e.id_gallery
JOIN is_part_of ip ON e.id_exhibitions = ip.id_exhibitions
JOIN Artworks aw ON ip.id_artworks = aw.id_artworks
JOIN Artist ar ON aw.Email = ar.Email;

-- Vue pour voir quels membres ont laissé des avis dans les expositions
CREATE OR REPLACE VIEW v_exhibition_reviewers AS
SELECT
    e.id_exhibitions,
    e.title AS exhibition_title,
    g.Name AS gallery_name,
    aw.id_artworks,
    aw.Title AS artwork_title,
    m.name AS member_name,
    m.surname AS member_surname,
    m.membershipType,
    r.rating,
    r.comment,
    r.reviewDate
FROM Exhibitions e
JOIN Gallery g ON e.id_gallery = g.id_gallery
JOIN is_part_of ip ON e.id_exhibitions = ip.id_exhibitions
JOIN Artworks aw ON ip.id_artworks = aw.id_artworks
JOIN Review r ON aw.id_artworks = r.id_artworks
             AND r.id_gallery = g.id_gallery
JOIN CommunityMember m ON r.email = m.email;

-- Vue pour repérer rapidement les réservations impayées
CREATE OR REPLACE VIEW v_unpaid_bookings AS
SELECT
    m.name AS member_name,
    m.surname AS member_surname,
    m.email,
    w.title AS workshop_title,
    w.date_ AS workshop_date,
    w.price AS amount_due,
    b.bookingDate,
    b.paymentStatus
FROM booking b
JOIN workshops w ON b.id_workshops = w.id_workshops
JOIN CommunityMember m ON b.email = m.email
WHERE b.paymentStatus = 'PENDING'
ORDER BY b.bookingDate ASC;

-- =========================
-- TRIGGERS
-- =========================

DELIMITER //

-- Vérifie qu'un workshop n'est pas déjà complet
CREATE TRIGGER trg_check_workshop_capacity
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE current_participants INT;
    DECLARE max_allowed INT;

    SELECT COUNT(*) INTO current_participants
    FROM booking
    WHERE id_workshops = NEW.id_workshops;

    SELECT maxParticipants INTO max_allowed
    FROM workshops
    WHERE id_workshops = NEW.id_workshops;

    IF current_participants >= max_allowed THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur : Cet atelier a atteint son nombre maximum de participants.';
    END IF;
END
//

-- Vérifie que la date de fin d'une exposition est correcte
CREATE TRIGGER trg_check_exhibition_dates
BEFORE INSERT ON Exhibitions
FOR EACH ROW
BEGIN
    IF NEW.endDate < NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Erreur de saisie : la date de fin ne peut pas être antérieure à la date de début.';
    END IF;
END
//

-- Met automatiquement la date du jour pour une réservation si elle est absente
CREATE TRIGGER trg_default_booking_date
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    IF NEW.bookingDate IS NULL THEN
        SET NEW.bookingDate = CURRENT_DATE();
    END IF;
END
//

-- Force la note d'une review à rester entre 0 et 5
CREATE TRIGGER trg_format_review_rating
BEFORE INSERT ON Review
FOR EACH ROW
BEGIN
    IF NEW.rating < 0 THEN
        SET NEW.rating = 0;
    ELSEIF NEW.rating > 5 THEN
        SET NEW.rating = 5;
    END IF;
END
//

DELIMITER ;

-- =========================
-- INDEX
-- =========================

-- Index utiles pour rechercher les artistes plus vite
CREATE INDEX idx_artist_isActive ON Artist(isActive);
CREATE INDEX idx_artist_city ON Artist(City);
CREATE INDEX idx_artist_birthYear ON Artist(Birth_Year);
CREATE INDEX idx_artist_name ON Artist(Name, surname);

-- Index utiles pour rechercher les œuvres plus vite
CREATE INDEX idx_artworks_email ON Artworks(Email);
CREATE INDEX idx_artworks_status ON Artworks(status);
CREATE INDEX idx_artworks_type ON Artworks(type);
CREATE INDEX idx_artworks_price ON Artworks(price);

-- Index utiles pour les galeries
CREATE INDEX idx_gallery_city ON Gallery(adress_city);
CREATE INDEX idx_gallery_name ON Gallery(Name);

-- Index utiles pour les expositions
CREATE INDEX idx_exhibitions_gallery ON Exhibitions(id_gallery);
CREATE INDEX idx_exhibitions_dates ON Exhibitions(start_date, endDate);
CREATE INDEX idx_exhibitions_theme ON Exhibitions(theme);

-- Index utiles pour les workshops
CREATE INDEX idx_workshops_email ON workshops(Email);
CREATE INDEX idx_workshops_date ON workshops(date_);
CREATE INDEX idx_workshops_level_price ON workshops(level, price);

-- Index utiles pour les membres
CREATE INDEX idx_member_membership ON CommunityMember(membershipType);
CREATE INDEX idx_member_city ON CommunityMember(city);
CREATE INDEX idx_member_name ON CommunityMember(name, surname);

-- Index utiles pour les réservations
CREATE INDEX idx_booking_email ON booking(email);
CREATE INDEX idx_booking_date ON booking(bookingDate);
CREATE INDEX idx_booking_payment ON booking(paymentStatus);

-- Index utiles pour les reviews
CREATE INDEX idx_review_artwork ON Review(id_artworks);
CREATE INDEX idx_review_rating ON Review(rating);
CREATE INDEX idx_review_date ON Review(reviewDate);
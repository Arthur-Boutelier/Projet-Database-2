USE db_art_connect;

-- =========================
-- FONCTIONS
-- =========================

DELIMITER //

-- Calcule la note moyenne d'un artiste à partir des reviews de ses œuvres
CREATE FUNCTION fn_avg_rating_artist(p_email VARCHAR(50))
RETURNS DECIMAL(3,2)
DETERMINISTIC
BEGIN
    DECLARE avg_note DECIMAL(3,2) DEFAULT 0.00;

    SELECT ROUND(AVG(r.rating), 2) INTO avg_note
    FROM Review r
    JOIN Artworks aw ON r.id_artworks = aw.id_artworks
    WHERE aw.Email = p_email;

    RETURN IFNULL(avg_note, 0.00);
END
//

-- Compte le nombre d'œuvres d'un artiste
CREATE FUNCTION fn_count_artworks(p_email VARCHAR(50))
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE nb INT DEFAULT 0;

    SELECT COUNT(*) INTO nb
    FROM Artworks
    WHERE Email = p_email;

    RETURN nb;
END
//

-- Vérifie si un membre est déjà inscrit à un workshop
CREATE FUNCTION fn_is_booked(p_email VARCHAR(50), p_id_workshop INT)
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE already_booked INT DEFAULT 0;

    SELECT COUNT(*) INTO already_booked
    FROM booking
    WHERE email = p_email
      AND id_workshops = p_id_workshop;

    RETURN already_booked > 0;
END
//

-- Calcule le nombre de places restantes dans un workshop
CREATE FUNCTION fn_remaining_spots(p_id_workshop INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE max_p INT DEFAULT 0;
    DECLARE current_p INT DEFAULT 0;

    SELECT maxParticipants INTO max_p
    FROM workshops
    WHERE id_workshops = p_id_workshop;

    SELECT COUNT(*) INTO current_p
    FROM booking
    WHERE id_workshops = p_id_workshop;

    RETURN max_p - current_p;
END
//

-- Calcule le potentiel de vente d'un artiste
CREATE FUNCTION fn_sales_potential(p_email VARCHAR(50))
RETURNS DECIMAL(15,2)
DETERMINISTIC
BEGIN
    DECLARE total DECIMAL(15,2) DEFAULT 0.00;

    SELECT IFNULL(SUM(price), 0.00) INTO total
    FROM Artworks
    WHERE Email = p_email
      AND status = 'FOR_SALE';

    RETURN total;
END
//

-- =========================
-- PROCEDURES STOCKEES
-- =========================

-- Affiche le profil complet d'un artiste
CREATE PROCEDURE sp_artist_profile(IN p_email VARCHAR(50))
BEGIN
    SELECT Name, surname, City, Birth_Year, bio, website, socialMedia, isActive
    FROM Artist
    WHERE Email = p_email;

    SELECT id_artworks, Title, type, price, status
    FROM Artworks
    WHERE Email = p_email;

    SELECT name
    FROM practice
    WHERE Email = p_email;

    SELECT fn_avg_rating_artist(p_email) AS avg_rating;
END
//

-- Inscrit un membre à un workshop si des places restent disponibles
CREATE PROCEDURE sp_book_workshop(
    IN p_email VARCHAR(50),
    IN p_id_workshop INT,
    IN p_payment VARCHAR(50)
)
BEGIN
    DECLARE places_restantes INT DEFAULT 0;

    SET places_restantes = fn_remaining_spots(p_id_workshop);

    IF places_restantes <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Erreur : atelier complet, inscription impossible.';
    ELSE
        INSERT INTO booking (id_workshops, email, paymentStatus, bookingDate)
        VALUES (p_id_workshop, p_email, p_payment, CURRENT_DATE());

        SELECT CONCAT('Inscription confirmée pour ', p_email,
                      ' - places restantes : ', places_restantes - 1) AS message;
    END IF;
END
//

-- Change le statut des œuvres d'une exposition
CREATE PROCEDURE sp_update_exhibition_status(
    IN p_id_exhibition INT,
    IN p_new_status VARCHAR(50)
)
BEGIN
    DECLARE nb_updated INT DEFAULT 0;

    UPDATE Artworks
    SET status = p_new_status
    WHERE id_artworks IN (
        SELECT id_artworks
        FROM is_part_of
        WHERE id_exhibitions = p_id_exhibition
    );

    SET nb_updated = ROW_COUNT();

    SELECT CONCAT(nb_updated, ' oeuvre(s) passee(s) au statut : ', p_new_status) AS resultat;
END
//

-- Génère un petit rapport sur une galerie
CREATE PROCEDURE sp_gallery_report(IN p_id_gallery INT)
BEGIN
    SELECT Name, adress_number, adress_street, adress_city, ownerName,
           OpeningHour, ClosingHour
    FROM Gallery
    WHERE id_gallery = p_id_gallery;

    SELECT id_exhibitions, title, start_date, endDate, theme, curatorName
    FROM Exhibitions
    WHERE id_gallery = p_id_gallery;

    SELECT aw.Title, aw.type, aw.price, aw.status,
           ar.Name AS artist_name, ar.surname AS artist_surname
    FROM Artworks aw
    JOIN is_part_of ip ON aw.id_artworks = ip.id_artworks
    JOIN Exhibitions e ON ip.id_exhibitions = e.id_exhibitions
    JOIN Artist ar ON aw.Email = ar.Email
    WHERE e.id_gallery = p_id_gallery;

    SELECT ROUND(AVG(r.rating), 2) AS avg_gallery_rating
    FROM Review r
    WHERE r.id_gallery = p_id_gallery;
END
//

-- Résume les réservations impayées
CREATE PROCEDURE sp_unpaid_summary()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_name VARCHAR(50);
    DECLARE v_surname VARCHAR(50);
    DECLARE v_email VARCHAR(50);
    DECLARE v_total DECIMAL(15,2);

    DECLARE cur_unpaid CURSOR FOR
        SELECT m.name, m.surname, m.email, SUM(w.price) AS total_due
        FROM booking b
        JOIN CommunityMember m ON b.email = m.email
        JOIN workshops w ON b.id_workshops = w.id_workshops
        WHERE b.paymentStatus = 'PENDING'
        GROUP BY m.email, m.name, m.surname;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_unpaid (
        member_name VARCHAR(50),
        member_surname VARCHAR(50),
        email VARCHAR(50),
        total_due DECIMAL(15,2)
    );

    OPEN cur_unpaid;

    read_loop: LOOP
        FETCH cur_unpaid INTO v_name, v_surname, v_email, v_total;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;

        INSERT INTO tmp_unpaid VALUES (v_name, v_surname, v_email, v_total);
    END LOOP;

    CLOSE cur_unpaid;

    SELECT * FROM tmp_unpaid ORDER BY total_due DESC;
    DROP TEMPORARY TABLE tmp_unpaid;
END
//

DELIMITER ;

-- =========================
-- TRANSACTIONS DE TEST
-- =========================

-- T1 : ajoute un artiste avec ses œuvres et sa discipline
START TRANSACTION;

INSERT INTO Artist (Email, Name, surname, City, Birth_Year, bio, phone, website, socialMedia, isActive)
VALUES ('sophie.lambert@artconnect.com', 'Sophie', 'Lambert', 'Nantes', 1990, 'Abstract expressionist', '0600000006', 'www.sophielambert.com', '@sophielambert', TRUE);

INSERT INTO Artworks (id_artworks, Title, creationYear, description, type, price, status, Email)
VALUES
(6, 'Red Horizon', 2021, 'Large abstract canvas in warm tones', 'Painting', 1600.00, 'FOR_SALE', 'sophie.lambert@artconnect.com'),
(7, 'Golden Wave', 2022, 'Acrylic on wood with gold leaf', 'Painting', 2100.00, 'EXHIBITED', 'sophie.lambert@artconnect.com');

INSERT INTO practice (Email, name)
VALUES ('sophie.lambert@artconnect.com', 'Painting');

INSERT INTO tags (id_artworks, Name)
VALUES
(6, 'abstract'),
(7, 'colorful');

COMMIT;

-- T2 : réserve un workshop puis régularise le paiement
START TRANSACTION;

INSERT INTO booking (id_workshops, email, paymentStatus, bookingDate)
VALUES (1, 'benoit.dupont@mail.com', 'PENDING', CURRENT_DATE());

UPDATE booking
SET paymentStatus = 'PAID'
WHERE id_workshops = 1
  AND email = 'benoit.dupont@mail.com'
  AND paymentStatus = 'PENDING';

COMMIT;

-- T3 : crée une exposition et y rattache des œuvres
START TRANSACTION;

INSERT INTO Exhibitions (id_exhibitions, title, start_date, endDate, description, curatorName, theme, id_gallery)
VALUES (6, 'Abstract Worlds', '2026-07-01', '2026-07-31', 'Focus on abstract and expressionist works', 'Marie Duval', 'Abstract', 1);

INSERT INTO is_part_of (id_artworks, id_exhibitions)
VALUES
(1, 6),
(6, 6),
(7, 6);

INSERT INTO tags (id_artworks, Name)
VALUES (6, 'modern')
ON DUPLICATE KEY UPDATE Name = Name;

COMMIT;

-- T4 : régularise toutes les réservations impayées d’un membre
START TRANSACTION;

UPDATE booking
SET paymentStatus = 'PAID'
WHERE email = 'benoit.dupont@mail.com'
  AND paymentStatus = 'PENDING';

COMMIT;

SET SQL_SAFE_UPDATES = 0;

-- T5 : supprime un artiste et toutes ses données liées
START TRANSACTION;

SET @email_artist = 'hugo.leroy@artconnect.com';

DELETE t
FROM tags t
JOIN Artworks aw ON t.id_artworks = aw.id_artworks
WHERE aw.Email = @email_artist;

DELETE ip
FROM is_part_of ip
JOIN Artworks aw ON ip.id_artworks = aw.id_artworks
WHERE aw.Email = @email_artist;

DELETE r
FROM Review r
JOIN Artworks aw ON r.id_artworks = aw.id_artworks
WHERE aw.Email = @email_artist;

DELETE FROM practice
WHERE Email = @email_artist;

DELETE b
FROM booking b
JOIN workshops w ON b.id_workshops = w.id_workshops
WHERE w.Email = @email_artist;

DELETE FROM workshops
WHERE Email = @email_artist;

DELETE FROM Artworks
WHERE Email = @email_artist;

DELETE FROM Artist
WHERE Email = @email_artist;

COMMIT;


SELECT * FROM workshops;
SELECT * FROM workshops WHERE title = 'Digital Creation';
SELECT * FROM Artist WHERE Email = 'hugo.leroy@artconnect.com';
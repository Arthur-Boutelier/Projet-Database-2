USE db_art_connect;

-- Artistes de départ
INSERT INTO Artist
(Email, Name, surname, City, Birth_Year, bio, phone, website, socialMedia, isActive)
VALUES
('anna.martin@artconnect.com', 'Anna', 'Martin', 'Paris', 1988, 'Contemporary painter', '0600000001', 'www.annamartin.com', '@annamartin', TRUE),
('luc.moreau@artconnect.com', 'Luc', 'Moreau', 'Lyon', 1982, 'Modern sculptor', '0600000002', 'www.lucmoreau.com', '@lucmoreau', TRUE),
('emma.bernard@artconnect.com', 'Emma', 'Bernard', 'Marseille', 1991, 'Urban photographer', '0600000003', 'www.emmabernard.com', '@emmabernard', TRUE),
('hugo.leroy@artconnect.com', 'Hugo', 'Leroy', 'Bordeaux', 1985, 'Digital artist', '0600000004', 'www.hugoleroy.com', '@hugoleroy', TRUE),
('clara.dubois@artconnect.com', 'Clara', 'Dubois', 'Toulouse', 1993, 'Mixed media artist', '0600000005', 'www.claradubois.com', '@claradubois', TRUE);

-- Galeries de départ
INSERT INTO Gallery
(id_gallery, Name, adress_number, adress_street, adress_city, ownerName, OpeningHour, ClosingHour)
VALUES
(1, 'Galerie Lumiere', '12', 'Rue Victor Hugo', 'Paris', 'Jean Simon', '09:00:00', '18:00:00'),
(2, 'Galerie Horizon', '8', 'Avenue des Arts', 'Lyon', 'Sophie Martin', '10:00:00', '19:00:00'),
(3, 'Galerie Perspective', '22', 'Rue Centrale', 'Marseille', 'Paul Garnier', '09:30:00', '18:30:00'),
(4, 'Galerie Moderne', '5', 'Boulevard Sud', 'Bordeaux', 'Claire Petit', '11:00:00', '20:00:00'),
(5, 'Galerie Atelier', '17', 'Place du Centre', 'Toulouse', 'Marc Laurent', '10:00:00', '18:00:00');

-- Membres de départ
INSERT INTO CommunityMember
(email, name, surname, birthYear, phone, city, membershipType)
VALUES
('alice.morel@mail.com', 'Alice', 'Morel', 1999, '0611111111', 'Paris', 'premium'),
('benoit.dupont@mail.com', 'Benoit', 'Dupont', 1997, '0622222222', 'Lyon', 'free'),
('camille.roux@mail.com', 'Camille', 'Roux', 2000, '0633333333', 'Marseille', 'premium'),
('david.faure@mail.com', 'David', 'Faure', 1995, '0644444444', 'Bordeaux', 'free'),
('eva.noel@mail.com', 'Eva', 'Noel', 1998, '0655555555', 'Toulouse', 'premium');

-- Disciplines disponibles
INSERT INTO Discipline (name)
VALUES
('Painting'),
('Sculpture'),
('Photography'),
('Digital Art'),
('Mixed Media');

-- Tags disponibles
INSERT INTO ArtworkTag (Name)
VALUES
('colorful'),
('abstract'),
('portrait'),
('urban'),
('modern');

-- Œuvres de départ
INSERT INTO Artworks
(id_artworks, Title, creationYear, description, type, price, status, Email)
VALUES
(1, 'Blue Silence', 2020, 'Abstract blue composition', 'Painting', 1200.00, 'FOR_SALE', 'anna.martin@artconnect.com'),
(2, 'Stone Echo', 2019, 'Minimalist stone sculpture', 'Sculpture', 2400.00, 'EXHIBITED', 'luc.moreau@artconnect.com'),
(3, 'Night Street', 2021, 'Urban photography at night', 'Photography', 900.00, 'FOR_SALE', 'emma.bernard@artconnect.com'),
(4, 'Pixel Dream', 2022, 'Digital composition with neon colors', 'Digital Art', 1500.00, 'FOR_SALE', 'hugo.leroy@artconnect.com'),
(5, 'Fragments', 2023, 'Mixed media work with layered textures', 'Mixed Media', 1800.00, 'EXHIBITED', 'clara.dubois@artconnect.com');

-- Expositions de départ
INSERT INTO Exhibitions
(id_exhibitions, title, start_date, endDate, description, curatorName, theme, id_gallery)
VALUES
(1, 'Spring Colors', '2026-04-01', '2026-04-20', 'Exhibition around color and light', 'Julie Morel', 'Color', 1),
(2, 'Forms and Materials', '2026-04-10', '2026-05-01', 'Exhibition focused on sculpture and material', 'Nicolas Petit', 'Material', 2),
(3, 'Urban Vision', '2026-05-05', '2026-05-25', 'Photography and city life', 'Sarah Blanc', 'Urban', 3),
(4, 'Digital Horizons', '2026-05-15', '2026-06-05', 'Digital and immersive artworks', 'Thomas Leroy', 'Digital', 4),
(5, 'Textures and Layers', '2026-06-01', '2026-06-20', 'Mixed media and experimentation', 'Laura Simon', 'Texture', 5);

-- Workshops de départ
INSERT INTO workshops
(id_workshops, title, date_, durationMinutes, maxParticipants, description, price, level, Email)
VALUES
(1, 'Intro to Painting', '2026-04-03 14:00:00', 120, 15, 'Basic painting techniques', 45.00, 'Beginner', 'anna.martin@artconnect.com'),
(2, 'Sculpture Basics', '2026-04-12 10:00:00', 150, 10, 'Learn the basics of sculpture', 60.00, 'Beginner', 'luc.moreau@artconnect.com'),
(3, 'Street Photography', '2026-05-08 16:00:00', 90, 12, 'Learn to photograph urban scenes', 40.00, 'Intermediate', 'emma.bernard@artconnect.com'),
(4, 'Digital Creation', '2026-05-18 11:00:00', 120, 14, 'Introduction to digital art tools', 55.00, 'Intermediate', 'hugo.leroy@artconnect.com'),
(5, 'Mixed Media Lab', '2026-06-04 15:00:00', 180, 8, 'Experiment with mixed media', 70.00, 'Advanced', 'clara.dubois@artconnect.com');

-- Lien œuvres / expositions
INSERT INTO is_part_of (id_artworks, id_exhibitions)
VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- Réservations de départ
INSERT INTO booking (id_workshops, email, paymentStatus, bookingDate)
VALUES
(1, 'alice.morel@mail.com', 'PAID', '2026-03-25'),
(2, 'benoit.dupont@mail.com', 'PENDING', '2026-03-26'),
(3, 'camille.roux@mail.com', 'PAID', '2026-04-20'),
(4, 'david.faure@mail.com', 'PAID', '2026-04-28'),
(5, 'eva.noel@mail.com', 'PENDING', '2026-05-22');

-- Disciplines pratiquées par les artistes
INSERT INTO practice (Email, name)
VALUES
('anna.martin@artconnect.com', 'Painting'),
('luc.moreau@artconnect.com', 'Sculpture'),
('emma.bernard@artconnect.com', 'Photography'),
('hugo.leroy@artconnect.com', 'Digital Art'),
('clara.dubois@artconnect.com', 'Mixed Media');

-- Disciplines préférées des membres
INSERT INTO favoriteDisciplines (email, name)
VALUES
('alice.morel@mail.com', 'Painting'),
('benoit.dupont@mail.com', 'Sculpture'),
('camille.roux@mail.com', 'Photography'),
('david.faure@mail.com', 'Digital Art'),
('eva.noel@mail.com', 'Mixed Media');

-- Tags des œuvres
INSERT INTO tags (id_artworks, Name)
VALUES
(1, 'colorful'),
(2, 'modern'),
(3, 'urban'),
(4, 'abstract'),
(5, 'portrait');

-- Avis de départ
INSERT INTO Review (id_artworks, id_gallery, email, rating, comment, reviewDate)
VALUES
(1, 1, 'alice.morel@mail.com', 5, 'Very beautiful work and great gallery', '2026-04-05'),
(2, 2, 'benoit.dupont@mail.com', 4, 'Interesting sculpture and nice space', '2026-04-15'),
(3, 3, 'camille.roux@mail.com', 5, 'Excellent photo and atmosphere', '2026-05-10'),
(4, 4, 'david.faure@mail.com', 4, 'Creative digital piece', '2026-05-20'),
(5, 5, 'eva.noel@mail.com', 5, 'Original work and very good exhibition', '2026-06-10');


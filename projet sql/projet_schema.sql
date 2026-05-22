

CREATE DATABASE db_art_connect;

USE db_art_connect;

CREATE TABLE Artist(
   Email VARCHAR(50),
   Name VARCHAR(50),
   surname VARCHAR(50),
   City VARCHAR(50),
   Birth_Year INT,
   bio TEXT,
   phone VARCHAR(50),
   website VARCHAR(50),
   socialMedia VARCHAR(50),
   isActive BOOLEAN,
   PRIMARY KEY(Email)
);

CREATE TABLE Artworks(
   id_artworks INT,
   Title VARCHAR(50),
   creationYear INT,
   description TEXT,
   type VARCHAR(50),
   price DECIMAL(15,2),
   status VARCHAR(50),
   Email VARCHAR(50) NOT NULL,
   PRIMARY KEY(id_artworks),
   FOREIGN KEY(Email) REFERENCES Artist(Email)
);

CREATE TABLE Gallery(
   id_gallery INT,
   Name VARCHAR(50),
   adress_number VARCHAR(50),
   adress_street VARCHAR(50),
   adress_city VARCHAR(50),
   ownerName VARCHAR(50),
   OpeningHour TIME,
   ClosingHour TIME,
   PRIMARY KEY(id_gallery)
);

CREATE TABLE Exhibitions(
   id_exhibitions INT,
   title VARCHAR(50),
   start_date DATE,
   endDate DATE,
   description TEXT,
   curatorName VARCHAR(50),
   theme VARCHAR(50),
   id_gallery INT NOT NULL,
   PRIMARY KEY(id_exhibitions),
   FOREIGN KEY(id_gallery) REFERENCES Gallery(id_gallery)
);

CREATE TABLE workshops(
   id_workshops INT,
   title VARCHAR(50),
   date_ DATETIME,
   durationMinutes INT,
   maxParticipants INT,
   description TEXT,
   price DECIMAL(15,2),
   level VARCHAR(50),
   Email VARCHAR(50) NOT NULL,
   PRIMARY KEY(id_workshops),
   FOREIGN KEY(Email) REFERENCES Artist(Email)
);

CREATE TABLE CommunityMember(
   email VARCHAR(50),
   name VARCHAR(50),
   surname VARCHAR(50),
   birthYear INT,
   phone VARCHAR(50),
   city VARCHAR(50),
   membershipType VARCHAR(50),
   PRIMARY KEY(email)
);

CREATE TABLE Discipline(
   name VARCHAR(50),
   PRIMARY KEY(name)
);

CREATE TABLE ArtworkTag(
   Name VARCHAR(50),
   PRIMARY KEY(Name)
);

CREATE TABLE is_part_of(
   id_artworks INT,
   id_exhibitions INT,
   PRIMARY KEY(id_artworks, id_exhibitions),
   FOREIGN KEY(id_artworks) REFERENCES Artworks(id_artworks),
   FOREIGN KEY(id_exhibitions) REFERENCES Exhibitions(id_exhibitions)
);

CREATE TABLE booking(
   id_workshops INT,
   email VARCHAR(50),
   paymentStatus VARCHAR(50),
   bookingDate DATE,
   PRIMARY KEY(id_workshops, email),
   FOREIGN KEY(id_workshops) REFERENCES workshops(id_workshops),
   FOREIGN KEY(email) REFERENCES CommunityMember(email)
);

CREATE TABLE practice(
   Email VARCHAR(50),
   name VARCHAR(50),
   PRIMARY KEY(Email, name),
   FOREIGN KEY(Email) REFERENCES Artist(Email),
   FOREIGN KEY(name) REFERENCES Discipline(name)
);

CREATE TABLE favoriteDisciplines(
   email VARCHAR(50),
   name VARCHAR(50),
   PRIMARY KEY(email, name),
   FOREIGN KEY(email) REFERENCES CommunityMember(email),
   FOREIGN KEY(name) REFERENCES Discipline(name)
);

CREATE TABLE tags(
   id_artworks INT,
   Name VARCHAR(50),
   PRIMARY KEY(id_artworks, Name),
   FOREIGN KEY(id_artworks) REFERENCES Artworks(id_artworks),
   FOREIGN KEY(Name) REFERENCES ArtworkTag(Name)
);

CREATE TABLE Review(
   id_artworks INT,
   id_gallery INT,
   email VARCHAR(50),
   rating INT,
   comment TEXT,
   reviewDate DATE,
   PRIMARY KEY(id_artworks, id_gallery, email),
   FOREIGN KEY(id_artworks) REFERENCES Artworks(id_artworks),
   FOREIGN KEY(id_gallery) REFERENCES Gallery(id_gallery),
   FOREIGN KEY(email) REFERENCES CommunityMember(email)
);
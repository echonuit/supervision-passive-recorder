CREATE TABLE Utilisateur(
                            idUtilisateur INT,
                            nomUtilisateur VARCHAR(100) NOT NULL,
                            nomChercheur VARCHAR(50),
                            prenomChercheur VARCHAR(50),
                            PRIMARY KEY(idUtilisateur)
);

CREATE TABLE Groupe(
                       idGroupe INT AUTO_INCREMENT,
                       nomGroupe VARCHAR(100),
                       idGroupe_1 INT,
                       PRIMARY KEY(idGroupe),
                       FOREIGN KEY(idGroupe_1) REFERENCES Groupe(idGroupe)
);

CREATE TABLE Capteur(
                        idCapteur INT AUTO_INCREMENT,
                        devEUI VARCHAR(16) NOT NULL,
                        nomCapteur VARCHAR(100) NOT NULL,
                        pourcentageBatterie TINYINT UNSIGNED,
                        pourcentageStockage TINYINT UNSIGNED,
                        longitude DECIMAL(11,8),
                        latitude DECIMAL(11,8),
                        idGroupe INT,
                        PRIMARY KEY(idCapteur),
                        UNIQUE(devEUI),
                        FOREIGN KEY(idGroupe) REFERENCES Groupe(idGroupe)
);

CREATE TABLE Historique(
                           idHistorique BIGINT AUTO_INCREMENT,
                           idCapteur INT NOT NULL,
                           dateHeure DATETIME NOT NULL,
                           pourcentageBatterie TINYINT UNSIGNED,
                           pourcentageStockage TINYINT UNSIGNED,
                           longitude DECIMAL(11,8),
                           latitude DECIMAL(11,8),
                           PRIMARY KEY(idHistorique, idCapteur),
                           FOREIGN KEY(idCapteur) REFERENCES Capteur(idCapteur)
);

CREATE TABLE est_lie_a(
                          idCapteur INT,
                          idUtilisateur INT,
                          PRIMARY KEY(idCapteur, idUtilisateur),
                          FOREIGN KEY(idCapteur) REFERENCES Capteur(idCapteur),
                          FOREIGN KEY(idUtilisateur) REFERENCES Utilisateur(idUtilisateur)
);

-- ////////// JEUX DE DONNÉES ////////// --

-- Allow explicit 0 in AUTO_INCREMENT columns (needed for the "-0" rows)
SET @old_sql_mode = @@SQL_MODE;
SET SQL_MODE = CONCAT_WS(',', @@SQL_MODE, 'NO_AUTO_VALUE_ON_ZERO');

-- Utilisateur
INSERT INTO Utilisateur (idUtilisateur, nomUtilisateur, nomChercheur, prenomChercheur) VALUES
    (1, 'normaline',   'dumont',   'aline'),
    (2, 'daltoline',   'dunoir',   'alinevoit'),
    (3, 'sourdaline',  'nentend',  'aline'),
    (4, 'aveuglaline', 'voitmal',  'aline'),
    (0, 'jamal',       'jamal',    'jamal');

-- Groupe (parents first, because of the self-referencing FK)
INSERT INTO Groupe (idGroupe, nomGroupe, idGroupe_1) VALUES
    (1, 'branche_batman_contingency', NULL),
    (0, 'jamal',                      NULL);

INSERT INTO Groupe (idGroupe, nomGroupe, idGroupe_1) VALUES
    (2, 'capteurs_batman_decimator',  1),
    (3, 'kiosque_kebab_letchup',      1);

-- Capteur
INSERT INTO Capteur (idCapteur, devEUI, nomCapteur, pourcentageBatterie, pourcentageStockage, longitude, latitude, idGroupe) VALUES
    (1, 'AMVIUS', 'batman_exterminator',   69, 67, 3.14159265,  1.23456789, 2),
    (2, 'JUNIOR', 'batman_decimator',       9, 11, 2.71828182, 42.42424242, 2),
    (3, 'IS',     'batman_atomizer',        7, 11, 1.72900000,  6.66000000, 2),
    (4, 'DEAD',   'batman_desintegrator',   4, 20, 8.88888880,  0.00000000, 2),
    (0, 'JAMAL',  'jamal',                  1,  1, 1.00000000,  1.00000000, 0);
-- est_lie_a
INSERT INTO est_lie_a (idCapteur, idUtilisateur) VALUES
    (1, 4),
    (2, 4),
    (3, 3),
    (4, 2),
    (0, 0);

-- Historique (idCapteur, idHistorique, ...)
INSERT INTO Historique (idCapteur, idHistorique, dateHeure, pourcentageBatterie, pourcentageStockage, longitude, latitude) VALUES
    (1, 1, '2026-11-05 12:00:01', 1, 2, 3.14159265,  1.23456789),
    (1, 2, '2026-11-01 15:09:00', 3, 4, 2.71828182, 42.42424242),
    (2, 2, '2026-11-02 15:08:24', 5, 6, 2.71828182, 42.42424242),
    (3, 2, '2026-11-03 13:54:56', 7, 8, 2.71828182, 42.42424243),
    (1, 3, '2026-10-07 02:01:07', 9, 1, 1.72900000,  6.66000000),
    (2, 3, '2026-10-09 14:47:22', 2, 3, 1.72900000,  6.66000000),
    (0, 0, '1999-09-10 03:00:00', 1, 1, 1.00000000,  1.00000000);

-- Restore the original SQL mode
SET SQL_MODE = @old_sql_mode;
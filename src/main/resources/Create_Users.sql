/*
 * DO_NOT_DELETE_COMMENTS
 */

USE wizualizacja2;/**/
CREATE TABLE KONRAD_UZYTKOWNICY(
    Id_Uzytkownika int IDENTITY(1,1) PRIMARY KEY,
    Login varchar(40) UNIQUE,
    Haslo varchar(255),
    Czy_Admin int DEFAULT 0
);
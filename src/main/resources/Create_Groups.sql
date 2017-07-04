/*
 * DO_NOT_DELETE_COMMENTS
 */

USE wizualizacja2;/**/
CREATE TABLE KONRAD_GRUPY(
    Id_Grupy int IDENTITY(1,1) PRIMARY KEY,
    Nazwa varchar(50) NOT NULL UNIQUE,
);
USE wizualizacja2;/**/
CREATE TABLE KONRAD_BRAMKI(
    Id_Grupy int NOT NULL REFERENCES KONRAD_GRUPY(Id_Grupy) ON DELETE CASCADE,
    GateId int NOT NULL,
    PRIMARY KEY (Id_Grupy,GateId)
);
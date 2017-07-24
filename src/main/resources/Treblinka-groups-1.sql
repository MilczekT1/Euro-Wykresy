/*
 * DO_NOT_DELETE_COMMENTS
 */

USE wizualizacja;/**/
CREATE TABLE EW_Grupy(
    Id_Grupy int IDENTITY(1,1) PRIMARY KEY,
    Nazwa varchar(50) NOT NULL UNIQUE,
);
USE wizualizacja;/**/
CREATE TABLE EW_Bramki(
    Id_Grupy int NOT NULL REFERENCES EW_Grupy(Id_Grupy) ON DELETE CASCADE,
    GateId int NOT NULL,
    PRIMARY KEY (Id_Grupy,GateId)
);
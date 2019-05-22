DROP DATABASE IF EXISTS TermoMoBoy;
CREATE DATABASE TermoMoBoy;
DROP USER IF EXISTS TermoAdmin;
CREATE USER TermoAdmin IDENTIFIED BY 'TermoAdmin';
GRANT ALL PRIVILEGES ON TermoMoBoy.* TO TermoAdmin WITH GRANT OPTION;
USE TermoMoBoy;



CREATE TABLE Dia (
	dia DATE PRIMARY KEY
);

CREATE TABLE Datos(
	dia DATE,
	hora VARCHAR(5), 
    humedad FLOAT(6,4),
    temperatura FLOAT(6,4),
    presionAt FLOAT(6,3),
    mm3Lluvia FLOAT(6,4),
    kmHViento FLOAT(6,4),
    #FOREIGN KEY A HORA
	FOREIGN KEY (dia)
	REFERENCES Dia(dia),
	CONSTRAINT dATOS_pk PRIMARY KEY (dia, hora)
);

CREATE TABLE Transporte (
	dia DATE PRIMARY KEY,
	coche INT,
    a_pie INT,
    t_pub INT,
    bici INT,
    otros INT,
	FOREIGN KEY (dia)
	REFERENCES Dia(dia)
);






SELECT * FROM Dia;
Select * FROM Datos;
DELETE From Datos;




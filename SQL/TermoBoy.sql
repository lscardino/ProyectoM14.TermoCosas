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
	hora TIME(5), 
    humedad FLOAT(4,2),
    temperatura FLOAT(4,2),
    presionAt FLOAT(4,2),
    mm3Lluvia FLOAT(4,2),
    kmHViento FLOAT(4,2),
    #FOREIGN KEY A HORA
	FOREIGN KEY (dia)
	REFERENCES Dia(dia),
	CONSTRAINT dATOS_pk PRIMARY KEY (dia, hora)
);
#
#CREATE TABLE Hora(
#	hora TIME,
#    dia DATE,
#    PRIMARY KEY (hora, dia)
#);



CREATE TABLE transporte (
	dia DATE PRIMARY KEY,
	coche INT,
    a_pie INT,
    t_pub INT,
    bici INT,
    otros INT,
	FOREIGN KEY (dia)
	REFERENCES Dia(dia)
);

CREATE TABLE ejemplo(
	ejemplo VARCHAR(30)
);

INSERT INTO ejemplo VALUES('wwwwww');
INSERT INTO ejemplo VALUES('eeeeee');
INSERT INTO ejemplo VALUES('eeee');

#INSERT INTO Dia Values(TO_DATE('17/12/2005','DD/MM/YYYY'));


#INSERT INTO Datos (humedad,temperatura,presionAt,mm3Lluvia,kmHViento) VALUES(80.3,21.3,0,2.21,6);
#INSERT INTO Datos (humedad,temperatura,presionAt,mm3Lluvia,kmHViento) VALUES(60.0,25.1,3,0.0,2);


SELECT * FROM transporte;




DROP DATABASE IF EXISTS TermoMoBoy;
CREATE DATABASE TermoMoBoy;

DROP USER IF EXISTS TermoAdmin;
CREATE USER TermoAdmin IDENTIFIED BY 'TermoAdmin';
GRANT ALL PRIVILEGES ON TermoMoBoy.* TO TermoAdmin WITH GRANT OPTION;

DROP USER IF EXISTS TermoGraf;
CREATE USER TermoGraf IDENTIFIED BY 'GrafPass';
GRANT SELECT ON TermoMoBoy.* TO 'TermoGraf'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE TermoMoBoy;

DROP TABLE IF EXISTS Transporte;
CREATE TABLE Transporte (
	fk_como CHAR(30)
);
ALTER TABLE Transporte ADD CONSTRAINT pk_vehiculo PRIMARY KEY (fk_como);

DROP TABLE IF EXISTS Dia;
CREATE TABLE Dia (
	fk_dia DATE
);
ALTER TABLE Dia ADD CONSTRAINT pk_dia PRIMARY KEY (fk_dia);

DROP TABLE IF EXISTS Usuario;
CREATE TABLE Usuario(
fk_codigo CHAR(35),
edad INT,
genero CHAR(15)
);
ALTER TABLE Usuario ADD CONSTRAINT pk_usuario PRIMARY KEY (fk_codigo);

	DROP TABLE IF EXISTS Datos;
	CREATE TABLE Datos (
		fk_dia DATE,
		fk_hora CHAR(5),
		humedad FLOAT,
		temperatura FLOAT,
		presionAt FLOAT,
		sensacionT FLOAT,
		polvo FLOAT,
		mm3Lluvia FLOAT,
		kmHViento FLOAT,
		lumens FLOAT
	);
	ALTER TABLE Datos ADD CONSTRAINT pk_datos PRIMARY KEY (fk_hora, fk_dia);
	ALTER TABLE Datos ADD FOREIGN KEY (fk_dia) REFERENCES Dia(fk_dia) ON DELETE CASCADE;

	DROP TABLE IF EXISTS Usar;
	CREATE TABLE Usar(
	fk_usuario CHAR(35),
	fk_transporte CHAR(30),
	fk_dia DATE
	);
	ALTER TABLE Usar ADD CONSTRAINT pk_usar PRIMARY KEY (fk_dia, fk_usuario);
	ALTER TABLE Usar ADD FOREIGN KEY (fk_usuario) REFERENCES Usuario(fk_codigo) ON DELETE CASCADE;
	ALTER TABLE Usar ADD FOREIGN KEY (fk_transporte) REFERENCES Transporte(fk_como) ON DELETE CASCADE;
	ALTER TABLE Usar ADD FOREIGN KEY (fk_dia) REFERENCES Dia(fk_dia) ON DELETE CASCADE;

/*
    ON DELETE CASCADE
    [ ON UPDATE { NO ACTION | CASCADE | SET NULL | SET DEFAULT } ] 
*/
insert into Transporte values("Apie");
insert into Transporte values("Coche");
insert into Dia values("2019-05-28");
insert into Dia values("2019-05-29");
insert into Dia values("2019-05-30");
insert into Usuario values("Xin",20,"H");
insert into Datos values("2019-05-28","20:00", 0,0,0,0,0,0,0,0);
insert into Datos values("2019-05-28","21:00", 0,0,0,0,0,0,0,0);
insert into Usar Values("Xin","Apie","2019-05-28");
insert into Usar Values("Xin","Coche","2019-05-29");

select * from Usar ;
select * from Usuario;

#Buscar cuantas mujeres, hombres y otros.
select 
	(select count(usu.fk_codigo) from Usuario usu where usu.genero = 'Mujer') as 'Mujeres',
	(select count(usu.fk_codigo) from Usuario usu where usu.genero = 'Hombre') as 'Hombres',
	(select count(usu.fk_codigo) from Usuario usu where usu.genero = 'Otro') as 'Otros',
	(select count(usu.fk_codigo) from Usuario usu where usu.genero is null) as 'Anónimos'
;

#Buscar los transportes del general DATOS


#Sequencia de busqueda por dia de GENERO
select 
	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from Usar usar where usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero = 'Mujer') as 'Mujeres' ,

	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from usar where usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero = 'Hombre') as 'Hombres',
    
	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from Usar usar  where  usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero = 'Otro') as 'Otros',
    
	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from Usar usar where  usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero is null) as 'Anónimos'
;

#Algo mas simple y relaciona tabla GENERO
	(select count(usu.fk_codigo) from Usuario usu , Usar usar where usu.fk_codigo = usar.fk_usuario AND usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" 
	AND usu.genero is null);
/**/
select 
	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from Usar usar where usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero = 'Mujer') as 'Mujeres' ,

	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from usar where usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero = 'Hombre') as 'Hombres',
    
	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from Usar usar  where  usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero = 'Otro') as 'Otros',
    
	(select count(usu.fk_codigo) from Usuario usu where usu.fk_codigo in (
	select usar.fk_usuario from Usar usar where  usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" )
	AND usu.genero is null) as 'Anónimos'
    
;


#Selencia por dia de movimiento DATOS

select usar.fk_transporte, count(usar.fk_transporte) from Usar usar where usar.fk_dia >= "2019-05-28" AND usar.fk_dia <= "2019-05-31" group by usar.fk_transporte;


select * from Dia where Dia.fk_dia order by Dia.fk_dia;
select * from Usuario  ;

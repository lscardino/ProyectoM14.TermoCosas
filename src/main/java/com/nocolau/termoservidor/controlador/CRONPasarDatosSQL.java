/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor.controlador;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import static com.nocolau.termoservidor.controlador.CRONTotalTransporte.conn;
import static com.nocolau.termoservidor.controlador.CRONTotalTransporte.database;
import static com.nocolau.termoservidor.controlador.CRONTotalTransporte.latch;
import static com.nocolau.termoservidor.controlador.EjecutarFireBase.database;
import com.nocolau.termoservidor.modelo.Connexion;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lorenzo
 */
public class CRONPasarDatosSQL {

    static FirebaseDatabase database;
    static DatabaseReference ref;
    static CountDownLatch latch;
    static Connection conn;
    static Query query;

    static String dia;

    static SimpleDateFormat parser;

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, InterruptedException, ParseException {
        FileInputStream serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Dia");

        query = ref.orderByKey().limitToFirst(2);
        conn = Connexion.getConnection();

        parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        //fechaRepetida("hola", "pepsicola");
        leerTransportes();
        //enviarSQL();
    }

    public static void leerTransportes() throws InterruptedException {
        latch = new CountDownLatch(1);

        System.out.println("Entra metodo");
        //DatabaseReference referenciaComp = ref.child("contador");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                int cantidadDatos = (int) ds.getChildrenCount();
                System.out.println("Cantidad datos - " + ds.getChildrenCount());

                //Hay que saber cuantos hay 
                //o simplemenete deja dos y ya - aunqeu en verdad solo usas uno
                if (cantidadDatos > 1) {
                    query = ref.orderByKey().limitToLast(1);
                } else {
                    System.out.println("ERROR: No hay datos que concuerden con la busqueda especificada.");
                }

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot ds) {
                        for (DataSnapshot entrada : ds.getChildren()) {
                            String diaEntrada = entrada.getKey();
                            System.out.println("\n//////////////\n Dia: " + diaEntrada + "\n/////////////////////\n");
                            for (DataSnapshot datos : entrada.getChildren()) {

                                String horaEntrada = datos.getKey();

                                try {
                                    if (!fechaRepetida("Holaaa", horaEntrada)) {

                                        String humedadEntrada = datos.child("Humedad").getValue().toString().substring(0, 5);
                                        String temperaturaEntrada = datos.child("Temperatura").getValue().toString().substring(0, 5);
                                        String presionEntrada = datos.child("Presión").getValue().toString().substring(0, 5);
                                        //Cambiar velocidad viento, ahora está si porque en Firebase es seimpre un 0;
                                        String velVientoEntrada = datos.child("Velocidad viento").getValue().toString();

                                        System.out.println("Hora: " + horaEntrada);
                                        System.out.println("Humedad " + humedadEntrada);
                                        System.out.println("-------------------\n");
                                        
                                        guardarSQL("Wolass", horaEntrada, humedadEntrada, temperaturaEntrada, presionEntrada, "jjaano", "otraAqki");

                                    }
                                } catch (ParseException | SQLException ex) {
                                    Logger.getLogger(CRONPasarDatosSQL.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                            //System.out.println("Valor - " +entrada.getValue(String.class));
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError de) {
                        System.out.println("ERROR: No hay datos que concuerden con la busqueda especificada.");
                        latch.countDown();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError de) {
                System.out.println("ERROR: No hay datos que concuerden con la busqueda especificada.");
                latch.countDown();
            }
        });
        latch.await();
    }

    public static boolean fechaRepetida(String fecha, String hora) throws ParseException, SQLException {
        boolean repetido = false;

        //De momento no podemos parsear porque la fecha está puesta en dd-MM-yyyy
        //Y en el firebase es dd-MM
        /*
        Date fechaParseada = parser.parse(fecha);
        System.out.println("Fecha parseade: " + fecha);
         */
        //Asi que usamso esto de prueba
        Date hoy = new Date();
        System.out.println("Fecha que estamos buscando " + hoy);
        String hoyparse = parser.format(hoy);

        System.out.println("Hoy- " + hoy);

        //Solo hace falta mirar aqui, ya que en dato solo se insertan cosas tras estra comprobacion.
        String queryFecha = "select count(*) from Dia d where d.dia=(?)";
        PreparedStatement sentenciaP = conn.prepareStatement(queryFecha);
        sentenciaP.setObject(1, hoyparse);

        try (ResultSet rs = sentenciaP.executeQuery()) {
            if (rs.next()) {
                int resultado = rs.getInt(1);
                System.out.println("Cantidad " + resultado);
                //El día existe, miramos si existen las horas.
                if (resultado > 0) {
                    repetido = horaRepetida(hoyparse, hora);
                    System.out.println("Repetido el dia y la hora");

                } else {
                    System.out.println("No existe, lo insertamos");
                    //No existe, lo creamos
                    String insertarFecha = "INSERT INTO Dia VALUES(?)";
                    sentenciaP = conn.prepareStatement(insertarFecha);
                    sentenciaP.setObject(1, hoyparse);
                    sentenciaP.execute();

                }

            } else {
                System.out.println("Errror");
            }
        }
        return repetido;
    }

    public static boolean horaRepetida(String dia, String hora) throws SQLException {

        boolean repetido = false;

        //Otra vez, ponesmo nuesto dia perosnalizado
        String queryFecha = "select count(*) from Datos d where d.dia=(?) and d.hora =(?)";
        PreparedStatement sentenciaP = conn.prepareStatement(queryFecha);
        sentenciaP.setObject(1, dia);
        sentenciaP.setString(2, hora);

        ResultSet rs = sentenciaP.executeQuery();
        if (rs.next()) {
            int resultado = rs.getInt(1);

            if (resultado > 0) {
                System.out.println("Existe, no creamos nada  y de hecho salimos");
                repetido = true;
            } else {
                /*
                System.out.println("No existe, lo creamos");
                String insertarEnDatos = "INSERT INTO Datos(dia,hora) VALUES (?,?)";
                sentenciaP = conn.prepareStatement(insertarEnDatos);
                sentenciaP.setObject(1, dia);
                sentenciaP.setString(2, hora);
                sentenciaP.execute();

                System.out.println("Hora insertada: " + hora);
                */
                repetido = false;

            }

        } else {
            System.out.println("Ha habido un error tocho!");
        }

        return repetido;
    }

    public static void guardarSQL(String dia, String hora, String humedad,
            String temperatura, String presion, String mmlluvia,
            String kmhViento) throws SQLException, ParseException {

        /*
        String input = "20-05-2019";
        Date hoy = new Date();
        LocalDate localHoy = LocalDate.now();
        Date hoycomparar = new Date();
        SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        System.out.println("");
        String hoyparse = parser.format(hoycomparar);
        if (input.equals(hoyparse)) {
            System.out.println("Coinciden");
        } else {
            System.out.println("No coinciden");
        }
        Date parseada = parser.parse(input);
        System.out.println("Parsead:  " + parseada);

        Time tiempo = new Time(010101);
         System.out.println("Tiempo: " + tiempo);
         System.out.println("Hora parse " + hora);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = formatter.format(hora);
         System.out.println("Formateada: " + hora);
        
         */
        Date hoy = new Date();
        System.out.println("Fecha que vamos a insertar " + hoy);
        String hoyparse = parser.format(hoy);

        String insertarlosDatos = "INSERT INTO Datos VALUES (?,?,?,?,?,?,?)";
        PreparedStatement sentenciaP = conn.prepareStatement(insertarlosDatos);
        sentenciaP.setObject(1, hoyparse);
        sentenciaP.setString(2, hora);
        sentenciaP.setFloat(3, Float.parseFloat(humedad));
        sentenciaP.setFloat(4, Float.parseFloat(temperatura));
        sentenciaP.setFloat(5, Float.parseFloat(presion));
        sentenciaP.setFloat(6, (float) 1.33);
        sentenciaP.setFloat(7, (float) 99.33);

        sentenciaP.execute();

        System.out.println("Escrito Todo guay");

    }
}

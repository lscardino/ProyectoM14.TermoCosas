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
import com.google.firebase.database.ValueEventListener;
import com.nocolau.termoservidor.modelo.Connexion;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Lorenzo
 *
 * Vale, básicamente, al final del dia nos interesa saber que medio de
 * transporte han usado los alumnos para ir al instituto.
 *
 * Para coseguir esto, programaremos esta clase para ser ejecutado a las 23.55
 * de la noche. Lo que hace la clase es leer las entradas bajo "Transporte" de
 * la BD de Firebase , convierte al información y la guarda en la BD local SQL.
 *
 */
public class CRONTotalTransporte {

    static FirebaseDatabase database;
    static DatabaseReference ref;
    static CountDownLatch latch;
    static Connection conn;

    static String dia;
    //TRANSPORTES
    static long totalEntradas;
    static int bici;
    static int coche;
    static int tPublico;
    static int apie;
    static int otros;

    static Date queDiaEsHoy;

    //De este map se puede estraer mucha más información.
    static HashMap<String, String> listaTotal;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, SQLException {
        FileInputStream serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance();
        conn = Connexion.getConnection();

        dia = formatearFecha();
        System.out.println("Dia: " + dia);
        //queDiaEsHoy = new Date();
        //queDiaEsHoy = 
        fechaExiste();
        if (!fechaRepetida()) {

            //Hay que ver una forma de decirle que dia leer - es facil.
            ref = database.getReference("Dia/" + dia + "/Transporte");

            bici = 0;
            coche = 0;
            tPublico = 0;
            apie = 0;
            otros = 0;

            leerTransportes();
            //Sobra este await -> Ya esta dentro del metodo al final
            latch.await();
            transformarParaSQL();
            //enviarSQL();
        } else {
            System.out.println("Ya hay datos recogidos en esa fecha.");
        }

    }

    public static String formatearFecha() {
        DateFormat diaActual = new SimpleDateFormat("yyyy-MM-dd");
        return diaActual.format(new Date());
    }

    public static void leerTransportes() throws InterruptedException {
        latch = new CountDownLatch(1);
        listaTotal = new HashMap<>();
        System.out.println("Entra metodo");
        //DatabaseReference referenciaComp = ref.child("contador");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                ;
                totalEntradas = ds.getChildrenCount();
                for (DataSnapshot entrada : ds.getChildren()) {
                    String usuario = entrada.getKey();
                    String transporte = entrada.getValue(String.class);
                    listaTotal.put(usuario, transporte);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError de) {
                System.out.println("Lectura de datos cancelada");
                latch.countDown();
            }
        });
        //latch.await();
    }

    public static void transformarParaSQL() throws SQLException {
        for (Map.Entry pair : listaTotal.entrySet()) {
            switch ((String) pair.getValue()) {
                case "Coche":
                    coche++;
                    break;
                case "Bici":
                    bici++;
                    break;
                case "Apie":
                    apie++;
                    break;
                case "Tpublico":
                    tPublico++;
                    break;
                default:
                    otros++;
                    break;
            }
        }
        enviarSQL();
    }

    public static void iniciarDiaSQL() throws SQLException {
        String query = "insert into Dia(dia)"
                + " values (?)";
        PreparedStatement sentenciaP = conn.prepareStatement(query);
        sentenciaP.setObject(1, queDiaEsHoy);

        sentenciaP.execute();
    }

    public static void enviarSQL() throws SQLException {
        String query = "insert into transporte"
                + " values (?,?,?,?,?,?)";
        PreparedStatement sentenciaP = conn.prepareStatement(query);
        sentenciaP.setObject(1, dia);
        sentenciaP.setInt(2, coche);
        sentenciaP.setInt(3, apie);
        sentenciaP.setInt(4, tPublico);
        sentenciaP.setInt(5, bici);
        sentenciaP.setInt(6, otros);

        sentenciaP.execute();

        System.out.println("Escrito: ");
        System.out.println("Dia " + dia);
        System.out.println("Nº gente coche: " + coche);
        System.out.println("Nº gente bici: " + bici);
        System.out.println("Nº gente transportes pub: " + tPublico);
        System.out.println("Nº gente a pie: " + apie);
        System.out.println("Nº gente otros: " + otros);
    }

    public static void fechaExiste() throws SQLException {
        System.out.println("Entro a chequear fecha");
        String query = "select count(*) as resultado from Dia d where d.dia=(?) ";
        PreparedStatement sentenciaP = conn.prepareStatement(query);
        sentenciaP.setObject(1, dia);

        ResultSet rs = sentenciaP.executeQuery();
        if (rs.next()) {
            int resultado = rs.getInt(1);
            System.out.println("Cantidad " + resultado);
            if (resultado == 0) {
                //No existe el dia, lo creamos
                iniciarDiaSQL();
            }

        } else {
            System.out.println("Errror");
        }
        rs.close();

    }

    public static boolean fechaRepetida() throws SQLException {
        boolean repetido = false;
        System.out.println("Entro a lo de repetido");
        String query = "select count(*) as resultado from Transporte d where d.dia=(?) ";
        PreparedStatement sentenciaP = conn.prepareStatement(query);
        sentenciaP.setObject(1, dia);

        ResultSet rs = sentenciaP.executeQuery();
        if (rs.next()) {
            int resultado = rs.getInt(1);
            System.out.println("Cantidad " + resultado);
            if (resultado > 0) {
                //Ya hay datos en transporte con este dia
                //No existe el dia, lo creamos
                //iniciarDiaSQL();
                repetido = true;
            }

        } else {
            System.out.println("Errror");
        }
        rs.close();
        return repetido;

    }
}

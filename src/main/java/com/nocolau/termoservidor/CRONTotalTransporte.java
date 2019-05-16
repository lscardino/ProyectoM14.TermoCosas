/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    static String dia;
    //TRANSPORTES
    static long totalEntradas;
    static int bici;
    static int coche;
    static int tPublico;
    static int apie;
    static int otros;

    //De este map se puede estraer mucha más información.
    static HashMap<String, String> listaTotal;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        FileInputStream serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance();

        dia = formatearFecha();
        System.out.println("Dia: " + dia);
        //Hay que ver una forma de decirle que dia leer - es facil.
        ref = database.getReference("Dia/" + dia +"/Transporte");

        bici = 0;
        coche = 0;
        tPublico = 0;
        apie = 0;
        otros = 0;

        leerTransportes();
        //Sobra este await -> Ya esta dentro del metodo al final
        latch.await();
        transformarParaSQL();
        enviarSQL();

    }

    public static String formatearFecha() {
        DateFormat diaActual = new SimpleDateFormat("MM-dd");
        return diaActual.format(new Date());
    }

    public static void leerTransportes() throws InterruptedException {
        latch = new CountDownLatch(1);
        listaTotal = new HashMap<>();
        System.out.println("Entra metodo");
        //DatabaseReference referenciaComp = ref.child("contador");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
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
        latch.await();
    }

    public static void transformarParaSQL() {
        Iterator it = listaTotal.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

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
    }

    public static void enviarSQL() {
        //deberia enviar, pero de momento solo muestra
        System.out.println("Nº gente coche: " + coche);
        System.out.println("Nº gente bici: " + bici);
        System.out.println("Nº gente transportes pub: " + tPublico);
        System.out.println("Nº gente a pie: " + apie);
        System.out.println("Nº gente otros: " + otros);
    }
}

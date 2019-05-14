/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cho_S
 */
public class ProyectoArduino {

    /**
     * @param args the command line arguments
     */
    static int puerto = 20003;
    static long TIEMPO_BUCLE = 30000;
    
    private final Date diaInicio = new Date();
  
    private static FileInputStream serviceAccount;
    private static FirebaseOptions options;
    private static FirebaseDatabase database;
    private static DatabaseReference ref;
    private static DateFormat dateFormat;
    private static DateFormat horaFormat;

    ProyectoArduino() throws FileNotFoundException, IOException {

        serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        dateFormat = new SimpleDateFormat("MM-dd");
        horaFormat = new SimpleDateFormat("HH:mm");

        //SERVIDOR
        try {
            ServerSocket ssk = new ServerSocket(puerto);
            System.out.println(ssk.getLocalSocketAddress().toString());
            System.out.println(ssk.getInetAddress().getLocalHost());

            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("-  -  -  -  -  -  -  -  -  -  -  -  -");
                Socket sk = ssk.accept(); //espera una conexión de un cliente
                Servidor servidor = new Servidor(sk);
                new Thread(servidor).start();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    class Servidor extends Thread {

        Socket sk;
        DataInputStream dis;
        DataOutputStream dos;

        Servidor(Socket sk) {
            this.sk = sk;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sk.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sk.getOutputStream()));

                System.out.println("Conectado " + sk.getInetAddress().getHostAddress());
                //Lee un caracter en formato byte
                do {
                    try {
                        if (in.readLine().equals("1111")) {

                            out.write(TIEMPO_BUCLE + "");
                            out.flush();
                        } else {

                            float temp = Float.parseFloat(in.readLine());
                            float humedad = Float.parseFloat(in.readLine());
                            float presion = Float.parseFloat(in.readLine());
                            float tempDHT22 = Float.parseFloat(in.readLine());
                            float humedadDHT22 = Float.parseFloat(in.readLine());
                            float velViento = Float.parseFloat(in.readLine());
                            /*
                            String temp = in.readLine() + "ºC";
                            String humedad = in.readLine() + "%";
                            String presion = in.readLine() + "Pa";
                            String tempDHT22 = in.readLine() + "ºC";
                            String humedadDHT22 = in.readLine() + "%";
                            String velViento = in.readLine() + "ms/rad";*/

                            System.out.println("Temperatura  " + temp);
                            System.out.println("Humedad  " + humedad);
                            System.out.println("Presión  " + presion);
                            System.out.println("Temperatura DHT22  " + tempDHT22);
                            System.out.println("Humedad DHT22  " + humedadDHT22);
                            System.out.println("Velocidad del viento  " + velViento);
                            /*
                            HashMap<String, String> datos = new HashMap<>();
                            datos.put("Temperatura", temp);
                            datos.put("Temperatura DHT22", tempDHT22);
                            datos.put("Humedad", humedad);
                            datos.put("Humedad DHT22", humedadDHT22);
                            datos.put("Presión", presion);
                            datos.put("Velocidad viento", velViento);

                            Date date = new Date();

                            for (Map.Entry<String, String> entry : datos.entrySet()) {

                                CountDownLatch donemm3Lluv = new CountDownLatch(1);
                                Map<String, Object> dato = new HashMap<>();
                                dato.put(entry.getKey(), entry.getValue());

                                FirebaseDatabase.getInstance().getReference("Dia").child(dateFormat.format(date) + "/" + horaFormat.format(date)).updateChildren(dato, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError de, DatabaseReference dr) {
                                        donemm3Lluv.countDown();
                                    }
                                });
                                donemm3Lluv.await();

                            }
                             */
                        }

                    } catch (NumberFormatException ex) {
                        //Sensor dañado, llama a un tecnico
                        Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    System.out.println("-  -  -  -  -  -  -");
                } while (!Thread.interrupted());
                System.out.println("INFO - Hilo cerrado");
            } catch (IOException ex) {
                Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        class ThreadClasifica extends Thread {

            float temp;
            float humedad;
            float presion;
            float tempDHT22;
            float humedadDHT22;
            float velViento;

            @Override
            public void run() {

            }

            void comparaDatos(float datos[], float porcentaje) {
                
            }

        }
    }

}

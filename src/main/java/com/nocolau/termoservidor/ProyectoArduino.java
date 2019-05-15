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
import java.util.Iterator;
import java.util.List;
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
    private int puerto = 20003;
    static long TIEMPO_BUCLE = 30000;
    static int TIEMPO_DIV_VARIABLE = 3;

    private final Date diaInicio = new Date();

    private FileInputStream serviceAccount;
    private FirebaseOptions options;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DateFormat dateFormat;
    private DateFormat horaFormat;

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
                Servidor servidor = new Servidor("HILO_SERVIDOR", sk);
                servidor.start();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    class Servidor extends Thread {

        private Socket sk;
        private BufferedReader in;
        private BufferedWriter out;

        private float temp[] = new float[TIEMPO_DIV_VARIABLE];
        private float humedad[] = new float[TIEMPO_DIV_VARIABLE];
        private float presion[] = new float[TIEMPO_DIV_VARIABLE];
        private float tempDHT22[] = new float[TIEMPO_DIV_VARIABLE];
        private float humedadDHT22[] = new float[TIEMPO_DIV_VARIABLE];
        private float velViento[] = new float[TIEMPO_DIV_VARIABLE];

        private int posNumDatos = 0;

        Servidor(String nombre, Socket sk) {
            super(nombre);
            this.sk = sk;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(sk.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(sk.getOutputStream()));

                System.out.println("INFO - Conectado " + sk.getInetAddress().getHostAddress());
                //Lee un caracter en formato byte
                do {
                    try {

                        switch (in.readLine()) {
                            case "1111":
                                out.write((TIEMPO_BUCLE / TIEMPO_DIV_VARIABLE) + "");
                                out.flush();
                                break;
                            case "0000":
                                recibirDatos();
                                break;
                            default:
                                System.out.println("ERROR - Attack in Server ");
                                break;
                        }

                    } catch (NumberFormatException ex) {
                        //Sensor dañado, llama a un tecnico
                        Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } while (!Thread.interrupted());
                System.out.println("INFO - Hilo cerrado");
            } catch (IOException ex) {
                Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private void recibirDatos() throws IOException, NumberFormatException {
            temp[posNumDatos]
                    = onlyTwoDecimalPlaces(in.readLine());
            humedad[posNumDatos]
                    = onlyTwoDecimalPlaces(in.readLine());
            presion[posNumDatos]
                    = onlyTwoDecimalPlaces(in.readLine());
            tempDHT22[posNumDatos]
                    = onlyTwoDecimalPlaces(in.readLine());
            humedadDHT22[posNumDatos]
                    = onlyTwoDecimalPlaces(in.readLine());
            velViento[posNumDatos]
                    = onlyTwoDecimalPlaces(in.readLine());
            /*
                            String temp = in.readLine() + "ºC";
                            String humedad = in.readLine() + "%";
                            String presion = in.readLine() + "Pa";
                            String tempDHT22 = in.readLine() + "ºC";
                            String humedadDHT22 = in.readLine() + "%";
                            String velViento = in.readLine() + "ms/rad";*/

            System.out.println("INFO - datos recibidos (" + (posNumDatos + 1) + "/" + TIEMPO_DIV_VARIABLE + ")");
            System.out.println("Temperatura  " + temp[posNumDatos] + "ºC");
            System.out.println("Humedad  " + humedad[posNumDatos] + "%");
            System.out.println("Presión  " + presion[posNumDatos] + "Pa");
            System.out.println("Temperatura DHT22  " + tempDHT22[posNumDatos] + "ºC");
            System.out.println("Humedad DHT22  " + humedadDHT22[posNumDatos] + "%");
            System.out.println("Velocidad del viento  " + velViento[posNumDatos] + "ms/rad");
            System.out.println("-  -  -  -  -  -  -");

            posNumDatos++;
            if (posNumDatos >= TIEMPO_DIV_VARIABLE) {
                posNumDatos = 0;
                ThreadClasifica clasifica = new ThreadClasifica("HILO_CLASIFICAR");
                clasifica.start();
            }
        }

        private float onlyTwoDecimalPlaces(String number) {
            StringBuilder sbFloat = new StringBuilder(number);
            int start = sbFloat.indexOf(".");
            if (start < 0) {
                return new Float(sbFloat.toString());
            }
            int end = start + 3;
            if ((end) > (sbFloat.length() - 1)) {
                end = sbFloat.length();
            }

            String twoPlaces = sbFloat.substring(start, end);
            sbFloat.replace(start, sbFloat.length(), twoPlaces);
            return new Float(sbFloat.toString());
        }

        class ThreadClasifica extends Thread {

            private float _temp;
            private float _humedad;
            private float _presion;
            private float _tempDHT22;
            private float _humedadDHT22;
            private float _velViento;

            private ThreadClasifica(String nombre) {
                super(nombre);
            }

            @Override
            public void run() {

                // differencia que hay entre un dato y otro
                _temp = onlyTwoDecimalPlaces(String.valueOf(comparaDatos(temp, 10.0f)));
                _humedad = comparaDatos(humedad, 20.0f);
                _presion = comparaDatos(presion, 100.0f);
                _tempDHT22 = comparaDatos(tempDHT22, 10.0f);
                _humedadDHT22 = comparaDatos(humedadDHT22, 20.0f);
                _velViento = comparaDatos(velViento, 10000.0f);

                System.out.println("INFO - CLASIFICAR DATOS");
                System.out.println("Temperatura  " + _temp + "ºC");
                System.out.println("Humedad  " + _humedad + "%");
                System.out.println("Presión  " + _presion + "Pa");
                System.out.println("Temperatura DHT22  " + _tempDHT22 + "ºC");
                System.out.println("Humedad DHT22  " + _humedadDHT22 + "%");
                System.out.println("Velocidad del viento  " + _velViento + "ms/rad");
                System.out.println("-  -  -  -  -  -  -");

                ThreadSubirDatos subirDatos = new ThreadSubirDatos("HILO_SUBIR_BD");
                subirDatos.start();
            }

            float comparaDatos(float datos[], float rangoError) {
                List<Float> media = new ArrayList<>();

                //Mira los datos en recursivo, si es correcto lo guarda en un array para calcular
                for (int numeroComparar = 0; numeroComparar < datos.length; numeroComparar++) {
                    if (recurComparaDatos(numeroComparar, datos, 0, rangoError, 1, 2)) {
                        media.add(datos[numeroComparar]);
                    }
                }

                float sum = 0f;
                int count = media.size();
                //Hce la media de los datos dentro del porcentaje de error
                for (Iterator<Float> iterator = media.iterator(); iterator.hasNext();) {
                    sum += iterator.next();
                }

                return (sum / count);
            }

            boolean recurComparaDatos(int datoFijo, float datos[], int posArray, float rangoError, int datosCorrectos, int minPas) {
                if (posArray >= datos.length) {
                    return datosCorrectos >= minPas;
                } else {
                    if (datoFijo == posArray) {
                        return recurComparaDatos(datoFijo, datos, posArray + 1, rangoError, datosCorrectos, minPas);
                    } else {
                        if (Math.abs(datos[datoFijo] - datos[posArray]) <= rangoError) {
                            return recurComparaDatos(datoFijo, datos, posArray + 1, rangoError, datosCorrectos + 1, minPas);
                        } else {
                            return recurComparaDatos(datoFijo, datos, posArray + 1, rangoError, datosCorrectos, minPas);
                        }
                    }
                }
            }

            //Hilo que sube los datos al servidor
            class ThreadSubirDatos extends Thread {

                private ThreadSubirDatos(String nombre) {
                    super(nombre);
                }

                @Override
                public void run() {
                    System.out.println("INFO - Subiendo Datos al servidor");

                    HashMap<String, Float> datos = new HashMap<>();
                    datos.put("Temperatura", _temp);
                    datos.put("Temperatura DHT22", _tempDHT22);
                    datos.put("Humedad", _humedad);
                    datos.put("Humedad DHT22", _humedadDHT22);
                    datos.put("Presión", _presion);
                    datos.put("Velocidad viento", _velViento);

                    Date date = new Date();

                    for (Map.Entry<String, Float> entry : datos.entrySet()) {

                        CountDownLatch donemm3Lluv = new CountDownLatch(1);
                        Map<String, Object> dato = new HashMap<>();
                        dato.put(entry.getKey(), entry.getValue());

                        FirebaseDatabase.getInstance().getReference("Dia").child(dateFormat.format(date) + "/" + horaFormat.format(date)).updateChildren(dato, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError de, DatabaseReference dr) {
                                donemm3Lluv.countDown();
                            }
                        });
                        try {
                            donemm3Lluv.await();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }

            }

        }
    }

}

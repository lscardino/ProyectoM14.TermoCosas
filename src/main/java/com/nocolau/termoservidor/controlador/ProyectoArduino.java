/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor.controlador;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.nocolau.termoservidor.modelo.DatosPaquete;
import com.nocolau.termoservidor.modelo.ConfiguracionServidor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cho_S
 */
public class ProyectoArduino {

    private int puerto;
    private long TIEMPO_BUCLE;
    private int TIEMPO_DIV_VARIABLE;
    private float PORC_ACEPTACION;

    private FileInputStream serviceAccount;
    private FirebaseOptions options;

    private final Date diaInicio = new Date();
    private static boolean bucleCrearServidor = true;
    private static boolean bucleServidorEscucha = true;

    /**
     *
     * @param nConfigServidor Class con las configuraciones hechas o
     * predeerminadas del main
     * @see Inicia la conexión con la base de datos FireBase y Crea el servidor
     * socket para recibir de los microordenadores
     */
    public ProyectoArduino(ConfiguracionServidor nConfigServidor) {
        this.puerto = nConfigServidor.getPuerto();
        this.TIEMPO_BUCLE = nConfigServidor.getTIEMPO_BUCLE();
        this.TIEMPO_DIV_VARIABLE = nConfigServidor.getTIEMPO_DIV_VARIABLE();
        this.PORC_ACEPTACION = nConfigServidor.getPORC_ACEPTACION();

        //Mira si esta el Puerto esta libre.
        /*Si esta ocupado, hay un posiblidad que sea el mismo programa.
        Por tanto hacemos que el antiguo cierre sessión y entre el nuevo.
         */
        pararServidorActual(nConfigServidor);

        //Crear Salida por terminal
        ThreadControladorTerminal controladorTerminal = new ThreadControladorTerminal("HILO_TERMINAL", nConfigServidor);
        //controladorTerminal.start();
        try {
            File archivo = new File(getClass().getResource("src" + File.separator + "main" + File.separator + "resources" + File.separator + "termomovidas-firebase-adminsdk-qgjn6-378a7de574.json").getFile());
            //serviceAccount = new FileInputStream("src" + File.separator + "main" + File.separator + "resources" + File.separator + "termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");
            //serviceAccount = (FileInputStream) ProyectoArduino.class.getResourceAsStream("src" + File.separator + "main" + File.separator + "resources" + File.separator + "termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");
            serviceAccount = new FileInputStream(archivo);
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
        }

        //SERVIDOR
        try {
            ServerSocket ssk = new ServerSocket(puerto);
            ssk.setSoTimeout((int) (10000));
            System.out.println(ssk.getLocalSocketAddress().toString());
            System.out.println(ssk.getInetAddress().getLocalHost());

            ArrayList<Socket> skList = new ArrayList<>();
            while (bucleCrearServidor) {
                try {
                    Socket sk = ssk.accept();
                    skList.add(sk); //espera una conexión de un cliente
                    System.out.println("-  -  -  -  -  -  -  -  -  -  -  -  -");
                    Servidor servidor = new Servidor("HILO_SERVIDOR", sk);
                    servidor.start();
                } catch (java.net.SocketTimeoutException exOut) {
                }
            }
            ssk.close();
            for (Socket next : skList) {
                try {
                    next.close();
                } catch (IOException ex) {
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            controladorTerminal.interrupt();
            System.out.println("INFO - Hilo crea crea socket CERRADO");
        }
    }

    /**
     *
     * @param nConfigServidor Class con la configuración del servidor
     * @see Crea un hilo para iniciar en el servidor y cerrar la sessión.
     */
    private void pararServidorActual(ConfiguracionServidor nConfigServidor) {
        CountDownLatch latchRevocarServidor = new CountDownLatch(1);
        ThreadOutServidor outServer = new ThreadOutServidor(nConfigServidor, latchRevocarServidor);
        outServer.start();
        try {
            latchRevocarServidor.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @see Hilo que controla las entradas del terminal
     */
    class ThreadControladorTerminal extends Thread {

        private final String STR_SALIR = "EXIT";
        private ConfiguracionServidor configServidor;

        /**
         *
         * @param nombre Nombre del hilo
         * @param nConfigServidor configuracion del servidor para poder conectar
         */
        public ThreadControladorTerminal(String nombre, ConfiguracionServidor nConfigServidor) {
            super(nombre);
            this.configServidor = nConfigServidor;
        }

        @Override
        public void run() {
            Scanner sc = new Scanner(System.in);
            String toReceive;

            do {
                toReceive = sc.nextLine();
            } while (!STR_SALIR.equals(toReceive.trim().toUpperCase()) || bucleCrearServidor);

            System.out.println("Preparando para salir");

            if (bucleCrearServidor) {
                pararServidorActual(configServidor);
            }
        }

    }

    /**
     * @see Hilo que inicia al conectar un usuario al socket Envia datos y
     * recibe de los microordenadores que estan conectados
     */
    class Servidor extends Thread {

        private Socket sk;
        private BufferedReader in;
        private BufferedWriter out;

        private DatosPaquete paqueteDatos;

        private int posNumDatos = 0;

        Servidor(String nombre, Socket sk) {
            super(nombre);
            this.sk = sk;
            paqueteDatos = new DatosPaquete(TIEMPO_DIV_VARIABLE);
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
                            case "EXIT":
                                bucleCrearServidor = false;
                                bucleServidorEscucha = false;
                            default:
                                System.out.println("ERROR - Attack in Server ");
                                break;
                        }

                    } catch (NumberFormatException ex) {
                        //Sensor dañado, llama a un tecnico
                        Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } while (bucleServidorEscucha);
            } catch (IOException ex) {
                Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sk.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("INFO - Hilo cerrado");
            }

        }

        /**
         *
         * @throws IOException Error al leer los datos
         * @throws NumberFormatException Error al pasar los datos a float
         * @see Lee los datos recibidos en una clase paquete y espera al tiempo
         * para clasificar los datos
         */
        private void recibirDatos() throws IOException, NumberFormatException {

            for (DatosPaquete.EnumDato enumDato : DatosPaquete.EnumDato.values()) {
                paqueteDatos.introducirdatos(enumDato, Float.parseFloat(in.readLine()), posNumDatos);
            }

            System.out.println("-  -  -  -  -  -  -");
            System.out.println("INFO - datos recibidos (" + (posNumDatos + 1) + "/" + TIEMPO_DIV_VARIABLE + ")");/*
            System.out.println("Temperatura  " + temp[posNumDatos] + "ºC");
            System.out.println("Humedad  " + humedad[posNumDatos] + "%");
            System.out.println("Presión  " + presion[posNumDatos] + "Pa");
            System.out.println("Temperatura DHT22  " + tempDHT22[posNumDatos] + "ºC");
            System.out.println("Humedad DHT22  " + humedadDHT22[posNumDatos] + "%");
            System.out.println("Velocidad del viento  " + velViento[posNumDatos] + "ms/rad");*/
            posNumDatos++;
            if (posNumDatos >= TIEMPO_DIV_VARIABLE) {
                posNumDatos = 0;
                ThreadClasificaSubeDato clasifica = new ThreadClasificaSubeDato("HILO_CLASIFICAR_SUBE_DATOS", paqueteDatos, PORC_ACEPTACION);
                clasifica.start();
            }
        }

    }
}

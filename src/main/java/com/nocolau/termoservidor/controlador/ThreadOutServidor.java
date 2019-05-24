/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor.controlador;

import com.nocolau.termoservidor.modelo.ConfiguracionServidor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see Clase que conecta con el servidor para cerrar sessión.
 * @author Cho_S
 */
public class ThreadOutServidor extends Thread {

    private ConfiguracionServidor configServidor;
    private CountDownLatch latch;
    private int puerto;

    /**
     * 
     * @param nConfig Configuración del servidor para recoger el puerto
     * @param latchRevocarServidor Un "Await" para que el hilo que llamo a esté esperé a terminar los procesos.
     */
    ThreadOutServidor(ConfiguracionServidor nConfig, CountDownLatch latchRevocarServidor) {
        this.configServidor = nConfig;
        this.latch = latchRevocarServidor;
    }

    @Override
    public void run() {

        try {
            puerto = configServidor.getPuerto();
            Socket skCheckPort = new Socket("localhost", puerto);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(skCheckPort.getOutputStream()));
            out.write("EXIT");
            out.newLine();
            out.flush();

            System.out.println("INFO - Revocando servidor");
            boolean servidorNoLibre;
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProyectoArduino.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    skCheckPort = new Socket("localhost", puerto);
                    servidorNoLibre = false;
                } catch (IOException exIntent) {
                    servidorNoLibre = true;
                } finally {
                    if (skCheckPort != null) {
                        skCheckPort.close();
                    }
                }
            } while (servidorNoLibre);
        } catch (IOException ex) {
            //Hay servidor en este puerto
        }
        latch.countDown();
    }
}

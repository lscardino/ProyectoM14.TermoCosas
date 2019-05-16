/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cho_S
 */
public class SkOutServidor {

    private boolean pr1 = true;

    SkOutServidor(ConfiguracionServidor nConfig) throws InterruptedException, IOException {

        int puerto = nConfig.getPuerto();

        Socket skCheckPort = new Socket("localhost", puerto);
        System.out.println("INFO - HAY SERVIDOR");

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(skCheckPort.getOutputStream()));
        out.write("EXIT");
        out.newLine();
        out.flush();

        System.out.println("INFO - Esperando que el servidor cierre");
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
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import com.nocolau.termoservidor.controlador.ProyectoArduino;
import com.nocolau.termoservidor.modelo.ConfiguracionServidor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Cho_S
 */
public class Controlador {

    public static void main(String[] args) throws IOException {
        ConfiguracionServidor nConfig = new ConfiguracionServidor();
        try {
            switch (args.length) {
                case 1:
                    nConfig.setPuerto(Integer.parseInt(args[0]));
                    break;
                case 2:
                    nConfig.setTIEMPO_BUCLE(Long.parseLong(args[0]));
                    nConfig.setTIEMPO_DIV_VARIABLE(Integer.parseInt(args[1]));
                    break;
                case 3:
                    nConfig.setPuerto(Integer.parseInt(args[0]));
                    nConfig.setTIEMPO_BUCLE(Long.parseLong(args[1]));
                    nConfig.setTIEMPO_DIV_VARIABLE(Integer.parseInt(args[2]));
                    break;
                default:
                    throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            System.out.println("ERROR - Valor incorrecto, el programa usara los valores predeterminados");
            System.out.println("INFO -        Puerto        -> " + nConfig.getPuerto());
            System.out.println("INFO -   Tiempo de datos    -> " + nConfig.getTIEMPO_BUCLE());
            System.out.println("INFO -  Tiempo muestras(" + nConfig.getTIEMPO_DIV_VARIABLE()
                    + ")  -> " + (nConfig.getTIEMPO_BUCLE() / nConfig.getTIEMPO_DIV_VARIABLE()) + "ms"
            );
        }
        //new SkOutServidor((nConfig));
        ProyectoArduino arduino = new ProyectoArduino(nConfig);
    }
}

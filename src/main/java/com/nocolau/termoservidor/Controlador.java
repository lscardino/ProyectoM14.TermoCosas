/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import com.nocolau.termoservidor.controlador.ProyectoArduino;
import com.nocolau.termoservidor.modelo.ConfiguracionServidor;
import java.io.IOException;

/**
 * 
 * @author Cho_S
 * @author LScardini
 */
public class Controlador {
    /**
     * 
     * @param args
     * Variables a pasar para configurar;
     * -Puerto
     * -Tiempo a Esperar en minutos, tiempo en cada muestra
     * -Puerto, Tiempo a Esperar, Muestra a Dividir
     * @throws IOException 
     */
    public static void main(String[] args) {
        long minutos = 30;
        int div = 10;
        int tiempoXMuestra;
        ConfiguracionServidor nConfig = new ConfiguracionServidor(minutos * 60000, div);
        try {
            switch (args.length) {
                case 1:
                    nConfig.setPuerto(Integer.parseInt(args[0]));
                    break;
                case 2:
                    tiempoXMuestra = (int)(Long.parseLong(args[0]) / Float.parseFloat(args[1]));
                    nConfig.setTIEMPO_BUCLE(Long.parseLong(args[0])* 60000);
                    nConfig.setTIEMPO_DIV_VARIABLE(tiempoXMuestra);
                    break;
                case 3:
                    tiempoXMuestra = (int)(Long.parseLong(args[1]) / Float.parseFloat(args[2]));
                    nConfig.setPuerto(Integer.parseInt(args[0]));
                    nConfig.setTIEMPO_BUCLE(Long.parseLong(args[1])* 60000);
                    nConfig.setTIEMPO_DIV_VARIABLE(tiempoXMuestra);
                    break;
                default:
                    throw new NumberFormatException();
            }
            System.out.println("INFO - Valor introducidos.");
        } catch (NumberFormatException ex) {
            System.out.println("ERROR - Valor incorrecto, el programa usara los valores predeterminados");
        }
        System.out.println("INFO -        Puerto        -> " + nConfig.getPuerto());
        System.out.println("INFO -   Tiempo de datos    -> " + nConfig.getTIEMPO_BUCLE() + "ms " 
                + convertToMin(nConfig.getTIEMPO_BUCLE()) + "min");
        System.out.println("INFO -  Tiempo muestras(" + nConfig.getTIEMPO_DIV_VARIABLE()
                + ")  -> " + (nConfig.getTIEMPO_BUCLE() / nConfig.getTIEMPO_DIV_VARIABLE()) + "ms "
                + convertToMin((nConfig.getTIEMPO_BUCLE() / nConfig.getTIEMPO_DIV_VARIABLE())) + "min"
        );
        //new SkOutServidor((nConfig));
        ProyectoArduino arduino = new ProyectoArduino(nConfig);
    }
    
    private static long convertToMin(long miliSeg){
        return miliSeg/60000;
    }
}

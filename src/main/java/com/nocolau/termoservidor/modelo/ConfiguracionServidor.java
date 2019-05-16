/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor.modelo;

/**
 *
 * @author Cho_S
 */
public class ConfiguracionServidor {

    private int puerto = 20003;
    private long TIEMPO_BUCLE = 30000;
    private int TIEMPO_DIV_VARIABLE = 3;

    public ConfiguracionServidor() {
    }

    public ConfiguracionServidor(int nPuerto) {
        this.puerto = nPuerto;
    }

    public ConfiguracionServidor(long nTiempoBucle, int nTiempoDiv) {
        this.TIEMPO_BUCLE = nTiempoBucle;
        this.TIEMPO_DIV_VARIABLE = nTiempoDiv;
    }

    public ConfiguracionServidor(int nPuerto, long nTiempoBucle, int nTiempoDiv) {
        this.puerto = nPuerto;
        this.TIEMPO_BUCLE = nTiempoBucle;
        this.TIEMPO_DIV_VARIABLE = nTiempoDiv;

    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public long getTIEMPO_BUCLE() {
        return TIEMPO_BUCLE;
    }

    public void setTIEMPO_BUCLE(long TIEMPO_BUCLE) {
        this.TIEMPO_BUCLE = TIEMPO_BUCLE;
    }

    public int getTIEMPO_DIV_VARIABLE() {
        return TIEMPO_DIV_VARIABLE;
    }

    public void setTIEMPO_DIV_VARIABLE(int TIEMPO_DIV_VARIABLE) {
        this.TIEMPO_DIV_VARIABLE = TIEMPO_DIV_VARIABLE;
    }

}

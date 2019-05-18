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
public class DatosPaquete {

    public static enum EnumDato {
        temp,
        humedad,
        presion,
        tempDHT22,
        humedadDHT22,
        velViento,
        lluvia,
        polvo
    }

    private final int TIEMPO_DIV_VARIABLE;
    private float[] _temp;
    private float[] _humedad;
    private float[] _presion;
    private float[] _tempDHT22;
    private float[] _humedadDHT22;
    private float[] _velViento;
    private float[] _lluvia;
    private float[] _polvo;

    public DatosPaquete(int nTiempoDiv) {
        this.TIEMPO_DIV_VARIABLE = nTiempoDiv;

        _temp = new float[TIEMPO_DIV_VARIABLE];
        _humedad = new float[TIEMPO_DIV_VARIABLE];
        _presion = new float[TIEMPO_DIV_VARIABLE];
        _tempDHT22 = new float[TIEMPO_DIV_VARIABLE];
        _humedadDHT22 = new float[TIEMPO_DIV_VARIABLE];
        _velViento = new float[TIEMPO_DIV_VARIABLE];
        _lluvia = new float[TIEMPO_DIV_VARIABLE];
        _polvo = new float[TIEMPO_DIV_VARIABLE];
    }

    public void introducirdatos(EnumDato enumDato, float numDato, int numPos) {
        switch (enumDato) {
            case temp:
                _temp[numPos] = numDato;
                break;
            case humedad:
                _humedad[numPos] = numDato;
                break;
            case presion:
                _presion[numPos] = numDato;
                break;
            case tempDHT22:
                _tempDHT22[numPos] = numDato;
                break;
            case humedadDHT22:
                _humedadDHT22[numPos] = numDato;
                break;
            case velViento:
                _velViento[numPos] = numDato;
                break;
            case lluvia:
                _lluvia[numPos] = numDato;
                break;
            case polvo:
                _polvo[numPos] = numDato;
                break;
        }
    }

    public float[] getTemp() {
        return _temp;
    }

    public float[] getHumedad() {
        return _humedad;
    }

    public float[] getPresion() {
        return _presion;
    }

    public float[] getTempDHT22() {
        return _tempDHT22;
    }

    public float[] getHumedadDHT22() {
        return _humedadDHT22;
    }

    public float[] getVelViento() {
        return _velViento;
    }

    public float[] getLluvia() {
        return _lluvia;
    }

    public float[] getPolvo() {
        return _polvo;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import com.google.api.client.util.DateTime;

/**
 *
 * @author Lorenzo
 */
public class Dato {
    public int temp;
    public int humedad;
    public int velViento;

    public Dato(int temp, int humedad, int velViento) {
        this.temp = temp;
        this.humedad = humedad;
        this.velViento = velViento;
    }
}

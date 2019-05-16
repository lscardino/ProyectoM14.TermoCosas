/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor.controlador;

import com.nocolau.termoservidor.modelo.DatosPaquete;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class ThreadClasificaSubeDato extends Thread {

    private float _temp;
    private float _humedad;
    private float _presion;
    private float _tempDHT22;
    private float _humedadDHT22;
    private float _velViento;
    private DatosPaquete paqueteDatos;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DateFormat dateFormat;
    private DateFormat horaFormat;

    ThreadClasificaSubeDato(String nombre, DatosPaquete nPaquete) {
        super(nombre);
        this.paqueteDatos = nPaquete;
    }

    @Override
    public void run() {

        // differencia que hay entre un dato y otro
        _temp = comparaDatos(paqueteDatos.getTemp(), 10.0f);
        _humedad = comparaDatos(paqueteDatos.getHumedad(), 20.0f);
        _presion = comparaDatos(paqueteDatos.getPresion(), 100.0f);
        _tempDHT22 = comparaDatos(paqueteDatos.getTempDHT22(), 10.0f);
        _humedadDHT22 = comparaDatos(paqueteDatos.getHumedadDHT22(), 20.0f);
        _velViento = comparaDatos(paqueteDatos.getVelViento(), 10000.0f);

        System.out.println("-  -  -  -  -  -  -");
        System.out.println("INFO - CLASIFICAR DATOS");
        System.out.println("Temperatura  " + _temp + "ºC");
        System.out.println("Humedad  " + _humedad + "%");
        System.out.println("Presión  " + _presion + "Pa");
        System.out.println("Temperatura DHT22  " + _tempDHT22 + "ºC");
        System.out.println("Humedad DHT22  " + _humedadDHT22 + "%");
        System.out.println("Velocidad del viento  " + _velViento + "ms/rad");

        subirDatosFirebase();
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

    private void subirDatosFirebase() {
        database = FirebaseDatabase.getInstance();

        ref = database.getReference();
        dateFormat = new SimpleDateFormat("MM-dd");
        horaFormat = new SimpleDateFormat("HH:mm");

        System.out.println("-  -  -  -  -  -  -");
        System.out.println("INFO - Subiendo Datos al servidor");

        HashMap<String, Float> datos = new HashMap<>();
        datos.put("Temperatura", onlyTwoDecimalPlaces(String.valueOf(_temp)));
        datos.put("Temperatura DHT22", onlyTwoDecimalPlaces(String.valueOf(_tempDHT22)));
        datos.put("Humedad", onlyTwoDecimalPlaces(String.valueOf(_humedad)));
        datos.put("Humedad DHT22", onlyTwoDecimalPlaces(String.valueOf(_humedadDHT22)));
        datos.put("Presión", onlyTwoDecimalPlaces(String.valueOf(_presion)));
        datos.put("Velocidad viento", onlyTwoDecimalPlaces(String.valueOf(_velViento)));

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
}

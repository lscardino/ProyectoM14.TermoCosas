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
 * @see Hilo que clasifica los datos y los envia al servidor FireBase
 */
public class ThreadClasificaSubeDato extends Thread {

    private float PORC_ACEPTACION;

    private float _temp;
    private float _humedad;
    private float _presion;
    private float _tempDHT22;
    private float _humedadDHT22;
    private float _velViento;
    private float _lluvia;
    private float _polvo;
    private float _sensacion;
    private DatosPaquete paqueteDatos;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat horaFormat = new SimpleDateFormat("HH:mm");
    Date date = new Date();

    ThreadClasificaSubeDato(String nombre, DatosPaquete nPaquete, float porcAceptacion) {
        super(nombre);
        this.paqueteDatos = nPaquete;
        this.PORC_ACEPTACION = porcAceptacion;
    }

    /**
     * @see Mira los datos si estan tendro del rango de error y las envia al
     * servidor
     */
    @Override
    public void run() {

        // differencia que hay entre un dato y otro
        _temp = comparaDatos(paqueteDatos.getTemp(), 10.0f);
        _humedad = comparaDatos(paqueteDatos.getHumedad(), 20.0f);
        _presion = comparaDatos(paqueteDatos.getPresion(), 100.0f);
        _tempDHT22 = comparaDatos(paqueteDatos.getTempDHT22(), 10.0f);
        _humedadDHT22 = comparaDatos(paqueteDatos.getHumedadDHT22(), 20.0f);
        _velViento = comparaDatos(paqueteDatos.getVelViento(), 10000.0f);
        _lluvia = comparaDatos(paqueteDatos.getLluvia(), 10000.0f);
        _polvo = comparaDatos(paqueteDatos.getPolvo(), 10000.0f);
        _sensacion = comparaDatos(paqueteDatos.getSensacion(), 10.0f);

        System.out.println("-  -  -  -  -  -  -");
        System.out.println("INFO - CLASIFICAR DATOS");
        System.out.println("Temperatura  " + _temp + "ºC");
        System.out.println("Humedad  " + _humedad + "%");
        System.out.println("Presión  " + _presion + "Pa");
        System.out.println("Temperatura DHT22  " + _tempDHT22 + "ºC");
        System.out.println("Humedad DHT22  " + _humedadDHT22 + "%");
        System.out.println("Velocidad del viento  " + _velViento + "ms/rad");
        System.out.println("Lluvia  " + _lluvia + "ml/h");
        System.out.println("Polvo  " + _polvo + "mg/m3");
        System.out.println("Sensación térmica  " + _sensacion + "ºC");

        subirDatosFirebase();
        if (horaFormat.format(date).equals("00:00")) {
            //Subir tmb lo de lso transportes
        }
    }

    /**
     * @see método que revisa los datos entrantes si estan dentro del rango de
     * error.
     * @param datos Array los datos a revisar.
     * @param rangoError Valor del rango de error que deben estar para poder
     * visualizar.
     * @return Devuelte una media de los valores correctos que están dentro del
     * rango de error.
     */
    float comparaDatos(float datos[], float rangoError) {
        List<Float> media = new ArrayList<>();

        //Mira los datos en recursivo, si es correcto lo guarda en un array para calcular
        for (int numeroComparar = 0; numeroComparar < datos.length; numeroComparar++) {
            if (recurComparaDatos(numeroComparar, datos, 0, rangoError, 1, (int) (datos.length * PORC_ACEPTACION))) {
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

    /**
     * @see Método recursivo para calcular los datos.
     * @param datoFijo Posición del valor principal para controlar.
     * @param datos Array con los datos a revisar.
     * @param posArray Posición del valor a revisar con el principal.
     * @param rangoError El rango de error que puede tener para poder pasar.
     * @param datosCorrectos Número de datos correctos que han pasado.
     * @param minPas Número de datos correctos para poder pasar la prueba.
     * @return
     */
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

    /**
     * @see Método pasar subir los datos revisado a la base de datos.
     */
    private void subirDatosFirebase() {
        database = FirebaseDatabase.getInstance();

        ref = database.getReference();

        System.out.println("-  -  -  -  -  -  -");
        System.out.println("INFO - Subiendo Datos al servidor");

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("Temperatura", onlyTwoDecimalPlaces(String.valueOf(_temp)));
        datos.put("Temperatura DHT22", onlyTwoDecimalPlaces(String.valueOf(_tempDHT22)));
        datos.put("Humedad", onlyTwoDecimalPlaces(String.valueOf(_humedad)));
        datos.put("Humedad DHT22", onlyTwoDecimalPlaces(String.valueOf(_humedadDHT22)));
        datos.put("Presión", onlyTwoDecimalPlaces(String.valueOf(_presion)));
        datos.put("Velocidad viento", onlyTwoDecimalPlaces(String.valueOf(_velViento)));
        datos.put("Lluvia", onlyTwoDecimalPlaces(String.valueOf(_lluvia)));
        datos.put("Polvo", onlyTwoDecimalPlaces(String.valueOf(_polvo)));
        datos.put("Sensacion", onlyTwoDecimalPlaces(String.valueOf(_sensacion)));

        CountDownLatch donemm3Lluv = new CountDownLatch(1);
        FirebaseDatabase.getInstance().getReference("Dia").child(dateFormat.format(date) + "/Hora/" + horaFormat.format(date)).updateChildren(datos, new DatabaseReference.CompletionListener() {
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

    private void crearTransportes() {
        database = FirebaseDatabase.getInstance();

        ref = database.getReference();

        System.out.println("-  -  -  -  -  -  -");
        System.out.println("INFO - Subiendo Trasnportes al servidor");

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("Coche", String.valueOf(0));
        datos.put("Bici", String.valueOf(0));
        datos.put("Apie", String.valueOf(0));
        datos.put("Tpublico", String.valueOf(0));

        CountDownLatch donemm3Lluv = new CountDownLatch(1);

        FirebaseDatabase.getInstance().getReference("Dia").child(dateFormat.format(date) + "/" + horaFormat.format(date) + "/Transporte").updateChildren(datos, new DatabaseReference.CompletionListener() {
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

    /**
     * @see Método para recortar los float a XX.XX
     * @param number Valor en string del float a recortar.
     * @return El valor en un formato más pequeño.
     */
    private String onlyTwoDecimalPlaces(String number) {
        StringBuilder sbFloat = new StringBuilder(number);
        int start = sbFloat.indexOf(".");
        if (start < 0) {
            return sbFloat.toString();
        }
        int end = start + 3;
        if ((end) > (sbFloat.length() - 1)) {
            end = sbFloat.length();
        }

        String twoPlaces = sbFloat.substring(start, end);
        sbFloat.replace(start, sbFloat.length(), twoPlaces);
        return sbFloat.toString();
    }
}

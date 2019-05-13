/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor;

import com.google.api.client.util.DateTime;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Lorenzo
 */
public class EjecutarFireBase {

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        FileInputStream serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        // Firestore db = FirestoreClient.getFirestore();
        /*CountDownLatch done = new CountDownLatch(1);
        HashMap<String, Dato> datos = new HashMap<>();
        DatabaseReference referencia = ref.child("DatoNuevo");
        datos.put("primero", new Dato(2, 1, 10));
        referencia.setValueAsync("eee");
        done = new CountDownLatch(1);
         */
 /*        CountDownLatch done = new CountDownLatch(1);

        FirebaseDatabase.getInstance().getReference("Dia").child("27-03/humedad").setValue(15, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError de, DatabaseReference dr) {
                done.countDown();
            }
        });
        done.await();
        
        
         */
        DateFormat dateFormat = new SimpleDateFormat("MM-dd");
        DateFormat horaFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        System.out.println("FECHA ------" + dateFormat.format(date));

        HashMap<String, Integer> atributos = new HashMap<>();
        atributos.put("Humedad", 20);
        atributos.put("Temperatura", 12);
        atributos.put("Presion", 0);

        for (Map.Entry<String, Integer> entry : atributos.entrySet()) {

            CountDownLatch donemm3Lluv = new CountDownLatch(1);
            Map<String, Object> tempe = new HashMap<>();
            tempe.put(entry.getKey(), entry.getValue());

            FirebaseDatabase.getInstance().getReference("Dia").child(dateFormat.format(date) +"/"+ horaFormat.format(date)).updateChildren(tempe, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError de, DatabaseReference dr) {
                    donemm3Lluv.countDown();
                }
            });
            donemm3Lluv.await();
        }
    }
}

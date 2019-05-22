/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nocolau.termoservidor.controlador;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import static com.nocolau.termoservidor.controlador.CRONBorrarUltimaSemana.latch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Lorenzo
 */
public class CRONBorrarUltimaSemana {

    static FirebaseDatabase database;
    static DatabaseReference ref;
    static CountDownLatch latch;
    static Connection conn;
    static Query query;

    static ArrayList listaDias;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        FileInputStream serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        listaDias = new ArrayList();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Dia");
        //ref.child("02-03").removeValueAsync();
        query = ref.orderByKey().limitToFirst(1);
        obtenerDatos();
        latch.await();

        if (listaDias.size() > 1) {
            listaDias.remove(listaDias.size() - 1);
            borrarDatos();
        } else {
            System.out.println("No hay datos que borrar");
        }
        latch.await();
    }

    private static void obtenerDatos() {
        latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                System.out.println("Numero de hijos: " + ds.getChildrenCount());
                for (DataSnapshot hijo : ds.getChildren()) {
                    listaDias.add(hijo.getKey());

                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError de) {
                latch.countDown();
            }
        });

    }

    public static void borrarDatos() {
        latch = new CountDownLatch(1);

        for (Object objeto : listaDias) {
            FirebaseDatabase.getInstance().getReference("Dia").
                    child(objeto.toString()).removeValue((DatabaseError de,
                            DatabaseReference dr) -> {
                        latch.countDown();
            });
        }

    }
}

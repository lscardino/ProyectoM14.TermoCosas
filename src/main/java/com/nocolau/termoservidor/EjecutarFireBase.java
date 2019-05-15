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
import com.google.common.collect.HashBiMap;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
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
       DatabaseReference ref = database.getReference("movidas/chungas");
      
       CountDownLatch hecho = new CountDownLatch(1);
       
       
       /*ESTO VA FETÉN - Mete movidas
       DatabaseReference datosRef = ref.child("datiyos");
       
       Map<String, Dato> mapaDatos = new HashMap<>();
       mapaDatos.put("dato1", new Dato(10, 2, 4));
       mapaDatos.put("Datos 2", new Dato(34, 2, 1));
       
       datosRef.setValueAsync(mapaDatos);
       System.out.println("Done");
       */
       
       //ESTE MODIFICA UN CHILDREN
       DatabaseReference referencia = ref.child("datiyos");
       Map<String, Object> update = new HashMap<>();
       update.put("Datos 2/temp", "tirentiayuno");
       
       referencia.updateChildrenAsync(update);
       System.out.println("Ya de ya");
       
                           //hecho.countDown();
                           //hecho.await();
       
       
       
       //A VER, AUMENTA EL CONTADOR, PERO POR ALGUNA RAZÓN, NO FUNCIONA
       //SI NO LLAMAS AL METODO getDataDealgo --- NO SÉ PORQUE!
       DatabaseReference referenciaComp = ref.child("contador");
       referenciaComp.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData md) {
                Integer currentValue = md.getValue(Integer.class);
                System.out.println("Valor actual: " + currentValue);
                if (currentValue == null) {
                    md.setValue(1);
                    
                    System.out.println("Valor nulo, inicializado a 1");
                }else{
                    md.setValue(currentValue + 1);
                    System.out.println("Sumado +1");
                }
                return Transaction.success(md);
            }

            @Override
            public void onComplete(DatabaseError de, boolean bln, DataSnapshot ds) {
                System.out.println("Hecho");
            }
        });
       
       
       hecho.await();

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
        DateFormat dateFormat = new SimpleDateFormat("MM-dd--HH:mm");
        DateFormat horaFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        System.out.println("FECHA ------" + dateFormat.format(date));

        //CountDownLatch donemm3Lluv = new CountDownLatch(1);
        
        
        System.out.println("sss");
        
        //System.out.println("Datos -- " + getDataFromFirebase());
        
        
        /*
        HashMap<String, Integer> atributos = new HashMap<>();
        atributos.put("Humedad", 20);
        atributos.put("Temperatura", 12);
        atributos.put("Presion", 0);

        for (Map.Entry<String, Integer> entry : atributos.entrySet()) {

            CountDownLatch donemm3Lluv = new CountDownLatch(1);
            Map<String, Object> tempe = new HashMap<>();
            tempe.put(entry.getKey(), entry.getValue());
        */

        /*
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("DOOMSDAY").setValue(dateFormat.format(date), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError de, DatabaseReference dr) {
                    donemm3Lluv.countDown();
                }
            });
            donemm3Lluv.await();
        }
*/
    }
    
    private static String getDataFromFirebase() throws InterruptedException{
        CountDownLatch hecho = new CountDownLatch(1);
        StringBuilder sb = new StringBuilder();
        
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference();
        
        dbRef.child("Dia/05-15").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                if (ds.exists()) {
                    for (DataSnapshot snap : ds.getChildren()) {
                        Dato dato = ds.getValue(Dato.class);
                        sb.append(dato.humedad);
                    }
                    hecho.countDown();
                }else{
                    sb.append("No eriste");
                    hecho.countDown();
                }
                
                
            }

            @Override
            public void onCancelled(DatabaseError de) {
                sb.append("ERROR");
                hecho.countDown();
            }
        });
        hecho.await();
        return sb.toString();
    }
    
}




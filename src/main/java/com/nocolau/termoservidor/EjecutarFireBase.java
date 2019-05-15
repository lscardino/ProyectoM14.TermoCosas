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

    static FirebaseDatabase database;
    static DatabaseReference ref;
    static CountDownLatch latch;

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {

        //INICIALIZACION MOVIDAS FIREBASE
        FileInputStream serviceAccount = new FileInputStream("termomovidas-firebase-adminsdk-qgjn6-378a7de574.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://termomovidas.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("movidas/chungas");
        //FIN INICIALIZACION MOVIDAS FIREBASE

        sumarContador();
        devolverValor();
        //meterMovidas();
        MeterMovidasConListener();
        updateChildren();
        latch.await();

     
        
         
        DateFormat dateFormat = new SimpleDateFormat("MM-dd--HH:mm");
        DateFormat horaFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        System.out.println("FECHA ------" + dateFormat.format(date));

        System.out.println("sss");

        
    }

    //LISTA DE METODOS
    //Podría haberles añadido un parametro para poder establecer al referencia,
    //pero gñeeee
    //NOTA SOBRE LOS METODOS
    //Vale, básicamente, los listeners del fireBase se ejecutan de forma
    //asyncrona, por lo que hay una posibilidad (siempre) de que la ejecucion 
    //del main finalice antes de que lso listeners tengan tiemp ode hacer lo suyo
    //por lo que es menester asegurarse de que el main se espere
    //Cómo hacemos esto? con un CountDownLatch, que, bueno... leete la documentacion
    //de la clase, este link lo explica guay https://www.baeldung.com/java-countdown-latch
    
    
    //Suma +1 a un contador existente y si esteF está a "null", lo
    //inicializa a 0
    public static void sumarContador() throws InterruptedException {
        DatabaseReference referenciaComp = ref.child("contador");
        latch = new CountDownLatch(1);

        referenciaComp.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData md) {
                Integer currentValue = md.getValue(Integer.class);
                System.out.println("Valor actual: " + currentValue);
                if (currentValue == null) {
                    md.setValue(1);
                    System.out.println("Valor nulo, inicializado a 1");

                } else {
                    md.setValue(currentValue + 1);
                    System.out.println("Sumado +1");

                }

                return Transaction.success(md);
            }

            @Override
            public void onComplete(DatabaseError de, boolean bln, DataSnapshot ds) {
                System.out.println("Hecho");
                latch.countDown();
            }
        });

        latch.await();
    }

    //Te devuelve lo que quieras, COMO OBJETO, asi que dile la clase.
    public static void devolverValor() throws InterruptedException {
        latch = new CountDownLatch(1);
        DatabaseReference referenciaComp = ref.child("contador");
        referenciaComp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                Integer valor = ds.getValue(Integer.class);
                System.out.println("Valor del contador: " + valor);
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError de) {
                System.out.println("Fallo al leer el contador");
                latch.countDown();
            }
        });
        latch.await();
    }

    //Por mucho que me gustara este metodo, el altch solo va bien cuando hay un onCancelled
    //Así que no funciona. - si solo se llmase a este...
    public static void meterMovidas() throws InterruptedException {
        latch = new CountDownLatch(1);
        DatabaseReference datosRef = ref.child("datiyos");

        Map<String, Dato> mapaDatos = new HashMap<>();
        mapaDatos.put("dato11", new Dato(11, 2, 4));
        mapaDatos.put("Datos 23", new Dato(34, 2, 1));

        //Cambiando esto para que no fuese async, quizas...
        datosRef.setValueAsync(mapaDatos);
        //latch.countDown();
        System.out.println("Done");
        latch.await();
    }

    //Mete las movidas que quieras donde quieas
    public static void MeterMovidasConListener() throws InterruptedException {
        latch = new CountDownLatch(1);
        DatabaseReference datosRef = ref.child("datiyos");
        Map<String, Dato> mapaDatos = new HashMap<>();
        mapaDatos.put("dato12", new Dato(11, 2, 4));
        mapaDatos.put("Datos 33", new Dato(34, 2, 1));

        datosRef.setValue(mapaDatos, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError de, DatabaseReference dr) {
                System.out.println("metidos bien, phrasing!");
                latch.countDown();
            }
        });
        latch.await();

    }
    
    
    public static void updateChildren() throws InterruptedException{
        latch = new CountDownLatch(1);
        DatabaseReference referencia = ref.child("datiyos");
        Map<String, Object> update = new HashMap<>();
        update.put("Datos 2/temp", "tirentiayuno");

        referencia.updateChildren(update, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError de, DatabaseReference dr) {
                System.out.println("Finisima actualizacion");
                latch.countDown();
            }
        });
      latch.await();
    }
    
    //Vale, lee de la base de datos los hijos de "Dia/elDiaQueSea/Transporte"
    //Que basicamente se añaden cuando lso usuarios dicen como han ido al insti
    //SOLO COMO HAN IDO XIN
    //ESPERA, esto tiene que ir en el clietne de Android. - es solo para actualizar
    //datos
    public void actualizarTransporte(){   
    }
}

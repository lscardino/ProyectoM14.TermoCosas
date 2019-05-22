////Liberias
#include <ESP8266WiFi.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>
#include "DHT.h"

#define SEALEVELPRESSURE_HPA (1013.25)
#define DHTTYPE DHT22

////Internet
const char* ssid = "MiFibra-58CD";
const char* contra = "itdMY4Xj";

////Servicio
#define host "192.168.137.1"
//#define host "192.168.1.88"
const int port = 20003;

////Definir los pin de entrada y salida
const int pinAnemometro = 0;
const int pinLed = 2;
const int pinDHT = 14;

////Declaracion de objetos
Adafruit_BME280 bme;
DHT dht(pinDHT,DHTTYPE);
WiFiClient client;

//Varibles
float temperatura,humedad,presion,altitud;
float t,h,hif;
float velocidad = 0;
float lluvia = 0;
float polvo = 0;

//Tiempo que espera para poder ver los datos
long esperaTiempo = 5000;
long tiempoEsperado = 0;
long tiempoAntes;
  void setup() {
    Serial.begin(115200);
    delay(10);
    
    bme.begin(0x76);
    dht.begin();
    
    pinMode(pinAnemometro, INPUT_PULLUP);//INPUT);
    pinMode(pinLed, OUTPUT);//INPUT);
    
    //Configurar WiFi
    WiFi.mode(WIFI_STA);// modo cliente wifi
    conectarWiFi();
    
    Serial.println("");
    Serial.println(WiFi.localIP());

    //Conectar con el servido
    conectarServidor();
    
    Serial.print("Tiempo Actual    " ); Serial.println(tiempoAntes);
    Serial.print("Tiempo a esperar " ); Serial.println(esperaTiempo);
    Serial.print("Resultado Tiempo " ); Serial.println(tiempoEsperado);
    
    //listener del Anemometro
    attachInterrupt(digitalPinToInterrupt(pinAnemometro), interrupcionViento, RISING);
    //attachInterrupt(digitalPinToInterrupt(), interrupcionLluvia, RISING);
  }

  void loop() {
    if(client.connected()){
        if(millis() > tiempoEsperado){
          miraTemBME280();
          miraTemDHT();
          miraPolvo();
          Serial.println();
          Serial.print("Velocidad ms/rad :"); Serial.println(velocidad);
          Serial.println("-  -  -  -  -  -  -  -  -  -  -  -");
          
          client.println("0000");
          delay(5);
          enviarDatos();
          tiempoEsperado = esperaTiempo + millis();
          reiniciarVariables();
        }
        /*else{
          delay(100);
          numEspera += 100;
        }*/
    }else{
      Serial.println("ERROR - Servidor caido!");
      client.stop();
      delay(5555);
      //Conectar con el servido
      conectarServidor();
    }
    if(WiFi.status()!= WL_CONNECTED){
      Serial.println("ERROR - Fallo WiFi");
      delay(5555);
      conectarWiFi();
    }
    
    delay(5);
  }

  void conectarWiFi(){
    long tiempoReintentar = millis() + 30000;
    WiFi.begin(ssid,contra);
    while(WiFi.status()!= WL_CONNECTED){
      if(millis() >= (tiempoReintentar)){
        Serial.println();
        WiFi.begin(ssid,contra);
        tiempoReintentar = millis() + 30000;
      }
      digitalWrite(pinLed, LOW);
      delay(250);
      Serial.print(".");
      
      digitalWrite(pinLed, HIGH);
      delay(250);
    }
  }
  
  void conectarServidor(){
    while(!client.connect(host,port)){
      digitalWrite(pinLed, LOW);
      Serial.println("ERROR - Fallo Conexión Servidor");
      delay(500);
      avisoLed(50,1);
    }
    digitalWrite(pinLed, HIGH);
    Serial.println("INFO - Conectado");

    delay(5);
    
    //envia un numero para recibir el numero de espera;
    digitalWrite(pinLed, LOW);
    client.println("1111");
    delay(5);
    digitalWrite(pinLed, HIGH);
    
    digitalWrite(pinLed, LOW);
    char skipChar[50] ;
    client.readStringUntil('\r').toCharArray(skipChar,50);
    esperaTiempo = atol(skipChar);
    digitalWrite(pinLed, HIGH);

    tiempoAntes = millis();
    tiempoEsperado = esperaTiempo + tiempoAntes;
  }

  void miraTemDHT(){
    h = dht.readHumidity();
    t = dht.readTemperature();
    hif = dht.computeHeatIndex(t,h, false);
  
    if (isnan(h) || isnan(t)) {
        Serial.println("ERROR - Fallo sensor DHT22");
        avisoLed(50,3);
        h = -100;
        t = -100;
    }else{
        Serial.println();
        Serial.println("DHT22");
        Serial.print("Temperatura :");  Serial.print(t);  Serial.println("ºC");
        Serial.print("Humedad :");  Serial.print(h);  Serial.println("%");
        Serial.print("Sensación termica: ");  Serial.println(hif);
        
      }
  }

  void miraTemBME280(){
    temperatura = bme.readTemperature();
    humedad = bme.readHumidity();
    presion = bme.readPressure() / 100.0F;
    //Altitud es un calculo, se puede ahorrar
    altitud = bme.readAltitude(SEALEVELPRESSURE_HPA);

    if(isnan(temperatura) || isnan(humedad) || isnan(presion)){
      Serial.println("ERROR - Fallo sensor BME280");
      avisoLed(50,3);
    }else{
      Serial.println();
      Serial.println("BME280");
      Serial.print("Temperatura :"); Serial.print(temperatura); Serial.println("ºC");
      Serial.print("Humedad :"); Serial.print(humedad); Serial.println("%");
      Serial.print("Presión :"); Serial.println(presion);
      Serial.print("Altitud :"); Serial.println(altitud);
    }
    
  }

  void miraPolvo(){
    
  }

  void interrupcionViento(){
    //NOTA_PROFE -> Mira el tiempo que tarda en dar la vuelta
    Serial.println("Sensor Magnetico");

    velocidad = (millis()-tiempoAntes);
    tiempoAntes = millis();
    
  }

  void interrupcionLluvia(){
    
  }

  void avisoLed(int tiempo, int parpadeo){
      digitalWrite(pinLed, HIGH);
    for(int num = 0 ; num < parpadeo; num++){
      delay(tiempo);
      digitalWrite(pinLed, LOW);
      delay(tiempo);
      digitalWrite(pinLed, HIGH);
    }
  }

  void reiniciarVariables(){
    /*
float temperatura,humedad,presion,altitud;
float t,h,hif;*/
    velocidad = 0;
    lluvia = 0;
    tiempoAntes = millis();
  }

  void enviarDatos(){
    Serial.println("INFO - Enviar Datos");
    digitalWrite(pinLed, LOW);
    //String UT8

    client.println(temperatura);
    client.println(humedad);
    client.println(presion);
    //client.println(altitud);
    client.println(t);
    client.println(h);
    client.println(velocidad);
    client.println(lluvia);
    client.println(polvo);
    client.println(hif);
    /*
     //DataInputStream
    client.println("- - - - - - -- ");
    client.print("hola");
    client.println("hola");
    //String UT8
    client.print(1);
    client.println(2);
    client.println(123);
    client.println(12.3f);
    //String UTF8 and numer Byte
    client.write(12);
    client.write("1");
    //client.readStringUntil('\r');*/
    digitalWrite(pinLed, HIGH);
  }

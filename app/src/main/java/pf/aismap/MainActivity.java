package pf.aismap;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener {

    //declaraciones del mapa
    GoogleMap mGoogleMap;
    Marker propio;
    int unciclo2 = 1;
    ArrayList<Marker> markers = new ArrayList<Marker>();
    int unciclo = 1;
    private Circle circle;
    private Circle circle2;
    private Circle circle3;
    private Circle circle4;
    private Circle circle5;


    //declaraciones del TCP
    private TCPClient mTcpClient;


    //declaraciones de decodificacion
    String salir = "no";
    String sentenciaEntra;
    String[] sentencia_separada;
    int a, b, k = 1;
    String casipayload = "", payload1 = "", continuar = "no";
    String payload, paquete;
    String[] PARAMETROS;
    double lt, lg;


    //declaraciones del bluetooth
    private static final String TAG = "aismap";
    Handler h;
    final int RECIEVE_MESSAGE = 1;  // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private tarea2 mtarea2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");      // SPP UUID service
    private static String address = "00:1C:97:10:DE:B4";  // MAC-address of Bluetooth module (you must edit this line)id del ds
    private boolean mRun = false;
    BufferedReader in;
    private String serverMessage;
    InputStream mmInStream;
    OutputStream mmOutStream;
    StringBuilder este = new StringBuilder("");


    //declaracion para envio al servidor
    UDPsender mUDPsender;
    static String msg1 = "";


    //boton de seguimiento de ubicación
    String cambio = "noundido";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);      //Mantener la pantalla en orientacion portrait
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //Mantener la pantalla encendida

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.icono);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            initMap();
        } else { //no muestra el mapa
        }


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:// if receive massage
                        try {
                            String strIncom = (String) msg.obj;
                            sb.append(strIncom);// append string
                            preseguir(strIncom);
                            sb.delete(0, sb.length());// and clear
                        } catch (Exception e) {
                            Log.e(TAG, "hay error", e);
                        }
                        break;
                }
            }

            ;
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();   // get Bluetooth adapter
        checkBTState();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menubttom) {
            return true;
        } else if (id == R.id.bulocation) {

            /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return TODO;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);



            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double s = location.getSpeed();
            double curs = location.getBearing();
            long epoch = System.currentTimeMillis()/1000;
            String time = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));
            String[] dat = {"-","-", "-", "000000001", "NA", String.valueOf(longitude), String.valueOf(latitude),
                    String.valueOf(s), String.valueOf(curs), "El Perla Negra","Passenger","3.0",time};
            setmarkerpropio(latitude,longitude, dat);

            */
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return TODO;
            }

            boolean gps_enabled = false;
            boolean network_enabled = false;


            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Location net_loc = null, gps_loc = null, finalLoc = null;

            if (gps_enabled)
                gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled)
                net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gps_loc != null && net_loc != null) {

                //smaller the number more accurate result will
                if (gps_loc.getAccuracy() > net_loc.getAccuracy()){
                    mensajeEnPantalla("Localizacion mas precisa GPS ");
                    finalLoc = net_loc;}

                else
                    finalLoc = gps_loc;
                    mensajeEnPantalla("Localizacion mas precisa GPS ");
                // I used this just to get an idea (if both avail, its upto you which you want to take as I've taken location with more accuracy)

            } else {

                if (gps_loc != null) {
                    finalLoc = gps_loc;
                } else if (net_loc != null) {
                    finalLoc = net_loc;
                }
            }


            double longitude = finalLoc.getLongitude();
            double latitude = finalLoc.getLatitude();
            double s = finalLoc.getSpeed();
            double curs = finalLoc.getBearing();
            long epoch = System.currentTimeMillis()/1000;
            String time = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));
            String[] dat = {"-","-", "-", "000000001", "NA", String.valueOf(longitude), String.valueOf(latitude),String.valueOf(s), String.valueOf(curs), "El Perla Negra","Passenger","3.0",time};
             setmarkerpropio(latitude,longitude, dat);

            if(propio!=null){

                if (cambio.equals("undido")){
                    cambio="noundido";
                    mensajeEnPantalla("Modo Navegacion Desactivado");
                    item.setIcon(R.drawable.locationbuttom);
                    propio.setVisible(false);
                    //icono no undido
                }else{
                    cambio="undido";
                    mensajeEnPantalla("Modo Navegacion Activado");
                    item.setIcon(R.drawable.locationbuttomon);
                    //icono undido
                    LatLng ll = propio.getPosition();
                    goToLocationZoom(ll.latitude,ll.longitude,15);
                }
            }else{
                Log.d("error:","no existe...");
                mensajeEnPantalla("No existe la posicion actual");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //seguir despues de recibir la sentencia de bluetooh
    private void preseguir(String Sentencia){
        sentenciaEntra=Sentencia;
        Log.i("inter",sentenciaEntra);
        if (Filtro(sentenciaEntra).equals("aceptada")) { //si es aceptada es AIVDM O GPRMC
            sentencia_separada = separar(sentenciaEntra, ",");
            if (sentencia_separada[0].equals("!AIVDM")){
                decodificarAivdm();
            }else if (sentencia_separada[0].equals("$GPRMC")){
                decodificarGprmc();
            }
        }
    }
    private void decodificarAivdm (){
        a = Integer.parseInt(sentencia_separada[1]);//total de fragmentos
        b = Integer.parseInt(sentencia_separada[2]);//fragmento numero tal
        if (a == b && b == k) {
            casipayload = casipayload + sentencia_separada[5];
            payload1 = casipayload;
            continuar = "si";
            k = 1;
            casipayload = "";
        } else if ((a > 1) && (b == k)) {
            casipayload = casipayload + sentencia_separada[5];
            k++;
        } else if (a == 1 && b == 1) {    //sino es la que se espera pero es 1,1
            payload1 = sentencia_separada[5];
            continuar = "si";
            casipayload = "";
            k = 1;
        } else if (a > 1 && b == 1) {
            casipayload = "";
            casipayload = casipayload + sentencia_separada[5];
            k = 2;
        }
        if ("si".equals(continuar)) {
            payload = payload1;                 //mensaje a decodificar
            paquete = sentencia_separada[0];    //indica que es AIVDM
            //se procede a decodificar
            Log.i("inter",payload);
            PARAMETROS = InterpretarPayload(paquete, payload);  //Se obtienen los datos en el vector PARAMETROS

            if ((PARAMETROS[2].equals("1") || PARAMETROS[2].equals("2") || PARAMETROS[2].equals("3")
                    || PARAMETROS[2].equals("18") || PARAMETROS[2].equals("19") )) {
                //SI EL TIPO ES 1,2,3,18 o 19

                lg = Double.parseDouble(PARAMETROS[5]);
                lt = Double.parseDouble(PARAMETROS[6]);

                if ((lg!=181)&&(lt!=91)&&(lg!=0)&&(lt!=0)){   //posicion valida para el mapa

                    if (unciclo==1){
                        setMarker(lt,lg,PARAMETROS);
                        unciclo=0;
                        String[] temp24 = {PARAMETROS[3], PARAMETROS[4] ,PARAMETROS[5] , PARAMETROS[6], PARAMETROS[7],
                                PARAMETROS[8] , PARAMETROS[9],PARAMETROS[10],PARAMETROS[11]};
                        enviardata(temp24);
                    }else{

                        String X = PARAMETROS[3];
                        int i = 0;
                        String salir2="no";
                        do {
                            try {
                                if (X.equals(markers.get(i).getTag())) {  //si ya hay un marcador con ese mmsi
                                    salir2 = "si";
                                }
                                i++;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        } while (i < markers.size() && (salir2.equals("no")));

                        if(salir2.equals("si")){
                            //si el marcador ya existe ese MMsi entonces modifico sus parametros
                            try {
                                markers.get(i - 1).setPosition(new LatLng(lt, lg));
                                String[] temp =  separar((markers.get(i - 1).getSnippet()),",");
                                markers.get(i - 1).setSnippet(temp[0]+","+temp[1]+","+PARAMETROS[4]+","+PARAMETROS[7]+","
                                        +PARAMETROS[8]+","+temp[5]+","+PARAMETROS[6]+","+PARAMETROS[5]+","+PARAMETROS[3]+","+PARAMETROS[12]);

                                if (!PARAMETROS[8].equals("360.0") && !PARAMETROS[8].equals("511.0")) {
                                    markers.get(i - 1).setRotation(Float.parseFloat(PARAMETROS[8]));
                                }

                                String[] temp2 ={PARAMETROS[3],PARAMETROS[4],PARAMETROS[5],PARAMETROS[6],
                                PARAMETROS[7],PARAMETROS[8],temp[0],temp[1],temp[5]};
                                enviardata(temp2);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            if(markers.get(i -1).isInfoWindowShown()){
                                markers.get(i -1).hideInfoWindow();
                                markers.get(i -1).showInfoWindow();
                            }

                        }else{
                            setMarker(lt,lg,PARAMETROS);
                            String[] temp25 = {PARAMETROS[3], PARAMETROS[4] ,PARAMETROS[5] , PARAMETROS[6], PARAMETROS[7],
                                    PARAMETROS[8] , PARAMETROS[9],PARAMETROS[10],PARAMETROS[11]};
                            enviardata(temp25);
                        }
                    }
                }
            }else if (PARAMETROS[2].equals("5") || PARAMETROS[2].equals("24")){
                String X = PARAMETROS[3];
                int i = 0;
                String salir2="no";
                do {
                    try {
                        if (X.equals(markers.get(i).getTag())) {
                            salir2 = "si";
                        }
                        i++;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } while (i < markers.size() && (salir2.equals("no")));

                if(salir2.equals("si")){
                    //si el marcador ya existe ese MMsi entonces modifico sus parametros
                    try {
                        String[] temp = separar(markers.get(i - 1).getSnippet(),",");
                        if (PARAMETROS[2].equals("24")){
                            if (PARAMETROS[4].equals("0")){
                                temp[0]=PARAMETROS[9];
                            }else if (PARAMETROS[4].equals("1")){
                                temp[1]=PARAMETROS[10];
                            }

                        }else{ //esto para los tipo 5
                            temp[0]=PARAMETROS[9];
                            temp[1]=PARAMETROS[10];
                            temp[5]=PARAMETROS[11];
                        }

                        String barcoTipo = reversarTipo(temp[1]);
                        switch (barcoTipo){
                            case "tanker":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowrojom)); break;
                            case "tugAndSpecial":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowazulm)); break;
                            case "cargo":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowverdem)); break;
                            case "unspecified":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowgrism)); break;
                            case "passenger":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowazuloscm)); break;
                            case "hsc":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowamarillom)); break;
                            case "pleasure":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowrosadom)); break;
                            case "fishing":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowpielm)); break;
                            //case "propio":  markers.get(i - 1).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.arrowcel)); break;
                        }

                        markers.get(i - 1).setSnippet(temp[0]+","+temp[1]+","+temp[2]+","+temp[3]+","
                                +temp[4]+","+temp[5]+","+temp[6]+","+temp[7]+","+temp[8]+","+temp[9]);

                        if (!temp[4].equals("360.0") && !temp[4].equals("511.0")) {
                            markers.get(i - 1).setRotation(Float.parseFloat(temp[4]));
                        }

                        String[] temp3 ={temp[8],temp[2],temp[7],temp[6],temp[3],temp[4],temp[0],temp[1],temp[5]};
                        enviardata(temp3);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(markers.get(i -1).isInfoWindowShown()){
                        markers.get(i -1).hideInfoWindow();
                        markers.get(i -1).showInfoWindow();
                    }
                }

            }else if (PARAMETROS[2].equals("4")){

                lg = Double.parseDouble(PARAMETROS[5]);
                lt = Double.parseDouble(PARAMETROS[6]);

                if ((lg!=181)&&(lt!=91)&&(lg!=0)&&(lt!=0)) {
                    if (unciclo == 1) {
                        setmarkerestacion(lt, lg, PARAMETROS);
                        unciclo = 0;
                        String[] temp24 = {PARAMETROS[3], PARAMETROS[4], PARAMETROS[5], PARAMETROS[6], PARAMETROS[7],
                                PARAMETROS[8], PARAMETROS[9], PARAMETROS[10], PARAMETROS[11]};
                        enviardata(temp24);
                    } else {
                        String X = PARAMETROS[3];
                        int i = 0;
                        String salir2 = "no";
                        do {
                            try {
                                if (X.equals(markers.get(i).getTag())) {  //si ya hay un marcador con ese mmsi se borra
                                    salir2 = "si";
                                }
                                i++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } while (i < markers.size() && (salir2.equals("no")));

                        if (salir2.equals("si")) {
                            //si el marcador ya existe ese MMsi entonces modifico sus parametros
                            try {
                                markers.get(i - 1).setPosition(new LatLng(lt, lg));
                                String[] temp = separar((markers.get(i - 1).getSnippet()), ",");
                                markers.get(i - 1).setSnippet(temp[0] + "," + temp[1] + "," + PARAMETROS[4] + "," + PARAMETROS[7] + ","
                                        + PARAMETROS[8] + "," + temp[5] + "," + PARAMETROS[6] + "," + PARAMETROS[5] + "," + PARAMETROS[3]+","+PARAMETROS[12]);

                                String[] temp2 = {PARAMETROS[3], PARAMETROS[4], PARAMETROS[5], PARAMETROS[6],
                                        PARAMETROS[7], PARAMETROS[8], temp[0], temp[1], temp[5]};
                                enviardata(temp2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (markers.get(i - 1).isInfoWindowShown()) {
                                markers.get(i - 1).hideInfoWindow();
                                markers.get(i - 1).showInfoWindow();
                            }
                        } else {
                            setmarkerestacion(lt, lg, PARAMETROS);
                            String[] temp25 = {PARAMETROS[3], PARAMETROS[4], PARAMETROS[5], PARAMETROS[6], PARAMETROS[7],
                                    PARAMETROS[8], PARAMETROS[9], PARAMETROS[10], PARAMETROS[11]};
                            enviardata(temp25);
                        }
                    }
                }
            }
            continuar = "no";
        }
    }
    private String reversarTipo(String X) {
        String resultado="";

        switch (X){
            //70-79
            case "Cargo": resultado="cargo";break;
            case "Cargo - Hazardous category A": resultado="cargo";break;
            case "Cargo - Hazardous category B": resultado="cargo";break;
            case "Cargo - Hazardous category C": resultado="cargo";break;
            case "Cargo - Hazardous category D": resultado="cargo";break;
            case "Cargo - RFU": resultado="cargo";break;

            //80 -89
            case "Tanker":resultado="tanker";break;
            case "Tanker - Hazardous category A":resultado="tanker";break;
            case "Tanker - Hazardous category B":resultado="tanker";break;
            case "Tanker - Hazardous category C":resultado="tanker";break;
            case "Tanker - Hazardous category D":resultado="tanker";break;
            case "Tanker - RFU":resultado="tanker";break;

            //60-69
            case "Passenger":resultado="passenger";break;
            case "Passenger - Hazardous category A":resultado="passenger";break;
            case "Passenger - Hazardous category B":resultado="passenger";break;
            case "Passenger - Hazardous category C":resultado="passenger";break;
            case "Passenger - Hazardous category D":resultado="passenger";break;
            case "Passenger - RFU":resultado="passenger";break;

            //40-49
            case "High speed craft":resultado="hsc";break;
            case "High speed craft - Hazardous category A":resultado="hsc";break;
            case "High speed craft - Hazardous category B":resultado="hsc";break;
            case "High speed craft - Hazardous category C":resultado="hsc";break;
            case "High speed craft - Hazardous category D":resultado="hsc";break;
            case "High speed craft - RFU":resultado="hsc";break;

            //50-59    31,32,33,34,35,38,39
            case "Pilot Vessel":resultado="tugAndSpecial";break;
            case "Search and Rescue vessel":resultado="tugAndSpecial";break;
            case "Tug":resultado="tugAndSpecial";break;
            case "Port Tender":resultado="tugAndSpecial";break;
            case "Anti-pollution equipment":resultado="tugAndSpecial";break;
            case "Law Enforcement":resultado="tugAndSpecial";break;
            case "Spare - Local Vessel":resultado="tugAndSpecial";break;
            case "Medical Transport":resultado="tugAndSpecial";break;
            case "Noncombatant ship according to RR Resolution No.18":resultado="tugAndSpecial";break;
            case "Towing":resultado="tugAndSpecial";break;
            case "Towing: length exceeds 200m or breadth exceeds 25m":resultado="tugAndSpecial";break;
            case "Dredging or underwater ops":resultado="tugAndSpecial";break;
            case "Diving ops":resultado="tugAndSpecial";break;
            case "Military ops":resultado="tugAndSpecial";break;
            case "Reserved":resultado="tugAndSpecial";break;

            //30
            case "Fishing":resultado="fishing";break;

            //36-37
            case "Sailing":resultado="pleasure";break;
            case "Pleasure Craft":resultado="pleasure";break;

            //0-29  90-99
            default:resultado="unspecified";break;

        }
        return resultado;
    }
    private void decodificarGprmc(){
    try {
        if (sentencia_separada[2].equals("A")) {
            double d = Double.valueOf(sentencia_separada[3]);
            d = d / 100;
            int degr = (int) d;
            d = (d - degr) / .60;
            d = degr + d;
            if (sentencia_separada[4].equals("S")) {
                d = d * (-1);
            }
            double lati = d;

            //sacando longitud
            d = Double.valueOf(sentencia_separada[5]);
            d = d / 100;
            degr = (int) d;
            d = (d - degr) / .60;
            d = degr + d;
            if (sentencia_separada[6].equals("W")) {
                d = d * (-1);
            }
            double longi = d;

            //sacando velocidad
            d = Double.valueOf(sentencia_separada[7]);
            double velo = d;

            //sacando curso
            d = Double.valueOf(sentencia_separada[8]);   //course over ground
            double curs = d;

            long epoch = System.currentTimeMillis()/1000;
            String time = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));

            //poner marcador
            String[] dat = {"-","-", "-", "000000001", "NA", String.valueOf(longi), String.valueOf(lati),
                    String.valueOf(velo), String.valueOf(curs), "Poseidon","Passenger","3.0",time};

            Log.d("marcador propio","lati" + lati + "long:" + longi);
            if (unciclo2==1) {
                Log.d("marcador propio","lati" + lati + "long:" + longi);
                setmarkerpropio(lati, longi, dat);
                unciclo2=0;

                CircleOptions copt= new CircleOptions()
                        .center(new LatLng(lati, longi))
                        .radius(1000)
                        .strokeColor(Color.GRAY)
                        .strokeWidth(5);

                circle = mGoogleMap.addCircle(copt);
                circle2 = mGoogleMap.addCircle(copt);
                circle2.setRadius(2000);
                circle3 = mGoogleMap.addCircle(copt);
                circle3.setRadius(3000);
                circle4 = mGoogleMap.addCircle(copt);
                circle4.setRadius(4000);
                circle5 = mGoogleMap.addCircle(copt);
                circle5.setRadius(5000);

            }else{


                propio.setPosition(new LatLng(lati, longi));
                propio.setSnippet(dat[9]+","+dat[10]+","+dat[4]+","+dat[7]+","
                        +dat[8]+","+dat[11]+","+dat[6]+","+dat[5]+","+dat[3]+","+dat[12]);
                if (!dat[8].equals("360.0") && !dat[8].equals("511.0")){
                    propio.setRotation(Float.parseFloat(dat[8]));
                }

                if(propio.isInfoWindowShown()){
                    propio.hideInfoWindow();
                    propio.showInfoWindow();
                }

                circle.setCenter(new LatLng(lati, longi));
                circle2.setCenter(new LatLng(lati, longi));
                circle3.setCenter(new LatLng(lati, longi));
                circle4.setCenter(new LatLng(lati, longi));
                circle5.setCenter(new LatLng(lati, longi));

                if (cambio.equals("undido")){
                    goToLocation(lati,longi);
                }
            }

            String[] temp33={dat[3],dat[4],dat[5],dat[6],dat[7],dat[8],dat[9],dat[10],dat[11]};
            enviardata(temp33);

            Log.i("decodificado","--------------------------------------");
            Log.i("decodificado","Paquete= " + "!GPRMC");

            Log.i("decodificado","Longitud= "+ longi);
            Log.i("decodificado","Latitud= "+ lati);
            Log.i("decodificado","Velocidad= "+ velo);
            Log.i("decodificado","Curso COG= "+ curs);

            Log.i("decodificado","Recibido= "+ time);
            Log.i("decodificado","--------------------------------------");

        }
    }catch (Exception e){
        e.printStackTrace();
        Log.d("marcador propio","error");
    }


}

    //enviar el servidor
    private void enviardata(String[] X){
    msg1= X[0] + "," + X[1]+ "," + X[2] + "," + X[3]
            + "," + X[4] + "," + X[5] + "," + X[6]+ "," + X[7]+ "," + X[8];

       mUDPsender = new UDPsender();
        mUDPsender.start();

}


    //Conexion por Bluetooh
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            mensajeEnPantalla("Falla interna");
            //finish();
           // errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");

        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
            mensajeEnPantalla("Conexion a equipo exitosa");
        } catch (IOException e) {
            /*try {
                btSocket.close();
                mensajeEnPantalla("Conexion a equipo fallida - Se forzo el cierre de la aplicacion");
                finish();
            } catch (IOException e2) {
                mensajeEnPantalla("Conexion a equipo fallida - Se forzo el cierre de la aplicacion");
                finish();
                //errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }*/
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mtarea2= new tarea2() ;
        mtarea2.execute();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "...In onPause()...");
        /*try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }*/
    }
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        //finish();
    }

    @Override

    //CAMBIE location por finalLoc
    public void onLocationChanged(Location finalLoc) {

        double longitude = finalLoc.getLongitude();
        double latitude = finalLoc.getLatitude();
        double s = finalLoc.getSpeed();
        double curs = finalLoc.getBearing();
        long epoch = System.currentTimeMillis()/1000;
        String time = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));
        String[] dat = {"-","-", "-", "000000001", "NA", String.valueOf(longitude), String.valueOf(latitude),String.valueOf(s), String.valueOf(curs), "El Perla Negra","Passenger","3.0",time};
        setmarkerpropio(latitude,longitude, dat);


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    private class tarea2 extends AsyncTask<BluetoothSocket,Integer,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(BluetoothSocket... param) {
            mRun = true;
            try {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                try{
                    in = new BufferedReader(new InputStreamReader(btSocket.getInputStream()));
                    int byt;
                    while (mRun) {
                        serverMessage = in.readLine();

                        if (serverMessage != null ) {
                            byt=serverMessage.length();
                            //va llamada handler
                            h.obtainMessage(RECIEVE_MESSAGE, byt, -1, serverMessage).sendToTarget();

                        }
                        serverMessage = null;
                    }
                    Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                }catch (Exception e) {
                    Log.e("TCP", "S: Error", e);
                }
                //finally {btSocket.close();}

            }catch (Exception e) {
                Log.e("TCP", "C: Error", e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

    }


    //Funciones de Decodificacion
    private static String[] InterpretarPayload(String Paquete, String Payload){
        String[] resultado = new String[13];

                String TEMP2,TEMP3;
                String bitpay;
                int temp;
                String TIPO;
                float temp3;
                String prec="-----";
                String time="-----";

                bitpay = ASCIItoSixbit(Payload);
                DecimalFormat numberformat = new DecimalFormat("#.00000");

                TEMP2 = bitpay.substring(0,6);
                temp = Integer.parseInt(TEMP2, 2);
                TIPO = Integer.toString(temp);


                if ((TIPO.equals("1"))||(TIPO.equals("2"))||(TIPO.equals("3"))){
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;

                    //precision posicion
                    TEMP2 = bitpay.substring(60,61);//para sacar lña posicion
                    if (TEMP2.equals("1")){
                        prec= "< 10m";
                    }else{
                        prec= "> 10m";
                    }

                    //------------time
                    //TEMP2 = bitpay.substring(137,143);
                    //temp = Integer.parseInt(TEMP2, 2);
                    //time=Integer.toString(temp);

                    //------------MMSI obtener el MMS!
                    TEMP2 = bitpay.substring(8,38);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[3]=Integer.toString(temp);

                    //------------Navigation Status
                    TEMP2 = bitpay.substring(38,42);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[4] = TablaNavigationStatus(temp);

                    //------------longitud
                    TEMP2 = bitpay.substring(61,89);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[5]= numberformat.format(temp3);
                    //resultado[5] = Float.toString(temp3);

                    //------------latitud
                    TEMP2 = bitpay.substring(89,116);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[6] = numberformat.format(temp3);
                    //resultado[6] = Float.toString(temp3);

                    //-----------speed  SOG
                    TEMP2 = bitpay.substring(50,60);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[7] = Float.toString((temp3/10));

                    if (resultado[7].equals("0.0")){
                        if (resultado[4].equals("Under way using engine")){
                            resultado[4]="Stopped";
                        }
                    }

                    //---------course COG
                    TEMP2 = bitpay.substring(116,128);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[8] = Float.toString((temp3/10));

                    //True Heading (HDG)
                    //TEMP2 = bitpay.substring(128,137);
                    //temp3=binariotoFloatSinSigno(TEMP2);
                    //resultado[8] = Float.toString((temp3));

                    //----------shipname
                    resultado[9] = "-------";

                    //----------vesseltype
                    resultado[10]="-------";

                    //----------calado
                    resultado[11]="0.0";


                }else if(TIPO.equals("4")){
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;

                    //------------MMSI
                    TEMP2 = bitpay.substring(8,38);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[3]=Integer.toString(temp);

                    //------------Navigation Status
                    resultado[4] = "-------";

                    //precision posicion
                    TEMP2 = bitpay.substring(78,79);
                    if (TEMP2.equals("1")){
                        prec= "< 10m";
                    }else{
                        prec= "> 10m";
                    }

                    //------------longitud
                    TEMP2 = bitpay.substring(79,107);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[5] = numberformat.format(temp3);
                    //resultado[5] = Float.toString(temp3);

                    //------------latitud
                    TEMP2 = bitpay.substring(107,134);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[6] = numberformat.format(temp3);
                    //resultado[6] = Float.toString(temp3);

                    //-----------speed  SOG
                    //TEMP2 = bitpay.substring(46,56);
                    //temp3=binariotoFloatSinSigno(TEMP2);
                    //resultado[7] = Float.toString((temp3/10));
                    resultado[7]="0.0";

                    //---------course COG
                    //TEMP2 = bitpay.substring(112,124);
                    //temp3=binariotoFloatSinSigno(TEMP2);
                    //resultado[8] = Float.toString((temp3/10));
                    resultado[8]="0.0";

                    //----------shipname
                    resultado[9] = "-------";

                    //----------vesseltype
                    resultado[10]="Estacion";

                    //----------calado
                    resultado[11]="0.0";

                }else if(TIPO.equals("5")){
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;
                    //Log.i("inter","entro a tipo 5");
                    //Log.i("inter","bitpay= "+bitpay);


                    //------------MMSI
                    TEMP2 = bitpay.substring(8,38);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[3]=Integer.toString(temp);
                    //Log.i("inter","hizo mmsi");

                    resultado[4]="-------";
                    resultado[5]="0.0";
                    resultado[6]="0.0";
                    resultado[7]="0.0";
                    resultado[8]="0.0";


                    //----------shipname
                    TEMP2 = bitpay.substring(112,232); //20 caracteres de 6 bit
                    TEMP3=binarioSixbittoASCII(TEMP2);
                    Log.i("inter","nombre de tipo 5"+TEMP3);
                    resultado[9] = TEMP3;

                    //------------vesseltype
                    TEMP2 = bitpay.substring(232,240);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[10]=TablaVesselType(temp);
                    Log.i("inter","vesseltype tipo 5="+resultado[10]);

                    //------------calado
                    TEMP2 = bitpay.substring(294,302);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[11] = Float.toString((temp3/10));
                    Log.i("inter","calado de tipo 5="+resultado[11]);


                }else if(TIPO.equals("24")){
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;

                    //------------MMSI
                    TEMP2 = bitpay.substring(8,38);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[3]=Integer.toString(temp);


                    resultado[5]="0.0";
                    resultado[6]="0.0";
                    resultado[7]="0.0";
                    resultado[8]="0.0";

                    //part number
                    TEMP2 = bitpay.substring(38,40);
                    temp = Integer.parseInt(TEMP2, 2);


                    resultado[4]=Integer.toString(temp);;  //utilizo esta casilla de nav status para indicar si es  A o B solo para este tipo 24
                    //0 es part A       1 is B     lo demas no
                    if(temp==0){
                        //----------shipname
                        TEMP2 = bitpay.substring(40,160); //20 caracteres de 6 bit
                        TEMP3=binarioSixbittoASCII(TEMP2);
                        resultado[9] = TEMP3;
                        Log.i("inter","shipname tipo24="+resultado[9]);

                        //----------vesseltype
                        resultado[10]="-------";

                        //----------calado
                        resultado[11]="0.0";

                    }else if(temp==1){
                        //----------shipname
                        resultado[9] = "-------";

                        //------------vesseltype
                        TEMP2 = bitpay.substring(40,48);
                        temp = Integer.parseInt(TEMP2, 2);
                        resultado[10]=TablaVesselType(temp);
                        Log.i("inter","vesseltype tipo24="+resultado[10]);

                        //----------calado
                        resultado[11]="0.0";

                    }else{
                        //----------shipname
                        resultado[9] = "-------";

                        //----------vesseltype
                        resultado[10]="-------";

                        //----------calado
                        resultado[11]="0.0";
                    }


                }else if(TIPO.equals("18")){
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;

                    //------------MMSI
                    TEMP2 = bitpay.substring(8,38);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[3]=Integer.toString(temp);

                    //------------Navigation Status
                    resultado[4] = "-------";

                    //precision posicion
                    TEMP2 = bitpay.substring(56,57);
                    if (TEMP2.equals("1")){
                        prec= "< 10m";
                    }else{
                        prec= "> 10m";
                    }


                    //------------longitud
                    TEMP2 = bitpay.substring(57,85);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[5] = numberformat.format(temp3);
                    //resultado[5] = Float.toString(temp3);

                    //------------latitud
                    TEMP2 = bitpay.substring(85,112);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[6] = numberformat.format(temp3);
                    //resultado[6] = Float.toString(temp3);

                    //-----------speed  SOG
                    TEMP2 = bitpay.substring(46,56);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[7] = Float.toString((temp3/10));

                    //---------course COG
                    TEMP2 = bitpay.substring(112,124);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[8] = Float.toString((temp3/10));

                    //True Heading (HDG)
                    //TEMP2 = bitpay.substring(124,133);
                    //temp3=binariotoFloatSinSigno(TEMP2);
                    //resultado[8] = Float.toString((temp3));

                    //----------shipname
                    resultado[9] = "-------";

                    //----------vesseltype
                    resultado[10]="-------";

                    //----------calado
                    resultado[11]="0.0";


                }else if(TIPO.equals("19")){
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;

                    //------------MMSI
                    TEMP2 = bitpay.substring(8,38);
                    temp = Integer.parseInt(TEMP2, 2);
                    resultado[3]=Integer.toString(temp);

                    //------------Navigation Status
                    resultado[4] = "-------";

                    //precision posicion
                    TEMP2 = bitpay.substring(56,57);
                    if (TEMP2.equals("1")){
                        prec= "< 10m";
                    }else{
                        prec= "> 10m";
                    }

                    //------------longitud
                    TEMP2 = bitpay.substring(57,85);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[5] = numberformat.format(temp3);
                    //resultado[5] = Float.toString(temp3);

                    //------------latitud
                    TEMP2 = bitpay.substring(85,112);
                    temp3=binariotoFloatconSigno(TEMP2);
                    temp3 = temp3 / 600000.0f;
                    resultado[6] = numberformat.format(temp3);
                    //resultado[6] = Float.toString(temp3);

                    //-----------speed  SOG
                    TEMP2 = bitpay.substring(46,56);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[7] = Float.toString((temp3/10));

                    //---------course COG
                    TEMP2 = bitpay.substring(112,124);
                    temp3=binariotoFloatSinSigno(TEMP2);
                    resultado[8] = Float.toString((temp3/10));

                    //True Heading (HDG)
                    //TEMP2 = bitpay.substring(124,133);
                    //temp3=binariotoFloatSinSigno(TEMP2);
                    //resultado[8] = Float.toString((temp3));

                    //----------shipname
                    TEMP2 = bitpay.substring(143,263); //20 caracteres de 6 bit
                    TEMP3=binarioSixbittoASCII(TEMP2);
                    resultado[9] = TEMP3;

                    //----------vesseltype
                    resultado[10]="-------";

                    //----------calado
                    resultado[11]="0.0";

                }else{
                    resultado[0]=Paquete;
                    resultado[1]=Payload;
                    resultado[2]=TIPO;
                    resultado[3]="-------";
                    resultado[4]="-------";
                    resultado[5]="-------";
                    resultado[6]="-------";
                    resultado[7]="-------";
                    resultado[8]="-------";
                    resultado[9]="-------";
                    resultado[10]="-------";
                    resultado[11]="-------";
                }


        long epoch = System.currentTimeMillis()/1000;
        time = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));
        resultado[12]=time;

        Log.i("decodificado","--------------------------------------");
        Log.i("decodificado","Paquete= "+resultado[0]);
        Log.i("decodificado","Payload= "+resultado[1]);
        Log.i("decodificado","Tipo= "+resultado[2]);
        Log.i("decodificado","MMSI= "+resultado[3]);
        Log.i("decodificado","Navigation Status= "+resultado[4]);
        Log.i("decodificado","Precision Posicion= "+ prec);
        Log.i("decodificado","Longitud= "+resultado[5]);
        Log.i("decodificado","Latitud= "+resultado[6]);
        Log.i("decodificado","Velocidad= "+resultado[7]);
        Log.i("decodificado","Curso COG= "+resultado[8]);
        Log.i("decodificado","Nombre= "+resultado[9]);
        Log.i("decodificado","Tipo embarcacion= "+resultado[10]);
        Log.i("decodificado","Calado= "+resultado[11]);
        Log.i("decodificado","Recibido= "+ time);
        Log.i("decodificado","--------------------------------------");


        return resultado;
    }
    private static String TablaNavigationStatus(int X){
        String resultado;
        switch (X){
            case 0:resultado="Under way using engine";break;
            case 1:resultado="At anchor";break;
            case 2:resultado="Not under command";break;
            case 3:resultado="Restricted manoeuverability";break;
            case 4:resultado="Constrained by her draught";break;
            case 5:resultado="Moored";break;
            case 6:resultado="Aground";break;
            case 7:resultado="Engaged in Fishing";break;
            case 8:resultado="Under way sailing";break;
            case 9:resultado="Reserved HSC";break;
            case 10:resultado="Reserved WIG";break;
            case 11:resultado="Reserved for future use";break;
            case 12:resultado="Reserved for future use";break;
            case 13:resultado="Reserved for future use";break;
            case 14:resultado="AIS-SART is active";break;
            case 15:resultado="Not defined";break;
            default:resultado="no entro";
        }
        return resultado;
    }
    private static String TablaVesselType(int X){
        String resultado;
        switch (X){
            case 0:resultado="RFU";break;
            case 1:resultado="RFU";break;
            case 2:resultado="RFU";break;
            case 3:resultado="RFU";break;
            case 4:resultado="RFU";break;
            case 5:resultado="RFU";break;
            case 6:resultado="RFU";break;
            case 7:resultado="RFU";break;
            case 8:resultado="RFU";break;
            case 9:resultado="RFU";break;
            case 10:resultado="RFU";break;
            case 11:resultado="RFU";break;
            case 12:resultado="RFU";break;
            case 13:resultado="RFU";break;
            case 14:resultado="RFU";break;
            case 15:resultado="RFU";break;
            case 16:resultado="RFU";break;
            case 17:resultado="RFU";break;
            case 18:resultado="RFU";break;
            case 19:resultado="RFU";break;
            case 20:resultado="Wing in ground";break;
            case 21:resultado="Wing in ground - Hazardous category A";break;
            case 22:resultado="Wing in ground - Hazardous category B";break;
            case 23:resultado="Wing in ground - Hazardous category C";break;
            case 24:resultado="Wing in ground - Hazardous category D";break;
            case 25:resultado="Wing in ground - RFU";break;
            case 26:resultado="Wing in ground - RFU";break;
            case 27:resultado="Wing in ground - RFU";break;
            case 28:resultado="Wing in ground - RFU";break;
            case 29:resultado="Wing in ground - RFU";break;
            case 30:resultado="Fishing";break;
            case 31:resultado="Towing";break;
            case 32:resultado="Towing: length exceeds 200m or breadth exceeds 25m";break;
            case 33:resultado="Dredging or underwater ops";break;
            case 34:resultado="Diving ops";break;
            case 35:resultado="Military ops";break;
            case 36:resultado="Sailing";break;
            case 37:resultado="Pleasure Craft";break;
            case 38:resultado="Reserved";break;
            case 39:resultado="Reserved";break;
            case 40:resultado="High speed craft";break;
            case 41:resultado="High speed craft - Hazardous category A";break;
            case 42:resultado="High speed craft - Hazardous category B";break;
            case 43:resultado="High speed craft - Hazardous category C";break;
            case 44:resultado="High speed craft - Hazardous category D";break;
            case 45:resultado="High speed craft - RFU";break;
            case 46:resultado="High speed craft - RFU";break;
            case 47:resultado="High speed craft - RFU";break;
            case 48:resultado="High speed craft - RFU";break;
            case 49:resultado="High speed craft";break;
            case 50:resultado="Pilot Vessel";break;
            case 51:resultado="Search and Rescue vessel";break;
            case 52:resultado="Tug";break;
            case 53:resultado="Port Tender";break;
            case 54:resultado="Anti-pollution equipment";break;
            case 55:resultado="Law Enforcement";break;
            case 56:resultado="Spare - Local Vessel";break;
            case 57:resultado="Spare - Local Vessel";break;
            case 58:resultado="Medical Transport";break;
            case 59:resultado="Noncombatant ship according to RR Resolution No.18";break;
            case 60:resultado="Passenger";break;
            case 61:resultado="Passenger - Hazardous category A";break;
            case 62:resultado="Passenger - Hazardous category B";break;
            case 63:resultado="Passenger - Hazardous category C";break;
            case 64:resultado="Passenger - Hazardous category D";break;
            case 65:resultado="Passenger - RFU";break;
            case 66:resultado="Passenger - RFU";break;
            case 67:resultado="Passenger - RFU";break;
            case 68:resultado="Passenger - RFU";break;
            case 69:resultado="Passenger";break;
            case 70:resultado="Cargo";break;
            case 71:resultado="Cargo - Hazardous category A";break;
            case 72:resultado="Cargo - Hazardous category B";break;
            case 73:resultado="Cargo - Hazardous category C";break;
            case 74:resultado="Cargo - Hazardous category D";break;
            case 75:resultado="Cargo - RFU";break;
            case 76:resultado="Cargo - RFU";break;
            case 77:resultado="Cargo - RFU";break;
            case 78:resultado="Cargo - RFU";break;
            case 79:resultado="Cargo";break;
            case 80:resultado="Tanker";break;
            case 81:resultado="Tanker - Hazardous category A";break;
            case 82:resultado="Tanker - Hazardous category B";break;
            case 83:resultado="Tanker - Hazardous category C";break;
            case 84:resultado="Tanker - Hazardous category D";break;
            case 85:resultado="Tanker - RFU";break;
            case 86:resultado="Tanker - RFU";break;
            case 87:resultado="Tanker - RFU";break;
            case 88:resultado="Tanker - RFU";break;
            case 89:resultado="Tanker";break;
            case 90:resultado="Other Type";break;
            case 91:resultado="Other Type - Hazardous category A";break;
            case 92:resultado="Other Type - Hazardous category B";break;
            case 93:resultado="Other Type - Hazardous category C";break;
            case 94:resultado="Other Type - Hazardous category D";break;
            case 95:resultado="Other Type - RFU";break;
            case 96:resultado="Other Type - RFU";break;
            case 97:resultado="Other Type - RFU";break;
            case 98:resultado="Other Type - RFU";break;
            case 99:resultado="Other Type";break;
            default:resultado="Not Available";
        }
        return resultado;
    }
    private static String Filtro(String X){
        //Aqui se realizan los procesos de verificacion de la sentencia entrante.
        //Este bloque verifica que la sentencia entrante tenga la estructura
        //variante del protocolo NMEA 0183. Las consideraciones son:
        //   -Sentencia comience por !AIVDM
        //   -se verifica el checksum de la sentencia
        //   ahora acepta GPRMC sentencias

        String resultado ="rechazada";
        String[] temp = separar(X,",");

        if (temp[0].equals("!AIVDM")) {
            //ahora se comprueba el checksum
            int checksum = 0;
            for (int i = 1; i < X.length() - 3; i++) {
                checksum = checksum ^ X.charAt(i);
            }
            String hexsum = Integer.toHexString(checksum).toUpperCase();
            if (hexsum.length() < 2) {
                hexsum = ("00" + hexsum);
                hexsum = hexsum.substring(1);
            }
            if (hexsum.equals(temp[6].substring(2, 4))) {
                resultado = "aceptada";
            }

        }else if (temp[0].equals("$GPRMC")) {
            int checksum = 0;
            for (int i = 1; i < X.length() - 3; i++) {
                checksum = checksum ^ X.charAt(i);
            }
            String hexsum = Integer.toHexString(checksum).toUpperCase();
            if (hexsum.length() < 2) {
                hexsum = ("00" + hexsum);
                hexsum = hexsum.substring(1);
            }
            if (hexsum.equals(temp[11].substring(2, 4))) {
                resultado = "aceptada";
            }
        }
        return resultado;
    }
    private static String[] separar(String X,String delimitador){
        String[] temp;
        temp = X.split(delimitador);
        return temp;
    }
    private static String ASCIItoSixbit(String X){
        int temp;
        String temp2;
        StringBuilder resultado = new StringBuilder();

        for(int i=0; i < X.length();i++){
            char caracter = X.charAt(i);
            temp = (int) caracter;
            temp = temp - 48;
            if (temp > 40){temp = temp - 8;}
            temp2 = Integer.toBinaryString(temp);
            while (temp2.length() < 6){
                temp2 = "0" + temp2;
            }
            resultado.append(temp2);
        }
        return resultado.toString();
    }
    private static float binariotoFloatconSigno(String X) {
        // dos posibilidades, entero negativo o positivo
        //para negativo:
        if (X.charAt(0) == '1') {
            //complemento a 2
            X = X.replace("0"," "); //donde hay cero poner espacio
            X = X.replace("1","0");
            X = X.replace(" ","1");
            String Xinvertido = X;
            int temp = Integer.parseInt(Xinvertido, 2);
            float temp2 = temp;
            temp2 = (temp2 + 1f) * -1;
            return temp2;
        }
        else {//para positivo
            int temp = Integer.parseInt(X, 2);
            float temp2 = temp;
            return temp2;
        }
    }
    private static float binariotoFloatSinSigno(String X){
        int temp = Integer.parseInt(X, 2);
        float temp2 = temp;
        return temp2;
    }
    private static String binarioSixbittoASCII (String X){
    StringBuilder resultado = new StringBuilder();
    String temp;
    int temp2;

    for(int i=0; i<X.length();i=i+6){
        temp=X.substring(i,i+6);
        temp2 = Integer.parseInt(temp,2);  //esto esta en sixbitchar
        //Log.i("inter","convirtiendo"+"#= "+String.valueOf(temp2));

        //temp2 = 32 + temp2;   //para pasar sixbit a ASCII
        if (temp2<=32) {
            temp = sixbitcharToASCII(temp2); //me retorna el valor en ASCII
            //temp= TablaCaracteres6bit(temp2);
            resultado.append(temp);
        }
       // Log.i("inter","convirtiendo"+"#= "+String.valueOf(temp2)+" = "+temp);
    }
    temp = resultado.toString();
    return temp;
}
    private static String TablaCaracteres6bit(int X){
        String[] vector={"0","1","2","3","4","5","6","7","8","9",":",";","<","=",">",
                "?","@","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P",
                "Q","R","S","T","U","V","W","`","a","b","c","d","e","f","g","h","i","j",
                "k","l","m","n","o","p","q","r","s","t","u","v","w"};
        return vector[X];
    }
    private static String sixbitcharToASCII(int X){

        String[] vector={"SP","!"," ","#","$","%","&","'","(",")","*","+",",","-",".","/",
                "0","1","2","3","4","5","6","7","8","9",":",";","<","=",">","?"," ","A",
                "B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S",
                "T","U","V","W","X","Y","Z","["," ","]","^","_"," "};
        return vector[X+32];
    }


    //Funciones del Mapa, agragar marcadores
    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }
    public boolean googleServicesAvailable(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if(isAvailable == ConnectionResult.SUCCESS){return true;}
        else if(api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this,isAvailable,0);
            dialog.show();
        }else{
            Toast.makeText(this,"No se pudo conectar a Play services",Toast.LENGTH_LONG).show();
        }
        return false;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        goToLocationZoom(11.022280,-74.828403,17);

        if (mGoogleMap!=null){
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    View v = getLayoutInflater().inflate(R.layout.info_window2,null);

                    TextView tvnombre = (TextView) v.findViewById(R.id.tv_nombre);
                    TextView tvtipo = (TextView) v.findViewById(R.id.tv_tipo);
                    TextView tvestado = (TextView) v.findViewById(R.id.tv_estado);
                    TextView tvvelocidad = (TextView) v.findViewById(R.id.tv_velocidad);
                    TextView tvrumbo = (TextView) v.findViewById(R.id.tv_rumbo);
                    TextView tvcalado = (TextView) v.findViewById(R.id.tv_calado);
                    TextView tvlat = (TextView) v.findViewById(R.id.tv_latitud);
                    TextView tvlng = (TextView) v.findViewById(R.id.tv_longitud);
                    TextView tvmmsi = (TextView) v.findViewById(R.id.tv_mmsi);
                    TextView tvtime = (TextView) v.findViewById(R.id.tv_time);
                    //TextView tvtiempo = (TextView) v.findViewById(R.id.tv_tiempo);

                    String[] tempo = separar(marker.getSnippet(),",");



                    tvnombre.setText(tempo[0]);
                    tvtipo.setText(tempo[1]);
                    tvestado.setText(tempo[2]);

                    if(tempo[3].equals("102.3")){tvvelocidad.setText("N/A");
                    }else if(tempo[3].equals("102.2")){tvvelocidad.setText(">=102.2");
                    } else{ tvvelocidad.setText(tempo[3] + " Knots");}

                    if( tempo[3].equals("0.0") || tempo[3].equals("102.3")){   //si la velocidad es cero o no disponible
                        tvrumbo.setText("N/A");
                    }else{ tvrumbo.setText(tempo[4] + "°");}

                    //if(tempo[5].equals("0.0")){
                     //   tvcalado.setText("N/A");
                    //}else{
                        tvcalado.setText(tempo[5] +  " m");
                    //}

                    tvlat.setText(tempo[6] + "°");
                    tvlng.setText(tempo[7] + "°");
                    //tvtiempo.setText("-------");
                    tvmmsi.setText(tempo[8]);
                    tvtime.setText(tempo[9]);


                    return v;
                }
            });
        }

        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);    //pooner el control de zoom
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);   //quitar los botones inferiores derechso por defecto del mapa

    }
    private void goToLocation(double lat, double lng) {
        LatLng ll= new LatLng(lat,lng);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mGoogleMap.moveCamera(update);
    }
    private void goToLocationZoom(double lat, double lng, int zoom) {
        LatLng ll= new LatLng(lat,lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        mGoogleMap.moveCamera(update);
    }
    private void setMarker(double lat, double lng, String[] parametros){
        Marker marker;
        MarkerOptions options = new MarkerOptions()
                .title("")   //"\n" +
                .position(new LatLng(lat,lng))
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrowgrism))
                .anchor(0.5f, 0.5f) // ubico la imagen en el centro de la posicion
                .snippet(parametros[9]+","+parametros[10]+","+parametros[4]+","+parametros[7]+","
                        +parametros[8]+","+parametros[11]+","+parametros[6]+","+parametros[5]+","+parametros[3]+","+parametros[12]);
        marker = mGoogleMap.addMarker(options);

        if (!parametros[8].equals("360.0")) {
            marker.setRotation(Float.parseFloat(parametros[8]));
        }

        marker.setTag(parametros[3]);  //datos[3]=mmsi
        markers.add(marker);   //guarda en lista el marcador al final del array
    }
    private void setMarkerposList(double lat, double lng, String[] parametros, int j){
        Marker marker;
        MarkerOptions options = new MarkerOptions()
                .title("MMSI ="+ parametros[3])
                .position(new LatLng(lat,lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ubicacion_red))
                .anchor(0.5f, 0.5f) // ubico la imagen en el centro de la posicion
                .snippet("Estado=" + parametros[4] + "\n" +
                        "Longitud =" + parametros[5] + "\n" +
                        "Latitud =" + parametros[6] + "\n" +
                        "Velocidad =" + parametros[7] + "\n" +
                        "Curso =" + parametros[8] + "\n" +
                        "Nombre =" + parametros[9]);
        marker = mGoogleMap.addMarker(options);
        marker.setTag(parametros[3]);  //datos[3]=mmsi
        markers.add(j,marker);   //guarda en lista el marcador en poso j del array

        //fecha y hra a la que se genera el marcador
        //String horaF = getDateTime();
    }
    private void setMarker2(double lat, double lng){
        //StringTokenizer name = new StringTokenizer(MarkerName);
        //Marker name;
        Marker marker2;
        MarkerOptions options = new MarkerOptions()
                .title("nombre")
                .position(new LatLng(lat,lng))
                .snippet("algo");
        marker2 = mGoogleMap.addMarker(options);
    }
    private void setmarkerpropio(double lat, double lng, String[] parametros){
        MarkerOptions options = new MarkerOptions()
                .title("")
                .position(new LatLng(lat,lng))
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.navpropio))
                .anchor(0.5f, 0.5f) // ubico la imagen en el centro de la posicion
                .snippet(parametros[9]+","+parametros[10]+","+parametros[4]+","+parametros[7]+","
                        +parametros[8]+","+parametros[11]+","+parametros[6]+","+parametros[5]+","+parametros[3]+","+parametros[12]);
        propio = mGoogleMap.addMarker(options);

        if (!parametros[8].equals("360.0")) {
            propio.setRotation(Float.parseFloat(parametros[8]));
        }
    }
    private void setmarkerestacion(double lat, double lng, String[] parametros){
        Marker marker;
        MarkerOptions options = new MarkerOptions()
                .title("")   //"\n" +
                .position(new LatLng(lat,lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.estacionmarker2))
                .anchor(0.5f, 0.5f) // ubico la imagen en el centro de la posicion
                .snippet(parametros[9]+","+parametros[10]+","+parametros[4]+","+parametros[7]+","
                        +parametros[8]+","+parametros[11]+","+parametros[6]+","+parametros[5]+","+parametros[3]+","+parametros[12]);
        marker = mGoogleMap.addMarker(options);
        marker.setTag(parametros[3]);  //datos[3]=mmsi
        markers.add(marker);   //guarda en lista el marcador al final del array
    }


    private void mensajeEnPantalla(String X){
        Toast toast1 = Toast.makeText(getApplicationContext(),X, Toast.LENGTH_LONG);
        toast1.show();
    }

    //Deshabilitar el boton de atras para esta actividad
    @Override
    public void onBackPressed (){
            //super.onBackPressed();
    }

}

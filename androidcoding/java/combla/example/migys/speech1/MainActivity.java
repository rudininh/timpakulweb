package combla.example.migys.speech1;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket bluetoothSocket = null;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final int ATIVA_BLUETOOTH = 1;
    private static final int KONEKSI = 2;
    public String voice;
    private Button openMic, connect;
    private TextView showVoiceText;
    private final int REQ_CODE_SPEECH_OUTPUT = 143;
    boolean connex = false;
    private static String MAC = null;

    ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(bluetoothAdapter == null){
                Toast.makeText(getApplicationContext(),"Your android doesn't support bluetooth",Toast.LENGTH_LONG).show();
            }else if(!bluetoothAdapter.isEnabled()){
                Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(ativaBluetooth,ATIVA_BLUETOOTH);
            }

            openMic = (Button) findViewById(R.id.tap);
            connect = (Button) findViewById(R.id.connect);
            showVoiceText = (TextView) findViewById(R.id.showVoiceOutput);

            connect.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if(connex){
                        //disc
                        try{
                            bluetoothSocket.close();
                            connex = false;
                            connect.setText("Hubungkan ke Arduino");
                            Toast.makeText(getApplicationContext(),"Koneksi diputus",Toast.LENGTH_LONG).show();
                        }catch (IOException E){
                            Toast.makeText(getApplicationContext(),"Occured Error: "+E,Toast.LENGTH_LONG).show();
                        }
                    }else{
                        //conn;
                        Intent konList = new Intent(MainActivity.this, ListKoneksi.class);
                        startActivityForResult(konList, KONEKSI);
                    }
                }
             });

        openMic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(connex){
                    btnToOpenMic();
                }else{
                    Toast.makeText(getApplicationContext(),"Bluetooth belum terkoneksi ke Arduino",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater nMenuInflater = getMenuInflater();
        nMenuInflater.inflate(R.menu.my_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.tentang_kami){
            Intent Tentangkami = new Intent(MainActivity.this, Tentang_Kami.class);
            startActivity(Tentangkami);
        }
        if(item.getItemId() == R.id.bt_setting){
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }
        return super.onOptionsItemSelected(item);
    }

    private void btnToOpenMic(){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ucapkan Perintah!");

            try{
                startActivityForResult(intent,REQ_CODE_SPEECH_OUTPUT);

            }catch (ActivityNotFoundException tim){
                Toast.makeText(getApplicationContext(),"Kesalahan saat membuaka Mic Google",Toast.LENGTH_LONG).show();
            }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_CODE_SPEECH_OUTPUT :
                if(resultCode == RESULT_OK && null != data){
                    ArrayList<String> voiceInText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if(connex){
                        voice = voiceInText.get(0);
                        Toast.makeText(getApplicationContext(),voice,Toast.LENGTH_LONG).show();
                        connectedThread.send(voice);

                    }else{
                        Toast.makeText(getApplicationContext(),"Bluetooth tidak terkoneksi ke arduino",Toast.LENGTH_LONG).show();
                    }

                }
                break;


            case KONEKSI:
                if(resultCode == Activity.RESULT_OK){
                    MAC = data.getExtras().getString(ListKoneksi.E_MAC);

                    //Toast.makeText(getApplicationContext(),"MAC FINAL: "+MAC,Toast.LENGTH_LONG).show();
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);

                    try{
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        bluetoothSocket.connect();
                        connex = true;
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                        connect.setText("Matikan Koneksi");
                        Toast.makeText(getApplicationContext(),"Terhubung: "+MAC,Toast.LENGTH_LONG).show();
                    }catch (IOException E){
                        connex = false;
                        Toast.makeText(getApplicationContext(),"Occured an error: "+E,Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Tidak Terhubung",Toast.LENGTH_LONG).show();
                }

        }
    }

    private class ConnectedThread extends Thread {
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            OutputStream tmpOut = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            outputStream = tmpOut;
        }

        public void send(String suara){
            byte[] msgBuffer = suara.getBytes();
            try{
                outputStream.write(msgBuffer);
            }catch (IOException e){

            }
        }
    }
}
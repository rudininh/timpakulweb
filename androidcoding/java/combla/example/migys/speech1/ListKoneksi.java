package combla.example.migys.speech1;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class ListKoneksi extends ListActivity{
    private BluetoothAdapter bluetoothAdapter2 = null;
    static String E_MAC = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> ArrayBlueetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        bluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> dispositive = bluetoothAdapter2.getBondedDevices();
        if(dispositive.size() > 0){
            for(BluetoothDevice dispositif : dispositive){
                String Bt = dispositif.getName();
                String macBt = dispositif.getAddress();

                ArrayBlueetooth.add(Bt +"\n"+macBt);
            }
        }
        setListAdapter(ArrayBlueetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String information = ((TextView) v).getText().toString();
        //Toast.makeText(getApplicationContext(),"Info: "+information,Toast.LENGTH_LONG).show();

        String enterMac = information.substring(information.length() - 17);
        //Toast.makeText(getApplicationContext(),"mac: "+enterMac,Toast.LENGTH_LONG).show();

        Intent returnMac = new Intent();
        returnMac.putExtra(E_MAC, enterMac);
        setResult(RESULT_OK, returnMac);
        finish();
    }

}

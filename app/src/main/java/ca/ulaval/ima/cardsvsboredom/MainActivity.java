package ca.ulaval.ima.cardsvsboredom;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.sql.BatchUpdateException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private Button              myPageButton;
    private Button              joinButton;
    private Button              createButton;
    private EditText            createEditText;
    private TextView            joinErrorText;
    private TextView            createErrorText;

    private BluetoothAdapter    bluetoothAdapter;

    private DeviceAdapter       arrayAdapter;
    private ListView            listView;

    private BluetoothDevice     selectedDevice;



    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                arrayAdapter.add(device);
                Toast.makeText(getApplicationContext(), String.format("device found : %s!", device.getName()), Toast.LENGTH_LONG).show();
            }
        }

        public void onDestroy(){
            unregisterReceiver(mReceiver);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPageButton    = (Button)      findViewById(R.id.myPage_button);
        joinButton      = (Button)      findViewById(R.id.join_button);
        createButton    = (Button)      findViewById(R.id.create_button);
        createEditText  = (EditText)    findViewById(R.id.create_edit_text);
        joinErrorText   = (TextView)    findViewById(R.id.error_join);
        createErrorText = (TextView)    findViewById(R.id.error_create);

        listView        = (ListView)    findViewById(R.id.gameListView);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
                // TODO Auto-generated method stub
                selectedDevice = (BluetoothDevice)arg0.getItemAtPosition(arg2);
                joinButton.setClickable(true);
                arg1.setSelected(true);
            }
        });

        joinButton.setClickable(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Log.e("bluetooth", "bluetooth unavailable");
            Toast.makeText(this, "bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }else{
            Toast.makeText(this, bluetoothAdapter.getName(), Toast.LENGTH_LONG).show();
        }

        arrayAdapter = new DeviceAdapter(this, new ArrayList<BluetoothDevice>());
        listView.setAdapter(arrayAdapter);

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

// Register the BroadcastReceiver
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    @Override
    protected void onResume(){
        super.onResume();
        arrayAdapter.clear();
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode) {
            case 0:
                if(resultCode != Activity.RESULT_OK) {
                    Log.e("bluetooth", "bluetooth disabled");
                    Toast.makeText(this, "bluetooth is disabled", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    arrayAdapter.clear();
                    bluetoothAdapter.startDiscovery();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);                }
                break;
            default:
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void create(View view){
        createErrorText.setText("");
        if(createEditText.getText().length() == 0){
            createErrorText.setText(R.string.error_create);
        }else{
            Intent intent = new Intent(getApplicationContext(), HubActivity.class);
            intent.putExtra("dealer", true);
            intent.putExtra("gameName", createEditText.getText().toString());
            startActivity(intent);
        }
    }

    public void join(View view){
        createErrorText.setText("");
        if(selectedDevice == null && listView.getCheckedItemPosition() == AdapterView.INVALID_POSITION){
            createErrorText.setText(R.string.error_join);
        }else{
            Intent intent = new Intent(getApplicationContext(), HubActivity.class);
            intent.putExtra("dealer", false);
            intent.putExtra("device", selectedDevice);
            startActivity(intent);
        }
    }

    public void goToMyPage(View view){
        Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
        startActivity(intent);
    }

    static public class DeviceAdapter extends ArrayAdapter<BluetoothDevice>{

        private ArrayList<BluetoothDevice> mDeviceList;
        private Activity mActivity;

        public DeviceAdapter(Activity inActivity, ArrayList<BluetoothDevice> inBrandList){
            super(inActivity, R.layout.list_cell_device, inBrandList);
            this.mActivity = inActivity;
            mDeviceList=inBrandList;

        }

        public void addDevice(BluetoothDevice device){
            mDeviceList.add(device);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mDeviceList.size();
        }

        @Override
        public BluetoothDevice getItem(int arg0) {
            // TODO Auto-generated method stub
            return mDeviceList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        static class ViewHolder {
            public TextView name;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            View childView = arg1;
            if(childView == null || childView.getTag() == null){

                childView = mActivity.getLayoutInflater().inflate(R.layout.list_cell_device, null);

                ViewHolder viewHolder = new ViewHolder();

                viewHolder.name = (TextView) childView.findViewById(R.id.game_name);

                childView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) childView.getTag();
            holder.name.setText(mDeviceList.get(arg0).getName());
            return childView;
        }

    }

}

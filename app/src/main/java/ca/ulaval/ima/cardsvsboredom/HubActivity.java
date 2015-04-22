package ca.ulaval.ima.cardsvsboredom;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.LogRecord;

/**
 * Created by Pierre on 18/04/2015.
 */
public class HubActivity extends ActionBarActivity {

    final Handler handler = new Handler();

    final Runnable updateList = new Runnable() {
        @Override
        public void run() {
            arrayAdapter.add(whiteCards[nbCards - 1]);
            arrayAdapter.notifyDataSetChanged();
            //Toast.makeText(getApplicationContext(), String.format("nouvelle carte : %s", whiteCards[nbCards - 1]), Toast.LENGTH_LONG).show();
        }
    };

    private Button continueButton;

    private BluetoothAdapter bluetoothAdapter;

    ListView listView;
    CardAdapter arrayAdapter;

    private boolean dealer;
    private String uuid;

    //Serveur : dealer = true

    private int clientId;
    private BluetoothServerSocket serverOwnSocket;
    private List<BluetoothSocket> clientSockets;
    private String[] clientChoices;

    private AcceptThread acceptThread;
    private List<ServerConnectedThread> serverConnectedThreads;

    //Client : dealer = false
    private String[] whiteCards;
    private int nbCards = 0;

    private BluetoothSocket serverSocket;

    private ConnectThread connectThread;
    private ClientConnectedThread clientConnectedThread;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        continueButton = (Button) findViewById(R.id.continue_button);

        continueButton.setVisibility(View.GONE);

        dealer = getIntent().getBooleanExtra("dealer", false);

        listView = (ListView) findViewById(R.id.hubListView);
        arrayAdapter = new CardAdapter(this, new ArrayList<String>());
        listView.setAdapter(arrayAdapter);


        uuid = getResources().getString(R.string.uuid);

        if(bluetoothAdapter == null){
            Log.e("bluetooth", "bluetooth unavailable");
            Toast.makeText(this, "bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        if(dealer){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, 1);

            clientChoices = new String[10];
            clientSockets = new ArrayList<>();
            serverConnectedThreads = new ArrayList<>();

            //Ecoute les connexions clients
            acceptThread = new AcceptThread();
            acceptThread.start();


        }else{
            BluetoothDevice device = getIntent().getParcelableExtra("device");
            Log.d("uuid-client", device.getAddress());

            whiteCards = new String[10];

            //Connexion au serveur
            connectThread = new ConnectThread(device);
            connectThread.start();





            //Recuperation des cartes blanches

            //Attente de l'envoi de la carte noir de la part du serveur

            //Lancement de la partie
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode) {
            case 0://activation du BT
                if(resultCode != Activity.RESULT_OK) {
                    Log.e("bluetooth", "bluetooth disabled");
                    Toast.makeText(this, "bluetooth is disabled", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(this, "bluetooth is now enabled", Toast.LENGTH_LONG).show();
                }
                break;
            case 1://activation de la discoverabilitationagation du BT
                if(resultCode != 300) {
                    Log.e("bluetooth", "device not discoverable");
                    Toast.makeText(this, "device not discoverable", Toast.LENGTH_LONG).show();
                    finish();
                }else{

                    Toast.makeText(this, String.format("device is now discoverable with uuid %s", uuid), Toast.LENGTH_LONG).show();


                    Log.d("uuid-server", uuid.toString());

                    continueButton.setVisibility(View.VISIBLE);

                    acceptThread = new AcceptThread();
                    acceptThread.start();//Les clients peuvent se connecter

                }
                break;
            case 2://retour du jeu du server
                if(resultCode != Activity.RESULT_OK) {
                    Log.e("server", "no cards was picking, operation canceled");
                    Toast.makeText(this, "server canceled the operation", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, String.format("choosen card : %s", data.getStringExtra("choice")), Toast.LENGTH_LONG).show();
                    //Determination du gagnant !
                }
            case 3://retour du jeu du client
                if(resultCode != Activity.RESULT_OK) {
                    Log.e("client", "no cards was picking, operation canceled");
                    Toast.makeText(this, "client canceled the operation", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, String.format("choosen card : %s", data.getStringExtra("choice")), Toast.LENGTH_LONG).show();
                }
                //Envoi au serveur de la réponse

                //Attente de la réponse du serveur sur le gagnant
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

    public void startGame(View view){

        //Choix d'un carte noir
        String[] blackCards = getResources().getStringArray(R.array.black);
        String black = blackCards[3];

        for(int i = 0;i < serverConnectedThreads.size();i++){
            serverConnectedThreads.get(i).write(black.getBytes(), false);
        }

        //Envoi de carte blanche aux clients
        //TO DO : envoie des cartes

        //Les clients jouent

        //Lancement du thread d'écoute des réponses

        /*acceptThread.cancel();
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);

        String[] whiteCards = getResources().getStringArray(R.array.white);


        String white[] = new String[10];

        for(int i = 0; i < 10;i++){
            white[i] = whiteCards[i + 10];
        }



        intent.putExtra("white", white);
        intent.putExtra("black", black);

        startActivityForResult(intent, 2);*/
    }

    public void stopGame(View view){
        //Le serveur arrête de recolter des réponses, et kick les clients qui n'on pas répondus
        //C'est à lui de jouer
    }

    public void addCard(String s){
        nbCards++;
        whiteCards[nbCards - 1] = s;
        Log.d("client", String.format("nouvelle carte : %s", whiteCards[nbCards - 1]));
        handler.post(updateList);


    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private boolean wait = true;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("myBluetooth", UUID.fromString(uuid));
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (wait) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    clientSockets.add(socket);

                    //Creation ServerConnectedSocket
                    ServerConnectedThread serverConnectedThread = new ServerConnectedThread(socket);
                    serverConnectedThreads.add(serverConnectedThread);

                    String[] cards = getResources().getStringArray(R.array.white);

                    for(int i = 0; i < 10;i++) {
                        cards[i] += "_";
                        serverConnectedThread.write(cards[i].getBytes(), true);
                    }





                    //Envoi cartes blanches
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                wait = false;
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        //private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // TO DO : get UUID from server
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
            } catch (IOException e) { }
            serverSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            //mBluetoothAdapter.cancelDiscovery(); At this stage, discovery should be already canceled

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                serverSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    serverSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            //Ecoute les reponse du serveur
            clientConnectedThread = new ClientConnectedThread(serverSocket);
            clientConnectedThread.start();
            //Toast.makeText(getApplicationContext(), "Connexion au serveur réussie, en attente d'instruction !", Toast.LENGTH_LONG).show();

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) { }
        }
    }


    //Thread d'écoute serveur
    //-A chaque reponse du client, ajoute la réponse
    private class ServerConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ServerConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()


            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Recupère les réponses du client
                    String result = new String(buffer);
                    clientChoices[clientId] = result;

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes, boolean start) {
            try {
                // Envois la carte noir aux clients
                //Ou
                //Envois le résultat aux clients
                mmOutStream.write(bytes);

            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    //Thread d'écoute client
    //-A la première réponse du serveur, lance la partie
    //-A la seconde réponse, affiche les résultats
    private class ClientConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ClientConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(nbCards < 10){
                        String s = new String(buffer);
                        String[] cards = s.split("_");
                        for(int i = 0;i < cards.length - 1;i++){
                            addCard(cards[i]);
                        }
                        //Toast.makeText(getApplicationContext(), "Cartes bien reçues !", Toast.LENGTH_LONG).show();


                    }else{

                        Log.d("client", "full hand");
                        bytes = mmInStream.read(buffer);
                        String s = new String(buffer);

                        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                        intent.putExtra("white", whiteCards);
                        intent.putExtra("black", s);
                        startActivity(intent);
                    }


                    // Recupère les réponses des serveur, avec leur Id
                    //Si envoi carte noir et main : lance la partie
                    //Si envoi carte blanche : affiche résultats

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                // Envois la carte blanche au serveur
                mmOutStream.write(bytes);

            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    static public class CardAdapter extends ArrayAdapter<String>{

        private ArrayList<String> mCardList;
        private Activity mActivity;

        public CardAdapter(Activity inActivity, ArrayList<String> inBrandList){
            super(inActivity, R.layout.list_cell_device, inBrandList);
            this.mActivity = inActivity;
            mCardList=inBrandList;

        }

        public void addCard(String card){
            mCardList.add(card);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mCardList.size();
        }

        @Override
        public String getItem(int arg0) {
            // TODO Auto-generated method stub
            return mCardList.get(arg0);
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
            holder.name.setText(mCardList.get(arg0));
            return childView;
        }

    }
}

package ca.ulaval.ima.cardsvsboredom.util;

import android.content.Context;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by nicolas on 22/04/2015.
 */
public class FavoriteManagement {

    public boolean addFavorite(Context context, String data) {

        boolean result = true;

        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        try {
            fOut = context.openFileOutput("favorites.dat", Context.MODE_APPEND);
            osw = new OutputStreamWriter(fOut);
            osw.write("/" + data);
            osw.flush();
        } catch (Exception e) {
            result = false;
        } finally {
            try {
                osw.close();
                fOut.close();
            } catch (IOException e) {
                result = false;
            }
        }

        return result;
    }

    public String getFavorites(Context context){

        FileInputStream fIn = null;
        InputStreamReader isr = null;

        char[] inputBuffer = new char[255];
        String data = null;

        try{
            fIn = context.openFileInput("favorites.dat");
            isr = new InputStreamReader(fIn);
            isr.read(inputBuffer);
            data = new String(inputBuffer);
            //affiche le contenu de mon fichier dans un popup surgissant
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(context, "Favorites not read",Toast.LENGTH_SHORT).show();
        }
            /*finally {
               try {
                      isr.close();
                      fIn.close();
                      } catch (IOException e) {
                        Toast.makeText(context, "Settings not read",Toast.LENGTH_SHORT).show();
                      }
            } */

        return data;
    }
}
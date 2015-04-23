package ca.ulaval.ima.cardsvsboredom;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ca.ulaval.ima.cardsvsboredom.util.FavoriteManagement;


public class FavoriteActivity extends ActionBarActivity {

    private TextView sentenceView;
    private TextView victoryView;
    private Button tweetButton;
    private Button saveButton;
    private Button returnButton;

    private String sentence;
    private String victory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        sentenceView = (TextView) findViewById(R.id.textView_favorite_sentence);
        victoryView = (TextView) findViewById(R.id.textView_favorite_victory);
        tweetButton = (Button) findViewById(R.id.button_favorite_tweet);
        saveButton = (Button) findViewById(R.id.button_favorite_save);
        returnButton = (Button) findViewById(R.id.button_favorite_return);

        sentence = String.format(getIntent().getStringExtra("black"), getIntent().getStringExtra("white"));
        if (getIntent().getBooleanExtra("victory", false)) {
            victory = "VICTOIRE !!!";
        }
        else {
            victory = "DEFAITE...";
        }

        sentenceView.setText(sentence);
        victoryView.setText(victory);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorite, menu);
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

    public void tweet(View view) {

    }

    public void save(View view) {

        FavoriteManagement fm = new FavoriteManagement();

        if (fm.addFavorite(getApplicationContext(), sentenceView.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Sentence saved", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Error : Sentence not saved", Toast.LENGTH_SHORT).show();
        }

        saveButton.setClickable(false);
    }

    public void goBack(View view) {
        finish();
    }

}

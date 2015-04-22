package ca.ulaval.ima.cardsvsboredom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

/**
 * Created by Pierre on 19/04/2015.
 */
public class PlayActivity extends ActionBarActivity {
    private ViewFlipper viewFlipper;

    private ArrayList<TextView> hand;
    private String[] whiteCards;

    private float lastX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        hand = new ArrayList<>();

        //XmlPullParser parser = getApplicationContext().getResources().getLayout(myResouce);
        //AttributeSet attributes = Xml.asAttributeSet(parser);

        whiteCards = getIntent().getStringArrayExtra("white");
        String blackCard = getIntent().getStringExtra("black");

        for(int i = 0;i < 10;i++){
            TextView text = new TextView(getApplicationContext());

            String cardText = String.format(blackCard, whiteCards[i]);

            //int nbCaract = cardText.length();

            text.setPadding(30, 30, 15, 15);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            text.setTextColor(Color.WHITE);
            text.setBackgroundColor(Color.BLACK);
            hand.add(text);
            text.setText(cardText);
            viewFlipper.addView(text);

            ViewGroup.LayoutParams params = text.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(R.dimen.card_view_height);
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

    // Using the following method, we will handle all screen swaps.
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float currentX = touchevent.getX();

                // Handling left to right screen swap.
                if (lastX < currentX) {

                    // If there aren't any other children, just break.
                    if (viewFlipper.getDisplayedChild() == 0)
                        break;

                    // Next screen comes in from left.
                    //viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);
                    // Current screen goes out from right.
                    //viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);

                    // Display next screen.
                    viewFlipper.showNext();
                }

                // Handling right to left screen swap.
                if (lastX > currentX) {

                    // If there is a child (to the left), kust break.
                    if (viewFlipper.getDisplayedChild() == 1)
                        break;

                    // Next screen comes in from right.
                    //viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
                    // Current screen goes out from left.
                    //viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                    // Display previous screen.
                    viewFlipper.showPrevious();
                }
                break;
        }
        return false;
    }

    public void choose(View view){
        Intent result = new Intent();
        String choice = whiteCards[viewFlipper.getDisplayedChild()];
        result.putExtra("choice", choice);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}

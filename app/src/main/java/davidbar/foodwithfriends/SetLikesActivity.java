package davidbar.foodwithfriends;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kumulos.android.jsonclient.Kumulos;
import com.kumulos.android.jsonclient.ResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;


public class SetLikesActivity extends AppCompatActivity {

    private static final String TAG = "SetLikesDislikes";

    // Cuisines user currently can set
    //private static final String[] CUISINES = {"American", "Chinese", "Cuban", "Indian", "Italian",
    //                                          "Japanese", "Korean", "Mexican", "Taiwanese", "Thai"};

    static final int color_gold = Color.parseColor("#FFD700");
    static final int color_green = Color.parseColor("#32CD32");
    static final int color_red = Color.parseColor("#FF0000");

    // Used to store user scores
    private HashMap<String, Integer> mLikes = new HashMap<>();

    private HashMap<String, String> mContactsNumbertoName;

    // Number to Likes map
    private HashMap<String, String> mSelectedFriends;

    private ArrayList<HashMap> mFriendLikes;

    // ID for cuisine buttons
    private int idCount = 0;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_likes);

        // Populate Extras
        Intent intent = getIntent();
        mContactsNumbertoName = (HashMap<String, String>) intent.getSerializableExtra("contacts");
        mSelectedFriends = (HashMap<String, String>) intent.getSerializableExtra("selectedFriends");
        mFriendLikes = (ArrayList) intent.getSerializableExtra("friendLikes");

        //likes = initLikes();
        showLikes();
    }

//    // Initialize our likes Map with cuisine keys and values of zero
//    private HashMap<String, Integer> initLikes(){
//
//        HashMap<String, Integer> map = new HashMap<String, Integer>();
//
//        for(String s : CUISINES){
//            map.put(s, 0);
//        }
//
//        return map;
//    }

    private void showLikes(){

        HashMap<String, String> params = new HashMap<>();

        String num = ContactsMagic.getRegisteredUser(this);

        Log.d(TAG, "FOUND REGISTERED USER: " + num);

        params.put("userNumber", num);

        Kumulos.call("queryUser", params, new ResponseHandler(){

            @Override
            public void didCompleteWithResult(Object result){

                Log.d(TAG, "Finished Query...");
                if (result != null && ((ArrayList)result).size() != 0){
                    Log.d(TAG, "Found user...");
                    HashMap r = (HashMap)((ArrayList) result).get(0);
                    String likes = (String)r.get("likes");
                    mLikes = CuisineMagic.convertLikesStringToMap(likes);
                    Log.d(TAG, "mLikes set to: " + mLikes.toString());
                    for (String s : CuisineMagic.CUISINES){
                        Integer score = mLikes.get(s);
                        addCuisineButton(s,intToColor(score));
                    }
                }

            }
        });
    }

    private void addCuisineButton(String s, int color){

        LinearLayout ll = (LinearLayout)findViewById(R.id.scroll_likes);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(15, 15, 15, 15);
        Button button = new Button(this);
        button.setText(s);
        button.setId(idCount++);
        button.setBackgroundColor(color);
        button.setOnClickListener(listener);
        button.setHeight(70);

        if (color == color_green) {
            button.setTextColor(Color.WHITE);
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.very_satisfied_white, 0, 0, 0);
        }else if (color == color_red){
            button.setTextColor(Color.WHITE);
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.very_dissatisfied_white, 0, 0, 0);
        }else{
            button.setTextColor(Color.BLACK);
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.satisfied_black, 0, 0, 0);
        }
        button.setPadding(50,0,0,0);

        ll.addView(button,lp);
    }

    // Listener for friend buttons...
    private View.OnClickListener listener = new View.OnClickListener(){
        public void onClick(View view) {

            Button button = (Button) findViewById(view.getId());

            // Get the color of the button
            ColorDrawable drawable = (ColorDrawable) button.getBackground();
            int color = drawable.getColor();

            // Get text of button as String
            String buttonText = (String) button.getText();

            // Debug info
            Log.d(TAG, "CLICK!!");
            Log.d(TAG, "Color is : ".concat(String.valueOf(color)));
            Log.d(TAG, "Text is : ".concat(buttonText));

            // Flip through the 3 colors
            if (color == color_gold) {
                button.setBackgroundColor(color_green);
                button.setTextColor(Color.WHITE);
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.very_satisfied_white, 0, 0, 0);
                mLikes.put(buttonText, 1);
            } else if (color == color_green) {
                button.setBackgroundColor(color_red);
                button.setTextColor(Color.WHITE);
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.very_dissatisfied_white, 0, 0, 0);
                mLikes.put(buttonText, -1);
            } else if (color == color_red) {
                button.setBackgroundColor(color_gold);
                button.setTextColor(Color.BLACK);
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.satisfied_black, 0, 0, 0);
                mLikes.put(buttonText, 0);
            }
        }
    };

    private static int intToColor(int in){

        if( in == 1){
            return color_green;
        }else if ( in == -1){
            return color_red;
        }else{
            return color_gold;
        }
    }

    /** Called when the user clicks the Next button */
    public void clickNextButton(View view) {
        try {
            Log.d(TAG, "CLICK!!");
            Log.d(TAG, "FINAL MAP: " + mLikes.toString());

            String num = ContactsMagic.getMyPhoneNumber(this);
            String sLikes = CuisineMagic.convertLikesMapToString(mLikes);

            // Save user likes to database
            if (num != null) {
                KumulosWrapper.updateUser(num, sLikes);
            }

            // Add user likes ot be included in calculations
            mFriendLikes.add(mLikes);

            Intent intent = new Intent(this, SetLocationActivity.class);

            intent.putExtra("friendLikes", mFriendLikes);
            intent.putExtra("selectedFriends", mSelectedFriends);
            intent.putExtra("contacts", mContactsNumbertoName);

            //intent.putExtra("likes", mLikes);
            startActivity(intent);
        }catch(Exception e){
            // Don't start new activity
        }
    }

    // Go back to MainActivity
    // Save user preferences if available
    // Ignore them if they haven't been set
    public void clickBackButton(View view){

        try {
            String num = ContactsMagic.getMyPhoneNumber(this);
            String sLikes = CuisineMagic.convertLikesMapToString(mLikes);

            // Save user likes to database
            if (num != null) {
                KumulosWrapper.updateUser(num, sLikes);
            }
        }catch (Exception e){
            // Nothing...
        }finally{
            finish();
        }
    }
}

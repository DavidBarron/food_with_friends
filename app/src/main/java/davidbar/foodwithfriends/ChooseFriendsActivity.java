package davidbar.foodwithfriends;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kumulos.android.jsonclient.Kumulos;
import com.kumulos.android.jsonclient.ResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class ChooseFriendsActivity extends AppCompatActivity {

    private static final String TAG = "ChooseFriendsActivity";

    static final int color_green = Color.parseColor("#32CD32");
    static final int color_grey = Color.parseColor("#a8a8a8");

    private HashMap<String, String> mContactsNametoNumber = new HashMap<String, String>();
    private HashMap<String, String> mContactsNumbertoName = new HashMap<String, String>();

    // Friends found registered in database
    // Number to Likes
    private HashMap<String, String> mFoundFriends = new HashMap<String, String>();

    // Friends currently selected by user
    // Number to Likes
    private HashMap<String, String> mSelectedFriends = new HashMap<String, String>();

    // id for Friend Buttons
    private int idCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friends);

        mContactsNametoNumber = ContactsMagic.getContactsNameToNumber(this);
        mContactsNumbertoName = ContactsMagic.getContactsNumberToName(this);

        showFriends(mContactsNametoNumber);
    }

    // Listener for friend buttons...
    private View.OnClickListener listener = new View.OnClickListener(){
        public void onClick(View view){

            Button button = (Button) findViewById(view.getId());

            // Get the color of the button
            ColorDrawable drawable = (ColorDrawable)button.getBackground();
            int color = drawable.getColor();

            if (color == color_grey){
                String name = (String)button.getText();
                String num = mContactsNametoNumber.get(name);
                String likes = mFoundFriends.get(num);
                mSelectedFriends.put(num, likes);
                button.setBackgroundColor(color_green);
                button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.check_box_white, 0, 0, 0);
            } else {
                String name = (String)button.getText();
                String num = mContactsNametoNumber.get(name);
                mSelectedFriends.remove(num);
                button.setBackgroundColor(color_grey);
                button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.check_box_outline_blank_white, 0, 0, 0);
            }
        }
    };


    private void addFriendButton(String s){

        LinearLayout ll = (LinearLayout)findViewById(R.id.scroll_friends);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(15, 15, 15, 15);
        Button button = new Button(this);
        button.setText(s);
        button.setId(idCount++);
        button.setBackgroundColor(color_grey);
        button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.check_box_outline_blank_white, 0, 0, 0);
        button.setOnClickListener(listener);
        button.setPadding(50,0,0,0);

        ll.addView(button,lp);
    }

    // contacts mapping is String name to String number
    protected  void showFriends(HashMap<String, String> contacts){

        HashMap<String, String> params = new HashMap<>();

        for (String s : contacts.keySet()){

            String num = contacts.get(s);
            params.clear();
            params.put("userNumber",num);
            Log.d(TAG,s);
            Log.d(TAG,params.toString());

            Kumulos.call("queryUser",params,new ResponseHandler(){

                @Override
                public void didCompleteWithResult(Object result){

                    if( result != null && ((ArrayList)result).size() != 0){
                        Log.d(TAG, result.getClass().toString());
                        Log.d(TAG, result.toString());
                        //Log.d(TAG, "" + ((ArrayList)result).size());
                        HashMap r = (HashMap)((ArrayList) result).get(0);
                        //Log.d(TAG, r.getClass().toString());
                        String num = (String)r.get("userNumber");
                        String likes = (String)r.get("likes");
                        mFoundFriends.put(num, likes);
                        addFriendButton(mContactsNumbertoName.get(num));
                    }

                }
            });
        }
    }

    public void clickInviteButton(View view){
        // do something
    }

    public void clickNextButton(View view){
        Log.d(TAG, mSelectedFriends.toString());
        Intent intent = new Intent(this, SetLikesActivity.class);

        ArrayList<HashMap> friendLikes = new ArrayList<>();

        for (HashMap.Entry<String, String> entry : mSelectedFriends.entrySet()) {
            HashMap map = CuisineMagic.convertLikesStringToMap(entry.getValue());
            friendLikes.add(map);
        }

        intent.putExtra("friendLikes", friendLikes);
        intent.putExtra("selectedFriends", mSelectedFriends);
        intent.putExtra("contacts", mContactsNumbertoName);

        startActivity(intent);
    }

    public void clickBackButton(View view){
        finish();
    }

}

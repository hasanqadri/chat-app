package com.shannor.simplechatapp;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.shannor.simplechatapp.model.ChatAdapter;
import com.shannor.simplechatapp.model.Conversation;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ChatActivity extends AppCompatActivity {

    public static final String FIREBASE_URL = "https://bootcampchat.firebaseio.com";

    private String uId;
    //FireBase stuff is not necessary for you to know
    private Firebase mFireBaseRef;
    private Firebase mFireBaseMessages;
    //********************************
    private ChatAdapter mChatAdapter;
    private String userName = "Hasan Qadri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FireBase stuff no Concern
        //***************************************************
        Firebase.setAndroidContext(this);
        mFireBaseRef = new Firebase(FIREBASE_URL);
        mFireBaseMessages = new Firebase(FIREBASE_URL).child("messages");
        //Creates an account for the user
        uId = setUpAuth();
        Firebase userIdSave = mFireBaseRef.child("users");
        Map<String,String> userInfo = new HashMap<>();
        //Add your name here
        userInfo.put("name",userName);
        userInfo.put("platform","android");
        userIdSave.child(uId).setValue(userInfo);
        mFireBaseRef = new Firebase(FIREBASE_URL).child("messages");
        //****************************************************

        //TODO:Setup Button and Button listener.
        Button firstbutton = (Button)findViewById(R.id.btnSend);
        firstbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            sendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ListView listView = (ListView)findViewById(R.id.list);
        mChatAdapter = new ChatAdapter(mFireBaseRef.limitToLast(40),this,R.layout.chat_item,uId);
        listView.setAdapter(mChatAdapter);

        mChatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mChatAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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

    /**
     * Code that deals with Firebase implementation.
     * Not necessary if you don't care about how Firebase does things.
     * @return
     */

    public String setUpAuth(){

        if (mFireBaseRef.getAuth() == null) {

            final CountDownLatch latch = new CountDownLatch(1);

            mFireBaseRef.authAnonymously(new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    latch.countDown();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    throw firebaseError.toException();
                }
            });

            awaitLatch(latch);
        }

        return mFireBaseRef.getAuth().getUid();
    }

    /**
     * Also apart of Firebase implementation.
     * @param latch
     */
    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * How the user sends a message. For our app.
     */
    public void sendMessage(){
        //Pull the information from the Activity to be able to send messages.
        EditText firstText = (EditText)findViewById(R.id.sending_messages);
        String editing = firstText.getText().toString();
        if (!editing.equals("")) {
            Conversation firstConversation = new Conversation(editing, uId);
            mFireBaseMessages.push().setValue(firstConversation);
            firstText.setText("");
        }
    }
}

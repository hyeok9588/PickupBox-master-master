package app.project.com.pickupbox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import app.project.com.pickupbox.Adapter.ChatAdapter;
import app.project.com.pickupbox.Data.ChatData;
import app.project.com.pickupbox.Data.UserData;
import app.project.com.pickupbox.Delivery_Now.Chatting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ChatBot extends AppCompatActivity implements Button.OnClickListener{

    private String nick, userName;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    // Views
    private ListView mListView2;
    private TextView tvMSG;
    // Values
    private ChatAdapter mAdapter2;


    private Button btnChat1,btnChat2,btnChat3,btnChat4;
    private String BotUser = "챗봇";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        initFind();
        initViews();
        initFirebaseDatabase();
        initValues();

    }

    private void initFind(){

      //  tvMSG = findViewById(R.id.tvMSG);
    }

    private void initValues() {

        //로그인 여부 확인/*---------------------------------------------------------------------------*/
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            userName = nick;

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면
            //로그인이 잘못 됨.

        }
    }

    private void initFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("message");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatData chatData = dataSnapshot.getValue(ChatData.class);
                chatData.firebaseKey = dataSnapshot.getKey();
                mAdapter2.add(chatData);
                mListView2.smoothScrollToPosition(mAdapter2.getCount());
                mAdapter2.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String firebaseKey = dataSnapshot.getKey();

                int count = mAdapter2.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter2.getItem(i).firebaseKey.equals(firebaseKey)) {
                        mAdapter2.remove(mAdapter2.getItem(i));
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }


    private void initViews() {
        mListView2 = (ListView) findViewById(R.id.list_message2);
        mAdapter2 = new ChatAdapter(this, 0);
        mListView2.setAdapter(mAdapter2);

        mListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String toPerson = mAdapter2.getItem(position).userName;
                //사람 아이디 알아내는 부분, 여기서 토큰을 알아내기 위한 첫 단계

            }
        });



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnChat1:
                //tvMSG.setText("배송 예정 시간 알려줘!");
                SendMsg(1);
                break;


            case R.id.btnChat2:
              //  tvMSG.setText("내 택배는 지금 어디쯤이야?");
                SendMsg(2);
                break;


            case R.id.btnChat3:
                SendMsg(3);
                break;


            case R.id.btnChat4:
             //   tvMSG.setText("기타 문의 사항 안내");
                SendMsg(4);
                break;



        }

    }

    private void SendMsg(int index) {
        switch (index){
            case 1:
                String msg1 = "배송 예정 시간 알려줘!";
                String ans1 = nick+"님의 택배 배송 시간은 2시!!";

                if (!TextUtils.isEmpty(msg1)) {
                //    tvMSG.setText("");
                    ChatData chatData = new ChatData();
                    chatData.userName = nick;
                    chatData.message = msg1;
                    chatData.time = System.currentTimeMillis();
                    mDatabaseReference.push().setValue(chatData);

                    chatData.userName = BotUser;
                    chatData.message = ans1;
                    chatData.time = System.currentTimeMillis();
                    mDatabaseReference.push().setValue(chatData);

                    mAdapter2.notifyDataSetChanged();

                }
                break;

            case 2:
                break;

            case 3 :
                break;

            case 4:
                break;
        }

    }



}

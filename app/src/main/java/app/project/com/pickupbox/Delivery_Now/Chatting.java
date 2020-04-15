package app.project.com.pickupbox.Delivery_Now;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import app.project.com.pickupbox.Adapter.ChatAdapter;
import app.project.com.pickupbox.Data.ChatData;
import app.project.com.pickupbox.Data.UserData;
import app.project.com.pickupbox.R;

public class Chatting extends AppCompatActivity implements View.OnClickListener {
    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    // Views
    private ListView mListView;
    private EditText mEdtMessage;
    // Values
    private ChatAdapter mAdapter;
    private String userName, nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        initViews();
        initFirebaseDatabase();
        initValues();
    }

    private void initValues() {

        //로그인 여부 확인/*---------------------------------------------------------------------------*/
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            userName = nick;

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면
            userName = "Guest " + new Random().nextInt(1000); //게스트의 이름을 Guest+랜덤 숫자로 적용한다. -> 요건 차후에 로그인 관련 액티비티 완성 후 로그인 시 닉네임을 넣을 예정

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
                mAdapter.add(chatData);
                mListView.smoothScrollToPosition(mAdapter.getCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String firebaseKey = dataSnapshot.getKey();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter.getItem(i).firebaseKey.equals(firebaseKey)) {
                        mAdapter.remove(mAdapter.getItem(i));
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
        mListView = (ListView) findViewById(R.id.list_message);
        mAdapter = new ChatAdapter(this, 0);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String toPerson = mAdapter.getItem(position).userName; //사람 아이디 알아내는 부분, 여기서 토큰을 알아내기 위한 첫 단계

                final EditText editText = new EditText(Chatting.this);
                new AlertDialog.Builder(Chatting.this)
                        .setMessage(toPerson + " 님 메세지를 보내시겠습니까")
                        .setView(editText)
                        .setPositiveButton("보내기", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendPostToFCM(toPerson, editText.getText().toString());
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"메세지 보내기가 취소되었습니다.",Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        mEdtMessage = (EditText) findViewById(R.id.edit_message);
        findViewById(R.id.btn_send).setOnClickListener(this);
    }


    /*----------------------------------------------------------------------------------------------------------------------------------*/
    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAA2pxtOMg:APA91bHTug-EJQ6SU5gU-d-IQf1Tx5UDoET8cEN3jpJx237iaBQvo3Ahej_xZbEJWMOgBAbLzphe-pcvq5kWuLfE_SlDN6_vDV5XBoJQa_cUv4g5leI-9jHJzY9ZezU62ttiO-0kXG5g";

    //FCM 푸쉬알림 보내는 메소드
    private void sendPostToFCM(final String nick, final String message) {
        mFirebaseDatabase.getReference("users")
                .child(nick) //users의 nick 밑에 가서 찾는다.
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final UserData userData = dataSnapshot.getValue(UserData.class);
                        final String ex = userData.fcmToken;
                        Toast.makeText(getApplicationContext(),"토큰 : "+ex, Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //-------------!!!!!!!!!!-------------이 부분을 나중에 가격딜 형태로 바꿀 예정----------------!!!!!!!!!!!!!!11---
                                    // FMC 메시지 생성 start
                                    JSONObject root = new JSONObject(); //root 오브젝트 생성
                                    JSONObject notification = new JSONObject(); //notification 오브젝트 생성
                                    notification.put("body", ex); //notification 오브젝트 내에 정보 집어넣기
                                    notification.put("title", "가격 딜 알림");
                                    notification.put("click_action",".DealPopup");
                                    root.put("notification", notification); //root 오브젝트에 notification 오브젝트 접어넣기
                                    root.put("to", ex); //root오브젝트에 수신자 토큰 집어넣기.
                                    //root.put("click_action", "OPEN_ACTIVITY_1"); // click_action 추가!


                                    // FMC 메시지 생성 end
                                    URL Url = new URL(FCM_MESSAGE_URL); //보내려는 URL
                                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setDoOutput(true);
                                    conn.setDoInput(true);
                                    conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                                    conn.setRequestProperty("Accept", "application/json");
                                    conn.setRequestProperty("Content-type", "application/json");
                                    OutputStream os = conn.getOutputStream();
                                    os.write(root.toString().getBytes("utf-8"));
                                    os.flush();
                                    conn.getResponseCode();
                                }

                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mChildEventListener);
    }

    @Override
    public void onClick(View v) { //채팅방에 메세지 보내기를 눌렀을 경우, DB에 메세지가 들어가는 메소드
        String message = mEdtMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            mEdtMessage.setText("");
            ChatData chatData = new ChatData();
            chatData.userName = userName;
            chatData.message = message;
            chatData.time = System.currentTimeMillis();
            mDatabaseReference.push().setValue(chatData); //userName으로 구분 가능하게 chaData를 집어넣는다.
        }


    }
}

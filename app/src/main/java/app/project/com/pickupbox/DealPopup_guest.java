package app.project.com.pickupbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import app.project.com.pickupbox.Data.UserData;

public class DealPopup_guest extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private TextView txtSenderInfo;
    private EditText etDealPrice;
    private Button btnGuestDeal, btnModifyPrice;

    private String dealprice, senderinfo;

    private String nick, myName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_deal_popup_guest);

        txtSenderInfo = findViewById(R.id.txtSenderInfo);
        etDealPrice = findViewById(R.id.txtDealPrice);
        btnGuestDeal = findViewById(R.id.btnGuestDeal);
        btnModifyPrice = findViewById(R.id.btnModifyPrice);

        etDealPrice.setEnabled(false);

        initValues(); //로그인 한 사람만 이용가능하게 처리.


        //딜을 받을 사람의 정보
        Intent intent = getIntent();
        senderinfo = intent.getStringExtra("userName");
        dealprice = intent.getStringExtra("boxPrice");

        txtSenderInfo.setText(senderinfo+"님에게");
        etDealPrice.setText(dealprice);


        btnGuestDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendPostToFCM(senderinfo, etDealPrice.getText().toString()); //FCM메세지를 보내는 함수 호출

                Toast.makeText(DealPopup_guest.this, "딜 메세지를 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(),FirstPage.class);
                intent.setAction("ACTION_LOGIN_BACK");
                startActivity(intent);

            }
        });

        btnModifyPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etDealPrice.setEnabled(true);
            }
        });
    }

    private void initValues() {//로그인 여부 확인/*---------------------------------------------------------------------------*/
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            myName = nick;

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면
            Toast.makeText(this, "로그인 부터 하고 이용해주세요", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(),FirstPage.class);
            intent.setAction("ACTION_LOGIN_BACK");
            startActivity(intent);

        }
    }




    /*----------------------------------------------------------------------------------------------------------------------------------*/
    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAA2pxtOMg:APA91bHTug-EJQ6SU5gU-d-IQf1Tx5UDoET8cEN3jpJx237iaBQvo3Ahej_xZbEJWMOgBAbLzphe-pcvq5kWuLfE_SlDN6_vDV5XBoJQa_cUv4g5leI-9jHJzY9ZezU62ttiO-0kXG5g";

    //FCM 푸쉬알림 보내는 메소드
    private void sendPostToFCM(final String nick, final String message) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference("users")
                .child(nick) //users의 nick 밑에 가서 찾는다.
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final UserData userData = dataSnapshot.getValue(UserData.class);
                        final String ex = userData.fcmToken;
                        final String dealPrice = message;
                       //Toast.makeText(getApplicationContext(),"토큰 : "+ex, Toast.LENGTH_SHORT).show();
                        Toast.makeText(DealPopup_guest.this, "메세지를 보내는중..", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //-------------!!!!!!!!!!-------------이 부분을 나중에 가격딜 형태로 바꿀 예정----------------!!!!!!!!!!!!!!11---
                                    // FMC 메시지 생성 start
                                    JSONObject root = new JSONObject(); //root 오브젝트 생성
                                    JSONObject notification = new JSONObject(); //notification 오브젝트 생성
                                    notification.put("body", dealPrice); //notification 오브젝트 내에 가격 집어넣기
                                    notification.put("title", myName+"님께서 딜을 하셨습니다.");

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
}

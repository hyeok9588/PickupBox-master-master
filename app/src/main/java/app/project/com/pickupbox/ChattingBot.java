package app.project.com.pickupbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Data.ChatData;
import app.project.com.pickupbox.Delivery_Now.Chatting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChattingBot extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    public  RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatData> chatList;


    private EditText EditText_chat;
    private Button Button_send;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ChildEventListener mChildEventListener;

    private String userName, nick;

    private String BotUser = "챗봇";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_bot);
        initValues(); //로그인 여부에 따라 이름 값 달라짐
        initFirebaseDatabase();

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        chatList = new ArrayList<>();
        mAdapter = new ChatBotAdapter(chatList, ChattingBot.this, userName);

        mRecyclerView.setAdapter(mAdapter);




    }

    private void initFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference("message");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("CHATCHAT", dataSnapshot.getValue().toString());

                ChatData chat = dataSnapshot.getValue(ChatData.class);

                ((ChatBotAdapter) mAdapter).addChat(chat);

                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               /* String firebaseKey = dataSnapshot.getKey();

                int count = mAdapter2.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter2.getItem(i).firebaseKey.equals(firebaseKey)) {
                        mAdapter2.remove(mAdapter2.getItem(i));
                        break;
                    }
                }*/
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.addChildEventListener(mChildEventListener);
    }


    private void initValues() {//로그인 여부 확인/*---------------------------------------------------------------------------*/
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            userName = nick;

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면
            userName = "Guest " + new Random().nextInt(1000); //게스트의 이름을 Guest+랜덤 숫자로 적용한다. -> 요건 차후에 로그인 관련 액티비티 완성 후 로그인 시 닉네임을 넣을 예정

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.removeEventListener(mChildEventListener);
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
                //  tvMSG.setText("PickupBox 알림 받기");
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
                    myRef.push().setValue(chatData);

                    chatData.userName = BotUser;
                    chatData.message = ans1;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);

                }
                break;

            case 2:
                String msg2 = "내 택배는 지금 어디쯤이야?";
                String ans2 = nick+"님의 택배 위치를 보여드릴게요!";

                if (!TextUtils.isEmpty(msg2)) {
                    //    tvMSG.setText("");
                    ChatData chatData = new ChatData();
                    chatData.userName = nick;
                    chatData.message = msg2;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);


                    chatData.userName = BotUser;
                    chatData.message = ans2;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);
                    mAdapter.notifyDataSetChanged();

                }

                break;

            case 3 :
                String msg3 = "PickupBox가 뭐야?";
                String ans3 = "PickupBox에 대한 설명 페이지로 이동할게요!";

                if (!TextUtils.isEmpty(msg3)) {
                    //    tvMSG.setText("");
                    ChatData chatData = new ChatData();
                    chatData.userName = nick;
                    chatData.message = msg3;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);

                    chatData.userName = BotUser;
                    chatData.message = ans3;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);


                }
                break;

            case 4:
                String msg4 = "기타 문의 사항 안내";
                String ans4 = "게시판에 글을 남겨주세요!!\n"+ nick+"님께 최대한 빠른 시간내에 답변을 드리겠습니다.";

                if (!TextUtils.isEmpty(msg4)) {
                    //    tvMSG.setText("");
                    ChatData chatData = new ChatData();
                    chatData.userName = nick;
                    chatData.message = msg4;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);

                    chatData.userName = BotUser;
                    chatData.message = ans4;
                    chatData.time = System.currentTimeMillis();
                    myRef.push().setValue(chatData);

                }


                break;
        }

    }




 /*   @Override
    public void onClick(View v) {
        //String msg = EditText_chat.getText().toString(); //msg
        if(msg != null) {
            //EditText_chat.setText("");
            ChatData chat = new ChatData();
            chat.userName = userName;
            chat.message = msg;
            myRef.push().setValue(chat);


        }

    }*/
}

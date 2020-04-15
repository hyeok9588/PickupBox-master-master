package app.project.com.pickupbox.Sample_Code;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private Button button;
    private EditText editText;
    private ListView listView;

    private ArrayList<String> list = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private ArrayAdapter<String> keyAdapter;


    private String name, chat_msg, chat_user, chat_key;

    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat"); //맨처음 DB에 들어갈때 schema명






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listView = (ListView) findViewById(R.id.listView);
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);  //채팅을 list로 구현하기 위함
        listView.setAdapter(arrayAdapter); //listview에 adapter 적용

        //로그인 여부 확인/*---------------------------------------------------------------------------*/
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        final String nick = pref.getString("email","email");

        if(nick != "email") {
            name = nick;

        }else if(nick == "email"){ //로그인 안되서 default 값인 email이 나온다면

            name = "Guest " + new Random().nextInt(1000); //게스트의 이름을 Guest+랜덤 숫자로 적용한다. -> 요건 차후에 로그인 관련 액티비티 완성 후 로그인 시 닉네임을 넣을 예정
        }

        /*-------------------------------------------------------------------*/


        button.setOnClickListener(new View.OnClickListener() { //전송 버튼 누를 경우 작동
            @Override
            public void onClick(View v) {

                Map<String, Object> map = new HashMap<String,Object>();

                String key = reference.push().getKey(); //DB에 넣어주기 위한 밑작업
                reference.updateChildren(map);

                DatabaseReference root = reference.child(key); //키 값으로 넣어준다. 이건 채팅 메세지 하나 당 고유값이다.

                Map<String, Object> objectMap = new HashMap<String, Object>(); //map형식으로 DB에 넣을 건데 그러기 위해선 string과 object에 선정해준다.
                objectMap.put("name", name); //name값 넣어줌.
                objectMap.put("text", editText.getText().toString()); //text 보낼 값 넣어줌
                objectMap.put("verify_key",key);


                root.updateChildren(objectMap); //db에 넣을 값들 선정.

                editText.setText(""); //DB에 값 보내고 나서는 메세지 입력 창 초기화
            }
        });


        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                chatConversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                chatConversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Toast.makeText(getApplicationContext(),((TextView) view).getText(),Toast.LENGTH_SHORT).show();*/

                final String data = list.get(position); //해당 포지션의 데이터를 그대로 가져온다.
                //String DelMsg = "해당 메세지 클릭 "+" position : "+ position +" <br />" + "data : " + data + "<br/>";
                //Toast.makeText(ChatActivity.this, DelMsg, Toast.LENGTH_SHORT).show();






            }
        });



    }




    private void chatConversation(DataSnapshot dataSnapshot) {  //채팅방에 이전 채팅 내역을 가져오는 과정
        Iterator i = dataSnapshot.getChildren().iterator(); //iterator 반복자롤 datafmf 하나씩 뽑아온다.

        while (i.hasNext()) { //하나씩 확인
            chat_user = (String) ((DataSnapshot) i.next()).getValue(); //user에 대한 정보(value값) 뽑아 오기
            chat_msg = (String) ((DataSnapshot) i.next()).getValue(); //msg에 대한 정보(value값) 뽑아오기
            chat_key = (String) ((DataSnapshot) i.next()).getValue();
            arrayAdapter.add(chat_user + " : " + chat_msg); //adapter에 넣어서 나열식으로 출력
        }
        arrayAdapter.notifyDataSetChanged(); //실시간 어댑터 변경 사항 반영을 위한 코드
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        String data = list.get(position);
        String DelMsg = "해당 메세지 삭제 하시겠습니까? <br/>"+" position : "+ position +" <br />" + "data : " + data + "<br/>";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("메세지 삭제");
        builder.setMessage(DelMsg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }



}

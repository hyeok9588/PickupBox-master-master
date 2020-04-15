package app.project.com.pickupbox.Frag;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.project.com.pickupbox.Adapter.ChattingBotAdapter;
import app.project.com.pickupbox.Bulltein;
import app.project.com.pickupbox.ChatBot;
import app.project.com.pickupbox.ChatBotAdapter;
import app.project.com.pickupbox.ChattingBot;
import app.project.com.pickupbox.Data.ChatData;
import app.project.com.pickupbox.Data.CurrentLocation;
import app.project.com.pickupbox.Data.DataParser;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.Data.MyItem;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Delivery_Now.Frag1;
import app.project.com.pickupbox.Explain;
import app.project.com.pickupbox.Main_Page.BoxLocationMap;
import app.project.com.pickupbox.Main_Page.PickupDetail;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.Sample_Code.MarkerClusterRenderer;

public class FirstPageFragment2 extends Fragment {
    private RecyclerView mRecyclerView;
    public  RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatData> chatList;

    private Button btnChat1, btnChat2, btnChat3, btnChat4;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ChildEventListener mChildEventListener;

    private String userName, nick;

    private String BotUser = "챗봇";

    /*
    * 채팅 참고 블로그!!! 저것도 만들자.
    * https://kutar37.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%B1%84%ED%8C%85%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0-ListView-Adapter
    * */

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.firstpage_fragment2, container, false);
        initValues(); //로그인 여부에 따라 이름 값 달라짐
        initFirebaseDatabase();

        mRecyclerView = v.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        chatList = new ArrayList<>();
        mAdapter = new ChatBotAdapter(chatList, getContext(), userName);
        mRecyclerView.setAdapter(mAdapter);

        /*onClick event -------------------------------------------------*/
        //view 선언
        btnChat1 = v.findViewById(R.id.btnChat1);
        btnChat2 = v.findViewById(R.id.btnChat2);
        btnChat3 = v.findViewById(R.id.btnChat3);
        btnChat4 = v.findViewById(R.id.btnChat4);

        //event 생성-------------------------------------------
        btnChat1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMsg(1); //1 이벤트 생성
            }
        });
        btnChat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMsg(2); //2 이벤트 생성
            }
        });
        btnChat3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("테스트1","확인중");
                SendMsg(3); //3 이벤트 생성
                Intent intent=new Intent(getActivity(),Explain.class);
                startActivity(intent);
                Log.d("테스트2","확인중");
            }
        });
        btnChat4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMsg(4); //4 이벤트 생성
                Intent intent=new Intent(getActivity(), Bulltein.class);
                startActivity(intent);
            }
        });

        /*-------------------------------------------------*/
        return v;
    }

    private void initFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance(); //파베 쓰겠단 설정
        myRef = mFirebaseDatabase.getReference("message").child(userName); //message 밑에 유저 이름 밑에 채팅
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
        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            userName = nick;

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면
            Toast.makeText(getContext(), "로그인 후 이용해주세요", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        myRef.removeEventListener(mChildEventListener);
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

}

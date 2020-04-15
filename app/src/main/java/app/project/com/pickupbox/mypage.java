package app.project.com.pickupbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Data.UserData;

public class mypage extends AppCompatActivity {

    private Button btnMypageHome, btnMypageChange;
    private TextView tvMypageId, tvMypageName, tvMypagePhone, tvMypageAddr, tvMypageGender, tvMypagePoint;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private String userId;//전역변수 / 로그인 화면에서 넘어온 사용자의 아이디 값
    private UserData userData;// DB에서 뽑아온 회원정보를 저장할 클래스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        initView();
        dbConn();

        tvMypageId.setText(userId);


        btnMypageHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),FirstPage.class);
                intent.setAction("ACTION_LOGIN_BACK");
                startActivity(intent);
            }
        });
    }

    public void initView(){
        btnMypageHome = findViewById(R.id.btnMypageHome);
        btnMypageChange = findViewById(R.id.btnMypageChange);

        tvMypageId = (TextView)findViewById(R.id.tvMypageId);
        tvMypageName=(TextView)findViewById(R.id.tvMypageName);
        tvMypagePhone=(TextView)findViewById(R.id.tvMypagePhone);
        tvMypageAddr=(TextView)findViewById(R.id.tvMypageAddr);
        tvMypageGender= (TextView)findViewById(R.id.tvMypageGender);
        tvMypagePoint=(TextView)findViewById(R.id.tvMypagePoint);
    }


    public void dbConn(){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.getReference("users")
                .child(userId) //users의 nick 밑에 가서 찾는다.
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        userData = dataSnapshot.getValue(UserData.class);
                        tvMypageName.setText(userData.userName);
                        tvMypagePhone.setText(userData.userPhone);
                        tvMypageAddr.setText(userData.userAddr);
                        tvMypageGender.setText(userData.userGender);



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
        /*여기까지 회원 아이디 별 DB연동--------------------------------------------------------------------------------------*/





}

package app.project.com.pickupbox.Frag;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.project.com.pickupbox.Data.UserData;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.R;


public class FirstPageFragment_mypage extends Fragment {

    private Button btnMypageHome, btnMypageChange;
    private TextView tvMypageId, tvMypageName, tvMypagePhone, tvMypageAddr, tvMypageGender, tvMypagePoint;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private String userId;//전역변수 / 로그인 화면에서 넘어온 사용자의 아이디 값
    private UserData userData;// DB에서 뽑아온 회원정보를 저장할 클래스


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_first_page_mypage, container, false);

        Intent intent = getActivity().getIntent();
        userId = intent.getStringExtra("userId");

        initView(v);
        dbConn();

        tvMypageId.setText(userId);


        btnMypageHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FirstPage.class);
                intent.setAction("ACTION_LOGIN_BACK");
                startActivity(intent);
            }
        });
        return v;
    }

    public void initView(View v){
        btnMypageHome = v.findViewById(R.id.btnMypageHome);
        btnMypageChange = v.findViewById(R.id.btnMypageChange);

        tvMypageId = (TextView)v.findViewById(R.id.tvMypageId);
        tvMypageName=(TextView)v.findViewById(R.id.tvMypageName);
        tvMypagePhone=(TextView)v.findViewById(R.id.tvMypagePhone);
        tvMypageAddr=(TextView)v.findViewById(R.id.tvMypageAddr);
        tvMypageGender= (TextView)v.findViewById(R.id.tvMypageGender);
        tvMypagePoint=(TextView)v.findViewById(R.id.tvMypagePoint);
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

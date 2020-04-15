package app.project.com.pickupbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import app.project.com.pickupbox.Adapter.LocationListAdapter;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Frag.FirstPageFragment1;
import app.project.com.pickupbox.Frag.FirstPageFragment1_map;
import app.project.com.pickupbox.Frag.FirstPageFragment2;
import app.project.com.pickupbox.Frag.FirstPageFragment3;
import app.project.com.pickupbox.Frag.FirstPageFragment4;
import app.project.com.pickupbox.Frag.FirstPageFragment5;
import app.project.com.pickupbox.Frag.FirstPageFragment6;
import app.project.com.pickupbox.Frag.FirstPageFragment_Loginokay;
import app.project.com.pickupbox.Frag.FirstPageFragment_mypage;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirstPage extends AppCompatActivity {

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    BottomNavigationView bottomNavigationView;
    FirstPageFragment1 fragment1;
    FirstPageFragment1_map fragment1_map;
    FirstPageFragment2 fragment2;
    FirstPageFragment3 fragment3;
    FirstPageFragment4 fragment4;
    FirstPageFragment5 fragment5;
    FirstPageFragment6 fragment6;
    FirstPageFragment_Loginokay fragment_loginokay;
    FirstPageFragment_mypage fragment_mypage;



    private String userName, nick;
    private Boolean checkResult;

    //Bundle tab6/3
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);


        //fragment
        fragment1 = new FirstPageFragment1(); //홈_순번1
        fragment4 = new FirstPageFragment4(); //배송 서비스 순번2
        fragment6 = new FirstPageFragment6(); //택배 추가1_ 순번3
        fragment3 = new FirstPageFragment3(); //택배 추가2_순번3
        fragment2 = new FirstPageFragment2(); //채팅 서비스_순번4
        fragment5 = new FirstPageFragment5(); //유저 관리_ 순번5

        fragment_loginokay = new FirstPageFragment_Loginokay();
        fragment_mypage = new FirstPageFragment_mypage();
        fragment1_map = new FirstPageFragment1_map();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment1).commitAllowingStateLoss();

        //택배 등록 완료 부분 및 로그인 부분에서 수정을 요구하거나 뒤로가기를 누를 경우 INTENT 동작
        final Intent intent = getIntent();
        if (intent.getAction().equals("ACTION_ADD_BACK")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout,fragment3).commitAllowingStateLoss();
        }
        else if (intent.getAction().equals("ACTION_LOGIN_BACK")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout,fragment1).commitAllowingStateLoss();
        }

        else if (intent.getAction().equals("ACTION_LOGIN_FALSE")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout,fragment5).commitAllowingStateLoss();
        }
        else if (intent.getAction().equals("ACTION_MYPAGE")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout,fragment_mypage).commitAllowingStateLoss();
        }
        else if (intent.getAction().equals("ACTION_LOGIN_OKAY")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout,fragment_loginokay).commitAllowingStateLoss();
        }
        else if (intent.getAction().equals("ACTION_MAIN_MAP")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout,fragment1_map).commitAllowingStateLoss();
        }

        //-----------------------------------------------------------------------------------------

        //bottomnavigationview의 아이콘을 선택 했을때 원하는 프래그먼트가 띄워질 수 있도록 리스너를 추가합니다.
        bottomNavigationView.setOnNavigationItemSelectedListener(   new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.tab1:{

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout,fragment1).commitAllowingStateLoss();
                        return true;
                    }

                    case R.id.tab2:{
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout,fragment2).commitAllowingStateLoss();
                        return true;
                    }

                    //상품 등록
                    case R.id.tab3:{
                       if (CheckLogin()==true){ //로그인 되어있다면
//                           DBConnTask dbConnTask = new DBConnTask();
//                           bundle = new Bundle();
//                           adapter = new LocationListAdapter(getApplicationContext(), dataList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
//                           dataList = new ArrayList<>(); //LocationExample 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)
//                           dbConnTask.execute();
//                           fragment6.setArguments(bundle);
                           getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment3).commitAllowingStateLoss();


                       } else{ //로그인 안되어있다면
                           Toast.makeText(getApplicationContext(),"로그인 이후 이용 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                           getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment5).commitAllowingStateLoss();
                       }

                        return true;
                    }

                    //배송 서비스
                    case R.id.tab4:{
                        if (CheckLogin()==true){ //로그인 되어있다면
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_layout,fragment4).commitAllowingStateLoss();
                        }else{ //로그인 안되어있다면
                            Toast.makeText(getApplicationContext(),"로그인 이후 이용 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_layout,fragment5).commitAllowingStateLoss();
                        }
                        return true;
                    }

                    case R.id.tab5:{
                        if (CheckLogin()==true){ //로그인 되어있다면
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_layout,fragment_loginokay).commitAllowingStateLoss();
                        }else{ //로그인 안되어있다면
                            //Toast.makeText(getApplicationContext(),"로그인 이후 이용 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_layout,fragment5).commitAllowingStateLoss();
                        }
                        return true;
                    }
                }

                return false;
            }
        });


    }

    public boolean CheckLogin(){
        //로그인 여부 확인/*---------------------------------------------------------------------------*/
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
        nick = pref.getString("email","def"); //이름이 email인 key값이 default값이라면 def, 아니라면 nick에 email값이 저장됨.

        if(nick != "def") { //로그인이 되어서 def가 아닌 다른 값이 들어온다면.
            userName = nick;
            checkResult = true;

        }else if(nick == "def"){ //로그인 안되서 default 값인 email이 나온다면

            checkResult = false;


        }
        return checkResult;
    }



    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT>21){
            finishAndRemoveTask();
        }else{
            finish();

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
            editor.clear();
            editor.commit();
        }

        super.onBackPressed();
    }




    @Override
    protected void onPause() {
        Log.d("액티비티 실행","acitvity_pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("액티비티 실행","acitvity_Destroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d("액티비티 실행","acitvity_stop");
        super.onStop();
    }
}

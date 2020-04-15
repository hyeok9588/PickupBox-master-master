package app.project.com.pickupbox.Main_Page;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Map.DelBoxLocationMap;
import app.project.com.pickupbox.Delivery_Now.TapActivity;
import app.project.com.pickupbox.User_Management.LoginActivity;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.Register_Box.AddBox;
import app.project.com.pickupbox.Adapter.SwipeRecyclerViewAdapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.baoyz.widget.PullRefreshLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;

public class PickupMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager; //리사이클러뷰 사용을 위한 설정

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    //private List<LocationExample> locationList;
    private List<UserBoxInfo> boxList;

    private FloatingActionButton button1;
    private Button btnOBM, btnOBT, btnOBS;
    private ScrollView scrollView;
    Intent intent;

    //private SwipeRefreshLayout swipeLayout;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_main);


        btnOBM = findViewById(R.id.orderByMoney);
        btnOBT = findViewById(R.id.orderByTime);
        btnOBS = findViewById(R.id.orderBySize);

        final PullRefreshLayout loading;
        loading = (PullRefreshLayout)findViewById(R.id.swipeRefreshLayout);


        loading.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);



        loading.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loading.setRefreshing(false);
                        dbConn();
                    }
                },100);
            }
        });




        button1 = findViewById(R.id.btnGoMap); //버튼 클릭시 frag2로 이동 후 지도를 보여준다.
        intent = new Intent(getBaseContext(),BoxLocationMap.class);

        //----------------------------------------------네비게이터------------------------------------------//
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        String k = pref.getString("memberId","memberId");

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.togglebar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        appBarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawer.openDrawer(GravityCompat.START);
            }
        });




        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        View nav_header_view = navigationView.getHeaderView(0);


        //----------------------------------------------네비게이터------------------------------------------//

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView7);

        recyclerView.setHasFixedSize(true); //리사이클러 뷰 기존성능 강황
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        boxList = new ArrayList<>(); //article 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)

       dbConn();

        adapter = new SwipeRecyclerViewAdapter(this, (ArrayList<UserBoxInfo>) boxList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        recyclerView.setAdapter(adapter);  //리사이클러뷰에 어뎁터 연결




        button1.setOnClickListener(new View.OnClickListener() { //버튼 클릭시 지도로 넘어감
            @Override
            public void onClick(View v) {

                startActivity(intent);

            }
        });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d("FCM Log", "FCM 토큰 : " + token);
                        //Toast.makeText(PickupMain.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        btnOBM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbConnPlusOrder("BoxPrice");
            }
        });

        btnOBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbConnPlusOrder("PickupTime");
            }
        });
        btnOBS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbConnPlusOrder("BoxSize");
            }
        });

    }


    public void dbConn(){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("BoxList");  //db테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//파이어베이스 데이터 받아오는 곳
                boxList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                    UserBoxInfo boxinfo = snapshot.getValue(UserBoxInfo.class); //만들어뒀던 article 객체에 데이터 담기

                    DatabaseReference pushPostRef =  snapshot.getRef();
                    boxinfo.setKeyValue(pushPostRef.getKey());

                    /*-------------------------고유값을 가져오는 곳----------------------------------*/
                    /*DatabaseReference pushPostRef = databaseReference.getRef().push();
                    boxinfo.setKeyValue(pushPostRef.getKey());*/
                    /*----------------------------------------------------------*/

                    boxList.add(boxinfo);
                }

                intent.putParcelableArrayListExtra("boxList",(ArrayList<UserBoxInfo>) boxList); //지도 보여줄때 리스트로 보낼 것.

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("Frag1", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

    }


    public void dbConnPlusOrder(String order){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("BoxList");  //db테이블 연결
        String orders = order ;

        databaseReference.orderByChild(orders).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boxList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                    UserBoxInfo boxinfo = snapshot.getValue(UserBoxInfo.class); //만들어뒀던 article 객체에 데이터 담기

                    DatabaseReference pushPostRef =  snapshot.getRef();
                    boxinfo.setKeyValue(pushPostRef.getKey());

                    /*-------------------------고유값을 가져오는 곳----------------------------------*/
                    /*DatabaseReference pushPostRef = databaseReference.getRef().push();
                    boxinfo.setKeyValue(pushPostRef.getKey());*/
                    /*----------------------------------------------------------*/

                    boxList.add(boxinfo);
                }

                intent.putParcelableArrayListExtra("boxList",(ArrayList<UserBoxInfo>) boxList); //지도 보여줄때 리스트로 보낼 것.

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

    }


    //----------------------------------------------네비게이터------------------------------------------//
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        String k = pref.getString("memberId","memberId");*/

        NavigationView navigationView1 = (NavigationView) findViewById(R.id.nav_view);
        Menu nv = navigationView1.getMenu();
        MenuItem item = nv.findItem(R.id.menu1);



        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.main2, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();




        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        String k = pref.getString("memberId","memberId");

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu1) { //로그인
            Intent intent = new Intent(PickupMain.this, LoginActivity.class);
            startActivity(intent);

        } else if (id == R.id.menu2) {//나의 배송 정보
            Intent intent = new Intent(PickupMain.this, TapActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu3) { //상품 등록하기
            Intent intent = new Intent(PickupMain.this, AddBox.class);
            startActivity(intent);
        } else if(id == R.id.menu4) { //과거이력
            Intent intent = new Intent(PickupMain.this, PickupMain.class);
            startActivity(intent);
        }else if(id == R.id.menu5) { //배송맵
            Intent intent = new Intent(PickupMain.this, DelBoxLocationMap.class);
            startActivity(intent);
        }else if(id == R.id.menu6) { //로그아웃
            editor.clear();
            editor.commit();
            Intent intent = new Intent(PickupMain.this, PickupMain.class);
            startActivity(intent);
        }else if(id==R.id.menu7){ //공지사항
            Intent intent = new Intent(PickupMain.this, PickupMain.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //----------------------------------------------네비게이터------------------------------------------//
}

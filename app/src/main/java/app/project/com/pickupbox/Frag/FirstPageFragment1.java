package app.project.com.pickupbox.Frag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.DefaultSliderView;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.project.com.pickupbox.Adapter.SwipeRecyclerViewAdapter;
import app.project.com.pickupbox.BottomSheetDialog;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.Loading_AD;
import app.project.com.pickupbox.Main_Page.BoxLocationMap;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.service.NetworkStatus;


public class FirstPageFragment1 extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager; //리사이클러뷰 사용을 위한 설정

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    //private List<LocationExample> locationList;
    private List<UserBoxInfo> boxList;

    private Button button1;
    private Button btnOBM, btnOBT, btnOBP;
    private Button btnSC;
    private ScrollView scrollView;
    private Intent intent;

    private SliderLayout sliderLayout;
    private String userName, nick;
    private GoogleApiClient googleApiClient = null;
    ViewGroup viewGroup;
    private PullRefreshLayout loading;

    private FirstPageFragment1_map fragment1_map;
    private Bundle bundle;

    private ImageView iv_visalizeMap;//데이터 시각화 자료 출력하는 부분
    private ShimmerFrameLayout mShimmerViewContainer; //로딩 중 itemview

    private LinearLayout main_frame;


    private FirstPageFragment1 fragment1;

    public static final String Intent_Filter_Name = "Sort_Rule";
    private BottomSheetDialog bottomSheetDialog;

    private FirstPage activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.firstpage_fragment1, container, false);
        btnSC = (Button) v.findViewById(R.id.SearchCase);
        btnOBM = v.findViewById(R.id.btnOBM); //저렴순
        btnOBT = v.findViewById(R.id.btnOBT); //최신순
        btnOBP = v.findViewById(R.id.btnOBP); //비싼순

        activity = new FirstPage();

        button1 = v.findViewById(R.id.btnGoMap); //버튼 클릭시 frag2로 이동 후 지도를 보여준다.
        button1.setVisibility(View.INVISIBLE);


        mShimmerViewContainer = v.findViewById(R.id.shimmer_view_container);

        fragment1_map = new FirstPageFragment1_map(); //fragment map 넘어가기 위한.

        bundle = new Bundle();
        fragment1_map.setArguments(bundle);

        iv_visalizeMap = (ImageView) v.findViewById(R.id.iv_visualizeMap);
        //viewMap();

        if(getArguments() != null) {
            String orderBy = getArguments().getString("key"); // 전달한 key 값

        }




        sliderLayout = v.findViewById(R.id.imageSlider);
        sliderLayout.setIndicatorAnimation(IndicatorAnimations.SLIDE);
        sliderLayout.setScrollTimeInSec(2);

        setSliderViews(); //광고 배너가 돌아가는 기능


        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView7);
        scrollView = v.findViewById(R.id.scollview);

        /*bottomSheet */
        //final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        //bottomSheetDialog.setContentView(R.layout.bottomsheet_content);

        /*-------------------------------------*/

        /*loading 기능 -----------------------------------------------*/
        loading = (PullRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);

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
                }, 1000);
            }
        });

        /*-----------------------------------------------*/


        recyclerView.setHasFixedSize(true); //리사이클러 뷰 기존성능 강황
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        boxList = new ArrayList<>(); //article 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)

        dbConn(); //db연결

        adapter = new SwipeRecyclerViewAdapter(getContext(), (ArrayList<UserBoxInfo>) boxList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        recyclerView.setAdapter(adapter);  //리사이클러뷰에 어뎁터 연결

        button1.setVisibility(View.VISIBLE);
        //버튼 클릭시 지도로 넘어감
        intent = new Intent(getContext(), Loading_AD.class);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //번들객체 생성 값 전달 위해

                /*getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_layout,fragment1_map).commitAllowingStateLoss();*/
                //intent.setAction("ACTION_MAIN_MAP");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //FLAG_ACTIIVTY_CLEAR_TASK는 기존에 쌓여있던 task(stack이 모여 형성하는 작업의 단위(?))를 모두 삭제하는 조건(?)을 받는 flag 상수다.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//이때 FLAG_ACTIVITY_NEW_TASK로 task를 새로 생성한다

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                        startActivity(intent);

                    }
                }, 100);


                getActivity().overridePendingTransition(0, 0);


            }
        });
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            //Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        //Log.d("FCM Log", "FCM 토큰 : " + token);
                        //Toast.makeText(PickupMain.this, token, Toast.LENGTH_SHORT).show();
                    }
                });


        btnSC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog();
                bottomSheetDialog.show(getFragmentManager(),"approval");

            }

                /*
                btnOBM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbConnPlusOrder("BoxPrice");
                        bottomSheetDialog.cancel();

                    }
                });

                btnOBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbConnPlusOrder("PickupTime");
                        bottomSheetDialog.cancel();

                    }
                });

                btnOBP.setOnClickListener(new View.OnClickListener() { //사이즈별
                    @Override
                    public void onClick(View v) {
                        dbConnPlusOrder("BoxSize");
                        bottomSheetDialog.cancel();

                    }
                });

*/


        });

        return v;
    }


    private void setSliderViews() {
        for(int i=0;i<=2;i++){

            DefaultSliderView sliderView=new DefaultSliderView(getContext());

            switch (i){
                case 0:
                    sliderView.setImageDrawable(R.drawable.adone);
                    break;
                case 1:
                    sliderView.setImageDrawable(R.drawable.adtwo);
                    break;
                case 2:
                    sliderView.setImageDrawable(R.drawable.adone);
                    break;
            }
            sliderView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            final int finalI=i;
            sliderView.setOnSliderClickListener(new SliderView.OnSliderClickListener(){
                public void onSliderClick(SliderView sliderView){
                    //Toast.makeText(getContext(),"slider"+(finalI+1),Toast.LENGTH_SHORT).show();
                }
            });
            sliderLayout.addSliderView(sliderView);
        }

    }

    public void viewMap(){
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://boxstorage-8f972.appspot.com");
        StorageReference storageRef = storage.getReference(); //생성된 Firebase Storage 참조하는 storage 생성

        StorageReference imageRef = storageRef.child("visualize/map_visualize_data.png"); //Storage 내부의 images 폴더 안의 image.jpg 파일명을 가리키는 참조 생성
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(FirstPageFragment1.this).load(uri).into(iv_visalizeMap);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(getContext(),"가져올 사진 정보 없음",Toast.LENGTH_SHORT).show();
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


                    boxList.add(boxinfo);
                }

                intent.putParcelableArrayListExtra("boxList",(ArrayList<UserBoxInfo>) boxList);
                adapter.notifyDataSetChanged();

                // Stopping Shimmer Effect's animation after data is loaded to ListView
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("Frag1", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

    }

    private BroadcastReceiver someBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String rule = intent.getStringExtra("Sort_Rule");
            btnSC.setText(rule);
            if (rule.contains("가격")){
                dbConnPlusOrder("BoxPrice");

            }else if(rule.contains("시간")){
                dbConnPlusOrder("PickupTime");

            }else if(rule.contains("크기")){
                dbConnPlusOrder("BoxSize");
            }
            //bottomSheetDialog.dismiss();

        }
    };


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(someBroadcastReceiver, new IntentFilter("Sort_Rule"));
        Log.d("프레그 테스트", "on resume");
        mShimmerViewContainer.startShimmerAnimation();

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(someBroadcastReceiver);
        Log.d("프레그 테스트", "on pause");
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }

    public void CheckNetwork(){  //인터넷 연결 상태 확인하기.
        int status = NetworkStatus.getConnectivityStatus(getContext());
        if(status == NetworkStatus.TYPE_MOBILE){
            Log.d("NetworkStatus","mobile");
        }else if (status == NetworkStatus.TYPE_WIFI){
            Log.d("NetworkStatus","WIFI");
        }else {
            Log.d("NetworkStatus","no network");
            Toast.makeText(getContext(), "인터넷 연결 후 다시 접속해주세요.", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("테스트","onAttach");
    }



    public void dbConnPlusOrder(String order){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        adapter = new SwipeRecyclerViewAdapter(getContext(), (ArrayList<UserBoxInfo>) boxList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("BoxList");  //db테이블 연결
        String orders = order.trim() ;
        Log.d("테스트",orders);

        boxList = new ArrayList<>(); //article 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)

        databaseReference.orderByChild(orders).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boxList.clear();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                    UserBoxInfo new_boxInfo = snapshot.getValue(UserBoxInfo.class); //만들어뒀던 article 객체에 데이터 담기

                    boxList.add(new_boxInfo);


                }


                intent.putParcelableArrayListExtra("boxList",(ArrayList<UserBoxInfo>) boxList); //지도 보여줄때 리스트로 보낼 것.

                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);  //리사이클러뷰에 어뎁터 연결

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

    }

    @Override
    public void onStop() {
        super.onStop();
    }


}

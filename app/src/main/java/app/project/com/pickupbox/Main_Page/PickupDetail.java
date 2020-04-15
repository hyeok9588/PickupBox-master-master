package app.project.com.pickupbox.Main_Page;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Adapter.DetailRecyclerViewAdapter;
import app.project.com.pickupbox.Adapter.SwipeRecyclerViewAdapter;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Deal.DealPopup;
import app.project.com.pickupbox.DealPopup_guest;
import app.project.com.pickupbox.R;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PickupDetail extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private RecyclerView DrecyclerView;
    private RecyclerView.Adapter Dadapter;
    private RecyclerView.LayoutManager DlayoutManager;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);  //기본 지도 표시 위도 경도
    private static final String TAG = "PICKUP DETAIL";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    ArrayList<LatLng> MarkerPoints; // 마커 저장

    private GoogleMap mMap = null;
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private Marker currentMarker = null;

    private LatLng currentPossition;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    /*private List<LocationExample> locationList;*/
    //private  ArrayList<LocationExample> list;
    private List<UserBoxInfo> boxList;
    private List<UserBoxInfo> detailList;


    private TextView tvDetailName, tvDetailDistance, tvDetailPTime, tvDetailDuration,tvDetailSize,tvDetailPrice;
    private Button btnDetailDeal;

    String b_latitude, b_longitude, b_boxName, b_pickupTime, b_duration, b_distance, b_boxSize, b_boxPrice,b_userName;
    String b_keyValue;
    private int box_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_pickup_detail);

        //init View
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailSize = findViewById(R.id.tvDetailSize);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailPTime = findViewById(R.id.tvDetailPTime);
        tvDetailDuration = findViewById(R.id.tvDetailDuration);
        tvDetailDistance = findViewById(R.id.tvDetailDistance);
        btnDetailDeal = findViewById(R.id.btnDetailDeal);

        final Intent intent = getIntent();
        /*---intent의 flag값으로 구분을 해주자----*/
        if (intent.getIntExtra("FLAG",0) == 0){
            Log.d(PickupDetail.TAG, "no data intent");
        }
        if ((intent.getIntExtra("FLAG",0) == 1) ||
                (intent.getIntExtra("FLAG",0) == 2) ) {
            Log.d(PickupDetail.TAG, "메인이나 지도1 FLAG");

            //1번 케이스--------메인에서 바로 넘어온 경우,
            b_boxName = intent.getStringExtra("boxName"); //상품 이름
            b_boxSize = intent.getStringExtra("boxSize"); //상품 사이즈
            b_boxPrice = intent.getStringExtra("boxPrice"); //상품 가격
            b_latitude = intent.getStringExtra("myLatitude"); //상품 위치 위도
            b_longitude = intent.getStringExtra("myLongitude"); //상품 위치 경도
            b_pickupTime = intent.getStringExtra("pickupTime"); //상품 배송원하는 시간
            b_duration = intent.getStringExtra("duration"); //배송지까지의 거리
            b_distance = intent.getStringExtra("distance"); //배송지까지 걸리는 시간
            b_userName = intent.getStringExtra("userName"); //배송지까지 걸리는 시간

            tvDetailName.setText(b_boxName);
            tvDetailPTime.setText(b_pickupTime);
            tvDetailDuration.setText(b_duration);
            tvDetailDistance.setText(b_distance);
            tvDetailSize.setText(b_boxSize);
            tvDetailPrice.setText(b_boxPrice+"원");
        }
        else if(intent.getIntExtra("FLAG",0)==2){
            Log.d(PickupDetail.TAG, "지도2 FLAG");

            //2번 케이스------BoxLocationMap에서 마커로 넘어온 경우,
            b_keyValue = intent.getStringExtra("keyValue"); //상품 키값
            Log.d("상품 키값",b_keyValue);
            getDatabyKeyValue();
        }

        MarkerPoints = new ArrayList<>(); // 마커 저장 시켜놓을 리스트
        MarkerPoints.clear();

        mapView = (MapView)findViewById(R.id.mapV2);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        /*recyclerview 설정-----------------------------------------------------------------------------------------------------------------*/

        DrecyclerView = (RecyclerView)findViewById(R.id.recyclerViewDetail);
        DrecyclerView.setHasFixedSize(true); //리사이클러 뷰 기존성능 강황
        DlayoutManager = new LinearLayoutManager(this);
        DrecyclerView.setLayoutManager(DlayoutManager);
        boxList = new ArrayList<>(); //article 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)
        detailList = new ArrayList<>();

        //dbConn(); //비슷한 가격대의 상품 찾아오기 위함

        Dadapter = new DetailRecyclerViewAdapter(this, (ArrayList<UserBoxInfo>) boxList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        DrecyclerView.setAdapter(Dadapter);  //리사이클러뷰에 어뎁터 연결

        /*recyclerview 설정-----------------------------------------------------------------------------------------------------------------*/

        /*비슷한 조건 평균 배송 가격 추천 ---------------------------------------------------------------------------------------------*/

        NumberPicker picker = findViewById(R.id.number_picker);
        String[] data = new String[6];

        for (int i=0; i<6; i++) {
            data[i] = String.valueOf(box_price + (300 * i));
        }

        picker.setMinValue(0);
        picker.setMaxValue(data.length-1);
        picker.setDisplayedValues(data);

        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setOnLongPressUpdateInterval(100);

        /*---------------------------------------------------------------------------------------------*/

        /*딜하기 버튼----------------------------------------------------------------------------------*/
        btnDetailDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(PickupDetail.this, "딜을 해 봅시다!", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(getApplicationContext(), DealPopup_guest.class);
                intent1.putExtra("userName",b_userName);
                intent1.putExtra("boxPrice",b_boxPrice);
                startActivity(intent1);
            }
        });

        //유사 상품 추천의 경우, 1.배송위치가 비슷한 것 // 2. 사이즈가 같은 것. // 3. 소요거리가 비슷한 것



    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    public void dbConn(){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("BoxList");  //db테이블 연결
        //databaseReference.orderByChild("BoxSize").equalTo(b_boxSize); //사이즈가 같은 애들
        databaseReference.orderByChild("BoxSize").equalTo(b_boxSize).limitToFirst(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boxList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
// /*-------------------------수정이 필요한 코드---------------아이디별 구분 코드 작성 필요-------------------------------------------------------------------*/
                    UserBoxInfo boxinfo = snapshot.getValue(UserBoxInfo.class); //만들어뒀던 article 객체에 데이터 담기
                    if (boxinfo.getUserName() != b_userName ){
                        boxList.add(boxinfo);
                    }
                }

                Dadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("PickupDetail", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

    }

    public void getDatabyKeyValue(){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("BoxList");  //db테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//파이어베이스 데이터 받아오는 곳
                detailList.clear();
                Log.d("키", "getDB입장");
                //UserBoxInfo detailInfo = dataSnapshot.getValue(UserBoxInfo.class);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { //반복문으로 데이터 가져오는 곳
                    UserBoxInfo detailInfo = snapshot.getValue(UserBoxInfo.class); //만들어뒀던 article 객체에 데이터 담기
                    Log.d("키", detailInfo.getKeyValue());

                    if (b_keyValue.trim().contentEquals(detailInfo.getKeyValue())) {
                        Log.d("키", "키값은 일치");

                        //다른 메소드에 쓰일 변수 값 저장
                        box_price = Integer.parseInt(detailInfo.getBoxPrice());
                        b_latitude = detailInfo.getMyLatitude().trim();
                        b_longitude = detailInfo.getMyLongitude().trim();
                        b_boxName = detailInfo.getBoxName();
                        b_boxSize = detailInfo.getBoxSize();
                        b_boxPrice = detailInfo.getBoxPrice();
                        b_userName = detailInfo.getUserName();

                        Log.d("키", detailInfo.getBoxName());

                        //textview 표현될 값들
                        tvDetailName.setText(detailInfo.getBoxName());
                        tvDetailPTime.setText(detailInfo.getPickupTime());
                        tvDetailDuration.setText(detailInfo.getDuration());
                        tvDetailDistance.setText(detailInfo.getDistance());
                        tvDetailSize.setText(detailInfo.getBoxSize());
                        tvDetailPrice.setText(detailInfo.getBoxPrice() + "원");
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("PickupDetail", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

    }



    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet){
        if(currentMarker != null) currentMarker.remove();

        if (location!=null){
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());   //현재위치 위경도 가져오기

            MarkerOptions markerOptions = new MarkerOptions(); //maker option 사용 설정
            markerOptions.position(currentLocation); //현재 위치로 표시
            markerOptions.title(markerTitle); //이름을 markerTitle로 표시
            markerOptions.snippet(markerSnippet); //클릭시 보이는 이름
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));    //현재 위경도 가져왔다면 파란색으로 표시
            currentMarker = this.mMap.addMarker(markerOptions);

            //this.mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation)); //현재위치로 카메라 이동.
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION); //현재 위치 가져오지 못했으니 기본 위치로 지정
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); //현재 위경도 가져오지 못한다면 빨간색으로 표시
        currentMarker = this.mMap.addMarker(markerOptions);

        //this.mMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION)); //기본 위치로 카메라 이동
        //Toast.makeText(getApplicationContext(), "위치 정보 확인 중...", Toast.LENGTH_LONG).show();





    }


    @Override
    public void onStart() {
        if(googleApiClient != null && googleApiClient.isConnected() == false) {
            Log.d(TAG, "onStart : mGoogleApiClient connect");
            googleApiClient.connect();
        }
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onStop() {
        if (googleApiClient!=null && googleApiClient.isConnected())
            googleApiClient.disconnect();
        super.onStop();
        mapView.onStop();



    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        if (googleApiClient!=null)
            googleApiClient.connect();
        super.onResume();
        mapView.onResume();



    }


    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();

        if (googleApiClient!=null && googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        googleApiClient.disconnect();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();

    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();


        if (googleApiClient!=null){
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);

            if (googleApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
                googleApiClient.disconnect();
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap; //구글 맵 사용하겠다.

        //런타임 퍼미션 요청 대화상자나 GPS활성 요청 대화 상자 보이기전에 초기위로 이동.
        setCurrentLocation(null,"위치정보 가져올 수 없음.","위치 퍼미션과 GPS활성 여부 확인");

        //나침반 사용
        mMap.getUiSettings().setCompassEnabled(true);
        //매끄럽게 이동
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //API 23이상일 경우 런타임 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //사용권한 체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED){
                //사용권한 없다면
                //권한 재요청
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }else {
                //사용권한 있다면
                if (googleApiClient == null){
                    buildGoogleApiClient();  //클래스 호출
                }

                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
            }
        }else{
            if (googleApiClient==null){
                buildGoogleApiClient();
            }

            mMap.setMyLocationEnabled(true);
        }

        /*이 부분에 대해선 위에서 기본 위치 지정 했음 으로 없어도 되는 코드*/
        LatLng sihwa = new LatLng(37.342554, 126.735857); //sihwa에 대한 위치  정보.
        mMap.addMarker(new MarkerOptions().position(sihwa).title("시화산업단지")); //시화에 대한 마커를 넣겠다.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sihwa)); //카메라를 시화로 움직이겠다.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //카메라를 줌인 하겠다.

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        LatLng latLng = new LatLng(Double.parseDouble(b_latitude),Double.parseDouble(b_longitude));
        mMap.addMarker(new MarkerOptions().position(latLng).title(b_boxName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //카메라를 줌인 하겠다.
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        }
    }

    private void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this,this)
                .build();
        googleApiClient.connect();
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkLocationServicesStatus()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("위치 서비스 활성화");
            builder.setMessage("앱 사용을 위해 위치 설정을 수정해 주세요");
            builder.setCancelable(true);
            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }else{
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(googleApiClient, locationRequest, this);

            this.mMap.getUiSettings().setCompassEnabled(true);
            this.mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG,"onConnectionSuspended() : Google Play Services" + "connection lost. Cause : network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG,"onConnectionSuspended() : Google Play Services" + "connection lost. Cause : service disconnected.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Location location = new Location("");
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude(DEFAULT_LOCATION.longitude);

        setCurrentLocation(location, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS활성 여부 확인");
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged call..");

        currentPossition = new LatLng(location.getLatitude(), location.getLongitude());
        String markerTitle = getCurrentAddress(currentPossition);
        String markerSnippet = "위도 : " + String.valueOf(location.getLatitude()) + " 경도 : " + String.valueOf(location.getLongitude());
        setCurrentLocation(location,markerTitle,markerSnippet);

    }


    public String getCurrentAddress(LatLng latlng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getApplicationContext(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getApplicationContext(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getApplicationContext(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

}

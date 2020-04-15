package app.project.com.pickupbox.Main_Page;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import app.project.com.pickupbox.Data.CurrentLocation;
import app.project.com.pickupbox.Data.MyItem;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Data.DataParser;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.R;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import javax.net.ssl.HttpsURLConnection;

public class BoxLocationMap extends AppCompatActivity  implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    public static Context mContext;//메인에서 가져다 쓰기위함

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);  //기본 지도 표시 위도 경도
    private static final String TAG = "google map example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 300000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 300000; //지도 갱신 시간 30만초 = 5분

    private ClusterManager<MyItem> mItemClusterManager;

    int ccnt = 0; // 길찾기 라인 색 다르게 체크
    ArrayList<LatLng> MarkerPoints; // 마커 저장

    private GoogleMap mMap = null;
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private Marker currentMarker = null;

    private Button btnRoad;
    private ImageButton btnGoBack;


    private LatLng currentPossition;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private ArrayList<UserBoxInfo> boxList;
    private ArrayList<CurrentLocation> disList;

    private int MAP_PAGE_FLAG_1 = 2; //그냥 마커에서 넘어가는 경우
    private int MAP_PAGE_FLAG_2 = 3; //3km이내를 구하고 넘어가는 경우

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_box_location_map);
     

        mContext = this; //메인에서 가져다 쓰기위함
        btnRoad = (Button)findViewById(R.id.btnChk);
        btnGoBack = (ImageButton) findViewById(R.id.btnGoBack);

        btnRoad.setVisibility(View.INVISIBLE);

        boxList = getIntent().getParcelableArrayListExtra("boxList");


        MarkerPoints = new ArrayList<>(); // 마커 저장 시켜놓을 리스트
        MarkerPoints.clear();

        mapView = (MapView)findViewById(R.id.mapV);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FirstPage.class);
                intent.setAction("ACTION_LOGIN_BACK");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  //스택 쌓이지 않게 지워주자
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //새로운 activity로 생성해주자.
                startActivity(intent);
            }
        });
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

            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation)); //현재위치로 카메라 이동.
            btnRoad.setVisibility(View.VISIBLE);



            CircleOptions circleOptions = new CircleOptions()
                    .center(currentLocation)
                    .radius(500)
                    .fillColor(Color.parseColor("#2271cce7"))
                    .strokeWidth(2)
                    .strokeColor(Color.BLUE);

            Circle circle = mMap.addCircle(circleOptions);


            return;
        }

        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION); //현재 위치 가져오지 못했으니 기본 위치로 지정
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); //현재 위경도 가져오지 못한다면 빨간색으로 표시
        currentMarker = this.mMap.addMarker(markerOptions);
        this.mMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION)); //기본 위치로 카메라 이동
        */

        Toast.makeText(getApplicationContext(), "위치 정보 확인 중...", Toast.LENGTH_LONG).show();

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

/*---------Intent로 위치 데이터 받는 부분.--------------------------------------------------------*/


        for (UserBoxInfo data : boxList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드
            //LatLng latlng = new LatLng(Double.parseDouble(data.getMyLatitude()), Double.parseDouble(data.getMyLogitude()));
            LatLng latlng = new LatLng(Double.valueOf(data.getMyLatitude()), Double.valueOf(data.getMyLongitude()));
            mMap.addMarker(new MarkerOptions().position(latlng).title(data.getBoxName())
                    .snippet(data.getBoxPrice())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.boxicon))

            ).setTag(data.getKeyValue());

        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        }

        // 다이얼로그 생성

        /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Intent intent = new Intent(getBaseContext(), PickupDetail.class);
                //String name = marker.getTitle();
                //Log.d("tag : ",marker.getTag().toString());
                String keyTag = marker.getTag().toString();
                for (UserBoxInfo boxInfo : boxList){ //DB에 있는 정보 중 내가 선택한 마커와 동일 위치의 정보
                    if (keyTag.trim().contentEquals(boxInfo.getKeyValue())){

                        intent.putExtra("boxName", boxInfo.getBoxName());
                        intent.putExtra("boxSize", boxInfo.getBoxSize());
                        intent.putExtra("boxPrice", boxInfo.getBoxPrice());
                        intent.putExtra("myLatitude", boxInfo.getMyLatitude());
                        intent.putExtra("myLongitude", boxInfo.getMyLongitude());
                        intent.putExtra("pickupTime", boxInfo.getPickupTime());
                        intent.putExtra("duration", boxInfo.getDuration());
                        intent.putExtra("distance", boxInfo.getDistance());
                        intent.putExtra("userName", boxInfo.getUserName());

                        intent.putExtra("FLAG",MAP_PAGE_FLAG);

                        Log.d("키값",keyTag);
                        startActivity(intent);                      //***지도에 잡은 위치 정보를 보여주려 intent넘기는 부분

                    }
                }

                return false;
            }
        });*/

//------------지도의 마커 클릭시 넘어가는 부분--------------------------------------------------------------
       mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Intent intent = new Intent(getBaseContext(), PickupDetail.class);
                //String name = marker.getTitle();
                Log.d("tag : ",marker.getTag().toString());
                String keyTag = marker.getTag().toString();
                for (UserBoxInfo boxInfo : boxList){ //DB에 있는 정보 중 내가 선택한 마커와 동일 위치의 정보
                    if (keyTag.contentEquals(boxInfo.getKeyValue())){

                        intent.putExtra("boxName", boxInfo.getBoxName());
                        intent.putExtra("boxSize", boxInfo.getBoxSize());
                        intent.putExtra("boxPrice", boxInfo.getBoxPrice());
                        intent.putExtra("myLatitude", boxInfo.getMyLatitude());
                        intent.putExtra("myLongitude", boxInfo.getMyLongitude());
                        intent.putExtra("pickupTime", boxInfo.getPickupTime());
                        intent.putExtra("duration", boxInfo.getDuration());
                        intent.putExtra("distance", boxInfo.getDistance());
                        intent.putExtra("userName", boxInfo.getUserName());
                        intent.putExtra("keyValue", boxInfo.getKeyValue());

                        Log.d("키_맵 테스트",boxInfo.getBoxName());
                        Log.d("키_맵 테스트",boxInfo.getUserName());
                        Log.d("키_맵 테스트",boxInfo.getBoxSize());
                        Log.d("키_맵 테스트",boxInfo.getBoxPrice());

                        intent.putExtra("FLAG",MAP_PAGE_FLAG_1);

                        Log.d("키값",keyTag);

                    }
                }

                startActivity(intent);                      //***지도에 잡은 위치 정보를 보여주려 intent넘기는 부분
              //  Toast.makeText(getApplicationContext(),"상세 화면으로 이동.",Toast.LENGTH_SHORT).show();

            }
        });

       /* mItemClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mItemClusterManager);
        mMap.setOnMarkerClickListener(mItemClusterManager);

        mItemClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                Toast.makeText(BoxLocationMap.this, myItem.getPosition().toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/

        //addItems(); //클러스터링...기능 일단 중단

    //3km이내 데이터 보기------------------------------------------------------------------
        btnRoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Location nowLatLong = new Location("my_point");
                nowLatLong.setLatitude(currentPossition.latitude);
                nowLatLong.setLongitude(currentPossition.longitude);

                disList = new ArrayList<>();
                disList.clear();
                float Distacne=0;
                for (UserBoxInfo data : boxList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드
                    /*LatLng latlng = new LatLng(Double.valueOf(data.getMyLatitude()), Double.valueOf(data.getMyLongitude()));*/
                    Location destLatLong = new Location("dest_point");
                    destLatLong.setLatitude(Double.valueOf(data.getMyLatitude()));
                    destLatLong.setLongitude(Double.valueOf(data.getMyLongitude()));

                    Distacne = (nowLatLong.distanceTo(destLatLong)/1000);

                    //tv.append("거리 : "+Distacne + "\n");
                    if (Distacne < 3){
                        CurrentLocation cr = new CurrentLocation();
                        cr.setDistance(Distacne);
                        cr.setChk_latitude(data.getMyLatitude());
                        cr.setChk_longitude(data.getMyLongitude());

                        disList.add(cr);
                    }
                }

                currentMarker.remove();
                mMap.clear();

                //추가해둔 리스트 불러와서 다시 지도 뿌려주기
                for (int i=0; i<disList.size();i++){
                    //tv.append(disList.get(i).getChk_latitude()+" // "+disList.get(i).getChk_longitude()+"\n");
                    LatLng caslatlng = new LatLng(Double.valueOf(disList.get(i).getChk_latitude()),Double.valueOf(disList.get(i).getChk_longitude()));

                    mMap.addMarker(new MarkerOptions()
                            .position(caslatlng)
                            .title(disList.get(i).getDistance()+"km거리에 있는")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));



                }

            }
        });


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
            this.mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

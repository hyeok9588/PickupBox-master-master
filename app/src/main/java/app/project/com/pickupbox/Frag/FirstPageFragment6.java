package app.project.com.pickupbox.Frag;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import app.project.com.pickupbox.Adapter.LocationListAdapter;
import app.project.com.pickupbox.Adapter.SwipeRecyclerViewAdapter;
import app.project.com.pickupbox.Data.DataParser;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.Register_Box.AddResult;


public class FirstPageFragment6 extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    FirstPageFragment3 fragment3;

    private static final String LOCATION_KEY = "LOCATION_KEY";
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);  //기본 지도 표시 위도 경도
    private static final String TAG = "google map example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 300000; //내 위치를 받아오는 시간 간격 5분(30만초)로 설정
    private static final int FASTEST_UPDATE_INTERVAL_MS = 300000;

    int ccnt = 0; // 길찾기 라인 색 다르게 체크
    ArrayList<LatLng> MarkerPoints; // 마커 저장

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager; //리사이클러뷰 사용을 위한 설정

    private GoogleMap mMap = null;
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private Marker currentMarker = null;
    private Button btnOkay;

    private TextView tv; // 아래 텍스트 출력 부분 컨트롤

    private LatLng currentPossition;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    /*private List<LocationExample> locationList;*/
    private  ArrayList<LocationExample> list;

    private String Dis, Dur, newDur; //소요시간 소요거리
    private Double DestLat,DestLong;

    private PolylineOptions lineOptions;
    private ArrayList<LatLng> polypoints;
    private Polyline polyline;
    private LatLng secondLain;
    private ArrayList<LocationExample> dataList;
    private String nick,userName;

    private  ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.firstpage_fragment6, container, false);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("실행 순서", "activity 생성");
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("\t지도를 준비중입니다!!");
        //show dialog
        progressDialog.show();

        mapView = (MapView)v.findViewById(R.id.mapV2);
        fragment3 = new FirstPageFragment3();

        /*action list에 넣기위한------------------*/
        final RecyclerView rcView = v.findViewById(R.id.rcView);
        rcView.setHasFixedSize(true); //리사이클러 뷰 기존성능 강황
        layoutManager = new LinearLayoutManager(getContext());
        rcView.setLayoutManager(layoutManager);

        dataList = new ArrayList<>(); //LocationExample 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)
        /*---------------------------------------------------------------*/

        /*DB 비동기 연결-----------------------*/
        DBConnTask dbConnTask = new DBConnTask();
        dbConnTask.execute();
        /*------------------------------------*/

        mapView.onCreate(savedInstanceState); //map 시작 부분
        Log.d("실행 순서", "map create");

        /*action list에 넣기위한------------------------------------------------*/
        adapter = new LocationListAdapter(getContext(), dataList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        rcView.setAdapter(adapter);
        Log.d("실행 순서", "adpater연결");
        /*------------------------------------------------------------------*/

        MarkerPoints = new ArrayList<>(); // 마커 저장 시켜놓을 리스트
        MarkerPoints.clear();

        mapView.getMapAsync(this);


        tv = (TextView)v.findViewById(R.id.DDtext2);
        btnOkay = (Button)v.findViewById(R.id.btnOkay);
        btnOkay.setVisibility(View.INVISIBLE);

        //AdResult로 데이터 넘기기
        final Intent intentInfo = new Intent(getContext(), AddResult.class);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_layout,fragment3).commitAllowingStateLoss();

                String myLatitude = Double.toString(currentMarker.getPosition().latitude);
                String myLongitude = Double.toString(currentMarker.getPosition().longitude);

                //번들객체 생성 값 전달 위해
                Bundle bundle = new Bundle();
                bundle.putString("myLatitude",myLatitude);
                bundle.putString("myLongitude",myLongitude);
                bundle.putString("ssDestLatitude",DestLat.toString());
                bundle.putString("ssDestLongitude",DestLong.toString());
                bundle.putString("ssDuration",newDur);
                bundle.putString("ssDistance",Dis);

                fragment3.setArguments(bundle);

            }
        });

        return v;
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Bundle에서 LOCATION_KEY를 찾았다면,
                // mCurrentLocation이 null이 아니라고 확신할 수 있다.
                currentPossition = savedInstanceState.getParcelable(LOCATION_KEY);
                Log.d("인스턴스 변수",savedInstanceState.toString());
            }

        }
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
            return;
        }


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION); //현재 위치 가져오지 못했으니 기본 위치로 지정
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); //현재 위경도 가져오지 못한다면 빨간색으로 표시
        currentMarker = this.mMap.addMarker(markerOptions);

        this.mMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION)); //기본 위치로 카메라 이동
        /*Toast.makeText(getApplicationContext(), "위치 정보 확인 중...", Toast.LENGTH_LONG).show();*/
        Log.d("실행 순서", "현재 위치 표시");

    }



    @Override
    public void onStart() {
        super.onStart();
        if(googleApiClient != null) {
            Log.d(TAG, "onStart : mGoogleApiClient connect");
            googleApiClient.connect();
        }

        mapView.onStart();

    }

    @Override
    public void onStop() {
        Log.d("실행 타임", "on stop");
        super.onStop();
        if (googleApiClient!=null && googleApiClient.isConnected())
            mapView.onStop();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();

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
        Log.d("실행 타임", "on pause");
        googleApiClient.stopAutoManage(getActivity());
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
        Log.d("실행 타임", "on Destroy");
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
        Log.d("실행 순서", "onMapReady 시작");

        //런타임 퍼미션 요청 대화상자나 GPS활성 요청 대화 상자 보이기전에 초기위로 이동.
        setCurrentLocation(null,"위치정보 가져올 수 없음.","위치 퍼미션과 GPS활성 여부 확인");

        //나침반 사용
        mMap.getUiSettings().setCompassEnabled(true);
        //매끄럽게 이동
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        Log.d("실행 순서", "map permission check");
        //API 23이상일 경우 런타임 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //사용권한 체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED){
                //사용권한 없다면
                //권한 재요청
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }else {
                //사용권한 있다면
                if (googleApiClient == null){
                    buildGoogleApiClient();  //클래스 호출
                }

                if (ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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
        /*LatLng sihwa = new LatLng(37.342554, 126.735857); //sihwa에 대한 위치  정보.
        mMap.addMarker(new MarkerOptions().position(sihwa).title("시화산업단지")); //시화에 대한 마커를 넣겠다.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sihwa)); //카메라를 시화로 움직이겠다.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //카메라를 줌인 하겠다.
*/
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        //list = getActivity().getIntent().getParcelableArrayListExtra("LocalList2");

        //Asynctask
        MyAsyncTask mAsyncTask = new MyAsyncTask();
        mAsyncTask.execute(dataList);
        Log.d("실행 순서", "map 위에 db 마커 출력");



        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        }

        // 다이얼로그 생성



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                LatLng latlng = currentMarker.getPosition();
                MarkerPoints.add(latlng);

                if (MarkerPoints.size()==1){
                    secondLain = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    MarkerPoints.add(secondLain);
                    DestLat = secondLain.latitude;
                    DestLong = secondLain.longitude;
                    Toast.makeText(getContext(),"도착 위치로 추가",Toast.LENGTH_SHORT).show();
                    btnOkay.setVisibility(View.VISIBLE);


                    // 찍혀있는 마커 차례대로 두개씩 대중교통 길찾기 실행 (ex, 0->1,  1->2)
                    for (int i = 0; i < MarkerPoints.size() - 1; i++) {
                        /*tv.setText(" 대중 교통 길찾기 실행"+ "\n");*/
                        String url = getUrl(MarkerPoints.get(i), MarkerPoints.get(i + 1)); // 마커 위치정보 넘겨줘서 맞는 url형식 만듬
                        FirstPageFragment6.fetchUrl fUrl = new FirstPageFragment6.fetchUrl(); // fetch할 클래스 생성
                        fUrl.execute(url); // url fetch
                    }


                } else if (MarkerPoints.size()>1){
                    btnOkay.setVisibility(View.INVISIBLE); //도착위치로 추가 버튼 비활성화
                    MarkerPoints.clear();
                    tv.setText("");

                    polypoints.clear();

                    MarkerPoints.add(latlng); //현재위치 추가
                    secondLain = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude); //다시 찍은 위치를 넣어두고
                    MarkerPoints.add(secondLain); //다시찍은 위치를 MarkerPoint에 넣고

                    DestLat = secondLain.latitude; //addResult에 넘어갈 데이터를 위해 저장.
                    DestLong = secondLain.longitude;

                    Log.d("마커 횟수", Integer.toString(MarkerPoints.size()));

                    for (int i = 0; i < MarkerPoints.size() - 1; i++) {
                        /*tv.setText(" 대중 교통 길찾기 실행"+ "\n");*/
                        String url = getUrl(MarkerPoints.get(i), MarkerPoints.get(i + 1)); // 마커 위치정보 넘겨줘서 맞는 url형식 만듬
                        FirstPageFragment6.fetchUrl fUrl = new FirstPageFragment6.fetchUrl(); // fetch할 클래스 생성
                        fUrl.execute(url); // url fetch
                    }

                    Toast.makeText(getContext(),"도착 위치가 변경 설정되었습니다.",Toast.LENGTH_SHORT).show();

                    btnOkay.setVisibility(View.VISIBLE);//도착위치로 추가 버튼 활성화
                }


                return false;
            }
        });

    }

    //AsyncTask<doInBackground, onPreexecute, onPostexecute>
    public class DBConnTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                /*여기서부터-------------------------------------------------------------------------------------------*/
                database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
                databaseReference = database.getReference("location");  //db테이블 연결
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//파이어베이스 데이터 받아오는 곳

                        dataList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                            LocationExample location = snapshot.getValue(LocationExample.class); //만들어뒀던 article 객체에 데이터 담기
                            dataList.add(location);

                        }
                        adapter.notifyDataSetChanged();
                        Log.d("실행 순서", "db list 다 가져옴");
                        Log.d("실행 순서", dataList.get(0).getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //디비를 가져오던 중 에러 발생시
                        Log.e("FPF6", String.valueOf(databaseError.toException())); //에러문 출력
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("DB불러오는 중..");
            //show dialog
            Log.d("실행 순서","onPreExecute DB");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Log.d("실행 순서","onPostExecute DB");


        }
    }
    //AsyncTask<doInBackground, onPreexecute, onPostexecute>
    public class MyAsyncTask extends AsyncTask<ArrayList<LocationExample>, Void, Void> {

        ProgressDialog progressDialog = new ProgressDialog(getContext());

        @Override
        protected Void doInBackground(ArrayList<LocationExample>... arrayLists) {
            try {
                for (LocationExample data : dataList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드
                    LatLng latlng = new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude()));

                    mMap.addMarker(new MarkerOptions().position(latlng).title(data.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                }
                Log.d("실행 순서","doInBackground--MyAsyncTask");

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("\t지도를 띄우는 중입니다!");
            //show dialog
            Log.d("실행 순서","onPreExecute--MyAsyncTask");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Log.d("실행 순서","onPostExecute-MyAsyncTask");


        }
    }


    private void buildGoogleApiClient(){
        Log.d("실행 순서", "buildGoogleApiClient 시작");
        if (googleApiClient!=null) googleApiClient.disconnect();

            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .enableAutoManage(getActivity(),this)
                    .build();

            googleApiClient.connect();
    }

    public boolean checkLocationServicesStatus(){
        Log.d("실행 순서", "checkLocationServicesStatus 시작");
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("실행 순서", "onConnected 시작");
        if (!checkLocationServicesStatus()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
            if (ActivityCompat.checkSelfPermission(getContext(),
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
        Log.d("실행 순서", "onlocationChange 시작");
        Log.i(TAG, "onLocationChanged call..");
        currentPossition = new LatLng(location.getLatitude(), location.getLongitude());
        String markerTitle = getCurrentAddress(currentPossition);
        String markerSnippet = "위도 : " + String.valueOf(location.getLatitude()) + " 경도 : " + String.valueOf(location.getLongitude());
        setCurrentLocation(location,markerTitle,markerSnippet);

        progressDialog.dismiss();
    }


    public String getCurrentAddress(LatLng latlng){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getContext(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getContext(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getContext(), "주소 미발견", Toast.LENGTH_LONG).show();
            Toast.makeText(getContext(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


// https://maps.googleapis.com/maps/api/directions/outputFormat?parameters
    //https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood4&key=YOUR_API_KEY
    // origin=41.43206,-81.38992


    // url 보내고 받아서 파싱 하는 부분 시작
    private String getUrl(LatLng origin, LatLng dest) // 위치 두개 받아서 길찾기 URL 형식으로 바꿈  // 키 필요   Google Direction APi 이용
    {
        String url = "";
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        if()
        Log.d("로그 시간 :",Long.toString(System.currentTimeMillis()));
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat a = new SimpleDateFormat("hh a, zzzz");
        Log.d("로그 날짜 :",a.format(date));

        //derection
        url = "https://maps.googleapis.com/maps/api/directions/json?" +  str_origin +"&"+str_dest +"&mode=transit"+"&alternatives=true"+  "&key=AIzaSyCA-UoD4WRsPs_ilJkhgcB3OQVSFZ0wXnQ";

        Log.d("로그 유알엘 :",url);
        return url;
    }


    // 길찾기 할때 패치함
    private class fetchUrl extends AsyncTask<String, Void, String> // AsyncTsk는 일종의 쓰레드 doInBackground 에서 PostExecute로 return값 넘겨줄수 있고, Post Execute는 ui컨트롤 부분 가능 Google Direction APi 이용
    {
        protected String doInBackground(String... url)
        {
            String data="";
            try {
                data = downloadUrl(url[0]); // URL 보내서 정보 받기
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;

        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            FirstPageFragment6.ParserTask parserTask = new FirstPageFragment6.ParserTask();
            parserTask.execute(result);

        }
    }// fetchUrl


    private String downloadUrl(String strUrl) throws IOException // 만든 URL 보내서 관련 정보 받아오기
    {
        String data = "";
        InputStream iStream = null;
        HttpsURLConnection urlConnection = null;
        Log.d("Url",strUrl);
        try{
            URL url = new URL(strUrl);

            // url 만들기
            urlConnection = (HttpsURLConnection) url.openConnection();

            // 연결
            urlConnection.connect();

            // 데이터 읽기
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null) // 다 읽을 때 까지 버퍼에 계속 넣기
            {
                sb.append(line);
            }

            data = sb.toString(); // 버퍼에 쌓인 내용 저장
            Log.d("downloadUrl", data.toString());
            br.close();

        }
        catch (Exception e)
        {
            Log.d("Urlfail", "urldownloadfail");
        }
        finally
        {
            Log.d("Urlend", "end");
            iStream.close();;
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
            // 맵에 길찾기 한 루트를 Polyline을 이용해 그려주고 소요시간, 거리 가져오는 함수 DataParser클래스를 이용해 JSON파싱한 내용을 이용한다. Google Direction API 이용
    {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            //루트 관련 정보 저장
            JSONObject jObject_route;
            List<List<HashMap<String,String >>> routes = null;
            //  List<List<String>> DD;

            try {
                jObject_route = new JSONObject(jsonData[0]);


                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject_route);

                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }// doinback

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                polypoints = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);


                Log.d("d_parsing", "path size: " + Integer.toString(path.size()));
                // Fetching all the points in i-th route

                for (int j = 0; j < path.size(); j++) { // 패스 수 많금 포문
                    HashMap<String, String> point = path.get(j);

                    if (point.containsKey("Distance") || point.containsKey("Duration")) { // 거리나 소요시간 키를 가지고 있으면
                        Dis = point.get("Distance"); // 그 거리 정보 가져온다.
                        Dur = point.get("Duration"); // 그 소요시간 정보 가져온다.

                        String hourRep, minRep;
                        if (Dur.contains("hour")) {
                            //Log.d("시간 :", "일단 시간으로 분류");
                            newDur = Dur.replace("hours","시간");
                            Log.d("시간",newDur);
                            newDur = newDur.trim();

                            if (Dur.contains("mins")) {
                                //Log.d("시간 :", "시간+분으로 분류");
                                newDur = Dur.replace("mins","분");
                                newDur = newDur.trim();

                                Log.d("시간",newDur);

                            }

                        }else if(Dur.contains("mins")){
                            Log.d("시간 :", "분으로만 분류");
                            newDur = Dur.replace("mins","분");
                            newDur = newDur.trim();

                            Log.d("시간",newDur);


                        }
                        tv.append("목적지 까지의 거리 : "+ Dis + "\n 예상 소요 시간 : " + Dur + "\n"); // 텍스트 뷰에 그 정보들 뿌려준다.



                    } else {

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        polypoints.add(position);
                    }
                } // for
                // Adding all the points in the route to LineOptions
//                    lineOptions.addAll(points);
                // lineOptions.addAll(polypoints);
                //lineOptions.width(20);


                if(ccnt==0) {
                    //lineOptions.color(Color.rgb(33, 142, 233)); //8EC7fF
                    ccnt++;
                }
                else { //이후 목적지를 변경 하였다면

                    //lineOptions.color(Color.rgb(133, 142, 233));    //8EC7fF
                    ccnt=0;
                }
                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }
            // Drawing polyline in the Google Map for the i-th route                                     //폴리 라인 그려주기
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
                polyline = mMap.addPolyline(lineOptions);
            }
            else {


                Log.d("onPostExecute","without Polylines drawn");
            }
        }

    }// ParserTask
// url 보내고 받아서 파싱 하는 부분 끝

}

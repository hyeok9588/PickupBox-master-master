package app.project.com.pickupbox.Frag;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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

import app.project.com.pickupbox.Data.CurrentLocation;
import app.project.com.pickupbox.Data.DataParser;
import app.project.com.pickupbox.Data.MyItem;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.Main_Page.BoxLocationMap;
import app.project.com.pickupbox.Main_Page.PickupDetail;
import app.project.com.pickupbox.R;


public class FirstPageFragment1_map extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /*public static Context mContext;//메인에서 가져다 쓰기위함*/
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);  //기본 지도 표시 위도 경도
    private static final String TAG = "FirstPageFragment_MAP";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 300000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 300000; //지도 갱신 시간 30만초 = 5분

    private ClusterManager<MyItem> mItemClusterManager;

    int ccnt = 0; // 길찾기 라인 색 다르게 체크
    ArrayList<LatLng> MarkerPoints; // 마커 저장

    private GoogleMap mMap = null; //구글 맵 쓰기 위한 설정
    private MapView mapView = null; //구글 맵을 보여주기 위한 뷰 설정
    private GoogleApiClient googleApiClient = null; //google api 를 쓰기 위한 googleApiclient설정
    private Marker currentMarker = null; //현재 위치 표시를 위한 마커

    private Button btnRoad; //polyline으로 그려진 길 보기
    private Button btnGoBack; //이전 화면 버튼
    private TextView tv; // 아래 텍스트 출력 부분 컨트롤

    private LatLng currentPossition; //현재 위치 저장

    private FirebaseDatabase database; //Firebase db 사용 설정
    private DatabaseReference databaseReference; //db 사용 설정 2

    private ArrayList<UserBoxInfo> boxList; //사용자 요청 위치들을 저장할 list
    private ArrayList<CurrentLocation> disList; //3km 반경을 찍기 위해 현재위치를 저장하는 list


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.first_page_fragment1_map, container, false);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //화면 꺼지지 않기 위한 설정

        btnRoad = (Button)v.findViewById(R.id.btnChk);
        btnGoBack = (Button)v.findViewById(R.id.btnGoBack);
        tv = (TextView)v.findViewById(R.id.DDtext);
        btnRoad.setVisibility(View.INVISIBLE);

        boxList = getActivity().getIntent().getParcelableArrayListExtra("boxList"); //앞의 activity에서 넘어온 boxlist 를 받아주는 곳.

        MarkerPoints = new ArrayList<>(); // 마커 저장 시켜놓을 리스트
        MarkerPoints.clear(); //list 초기화 하고

        mapView = (MapView)v.findViewById(R.id.mapV); //맵 뷰 설정
        mapView.onCreate(savedInstanceState); //전역 변수 값 유지를 위한 savedInstanceState
        mapView.getMapAsync(this); //과거에 getMap()으로 googleMap 객체를 얻어왔지만 지금은 이런식으로 얻어온다.


        btnGoBack.setOnClickListener(new View.OnClickListener() { //버튼 누를시 액션 가지고 처음으로 돌아가서 로그인 하는 부분...
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FirstPage.class);
                intent.setAction("ACTION_LOGIN_BACK");
                startActivity(intent);
            }
        });

        return v;
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet){
        if(currentMarker != null) currentMarker.remove(); //현재 위치가 표시되어 있다면 지우고 시작.

        if (location!=null){
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());   //현재위치 위경도 가져오기

            MarkerOptions markerOptions = new MarkerOptions(); //maker option 사용 설정
            markerOptions.position(currentLocation); //현재 위치로 표시
            markerOptions.title(markerTitle); //이름을 markerTitle로 표시
            markerOptions.snippet(markerSnippet); //클릭시 보이는 이름
            markerOptions.draggable(true); //드레그 허용
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));   //현재 위경도 가져왔다면 파란색으로 표시
            currentMarker = this.mMap.addMarker(markerOptions); //currentMarker에 markerOptions저장
            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation)); //현재위치로 카메라 이동.

            btnRoad.setVisibility(View.VISIBLE);
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
        Toast.makeText(getContext(), "위치 정보 확인 중...", Toast.LENGTH_LONG).show();


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
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED){
                //사용권한 없다면
                //권한 재요청
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

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
        LatLng sihwa = new LatLng(37.342554, 126.735857); //sihwa에 대한 위치  정보.
        mMap.addMarker(new MarkerOptions().position(sihwa).title("시화산업단지")); //시화에 대한 마커를 넣겠다.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sihwa)); //카메라를 시화로 움직이겠다.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //카메라를 줌인 하겠다.

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        /*---------Intent로 위치 데이터 받는 부분.--------------------------------------------------------*/
        for (UserBoxInfo data : boxList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드
            LatLng latlng = new LatLng(Double.valueOf(data.getMyLatitude()), Double.valueOf(data.getMyLongitude()));
            mMap.addMarker(new MarkerOptions().position(latlng).title(data.getBoxName())
                    .snippet(data.getBoxPrice())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            ).setTag(data.getKeyValue());
        }

        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        }



//------------지도의 마커 클릭시 넘어가는 부분--------------------------------------------------------------
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Intent intent = new Intent(getContext(), PickupDetail.class);
                String name = marker.getTitle();

                Log.d("tag : ",marker.getTag().toString());
                String keyTag = marker.getTag().toString();
                for (UserBoxInfo boxInfo : boxList){ //DB에 있는 정보 중 내가 선택한 마커와 동일 위치의 정보
                    if (keyTag==boxInfo.getKeyValue()){

                        intent.putExtra("boxName", boxInfo.getBoxName());
                        intent.putExtra("boxSize", boxInfo.getBoxSize());
                        intent.putExtra("boxPrice", boxInfo.getBoxPrice());
                        intent.putExtra("myLatitude", boxInfo.getMyLatitude());
                        intent.putExtra("myLongitude", boxInfo.getMyLongitude());
                        intent.putExtra("pickupTime", boxInfo.getPickupTime());
                        intent.putExtra("duration", boxInfo.getDuration());
                        intent.putExtra("distance", boxInfo.getDistance());
                        intent.putExtra("userName", boxInfo.getUserName());

                    }
                }
                startActivity(intent);                      //***지도에 잡은 위치 정보를 보여주려 intent넘기는 부분
                //  Toast.makeText(getApplicationContext(),"상세 화면으로 이동.",Toast.LENGTH_SHORT).show();

            }
        });

        mItemClusterManager = new ClusterManager<>(getContext(), mMap);
        mMap.setOnCameraIdleListener(mItemClusterManager);
        mMap.setOnMarkerClickListener(mItemClusterManager);

        mItemClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                Toast.makeText(getContext(), myItem.getPosition().toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        addItems();

    //3km이내 데이터 보기------------------------------------------------------------------
        btnRoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Location nowLatLong = new Location("my_point");
                nowLatLong.setLatitude(currentPossition.latitude);
                nowLatLong.setLongitude(currentPossition.longitude);

                disList = new ArrayList<CurrentLocation>();
                disList.clear();
                float Distacne=0;
                for (UserBoxInfo data : boxList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드

                    Location destLatLong = new Location("dest_point");
                    destLatLong.setLatitude(Double.valueOf(data.getMyLatitude()));
                    destLatLong.setLongitude(Double.valueOf(data.getMyLongitude()));

                    Distacne = (nowLatLong.distanceTo(destLatLong)/1000);

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

                for (int i=0; i<disList.size();i++){
                    LatLng caslatlng = new LatLng(Double.valueOf(disList.get(i).getChk_latitude()),Double.valueOf(disList.get(i).getChk_longitude()));
                    mMap.addMarker(new MarkerOptions().position(caslatlng).title(disList.get(i).getDistance()+"km 택배")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                }

            }
        });


    }

    private void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(getActivity(),this)
                .build();
        googleApiClient.connect();
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void addItems(){

        for (UserBoxInfo data : boxList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드
            //LatLng latlng = new LatLng(Double.parseDouble(data.getMyLatitude()), Double.parseDouble(data.getMyLogitude()));
            LatLng latlng = new LatLng(Double.valueOf(data.getMyLatitude()), Double.valueOf(data.getMyLongitude()));
            mMap.addMarker(new MarkerOptions().position(latlng).title(data.getBoxName())
                    .snippet(data.getBoxPrice())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            ).setTag(data.getKeyValue());

            MyItem offsetItem = new MyItem(Double.valueOf(data.getMyLatitude()), Double.valueOf(data.getMyLongitude()));
            mItemClusterManager.addItem(offsetItem);

        }

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
        Log.i(TAG, "onLocationChanged call..");

        currentPossition = new LatLng(location.getLatitude(), location.getLongitude());
        String markerTitle = getCurrentAddress(currentPossition);
        String markerSnippet = "위도 : " + String.valueOf(location.getLatitude()) + " 경도 : " + String.valueOf(location.getLongitude());
        setCurrentLocation(location,markerTitle,markerSnippet);

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
        Log.d("l_d",Long.toString(System.currentTimeMillis()));
//        long now = System.currentTimeMillis();
//        Date date = new Date(now);
//        SimpleDateFormat a = new SimpleDateFormat("hh a, zzzz");
//        Log.d("l_d",a.format(date));

        //derection
        url = "https://maps.googleapis.com/maps/api/directions/json?" +  str_origin +"&"+str_dest +"&mode=transit"+"&alternatives=true"+  "&key=AIzaSyCA-UoD4WRsPs_ilJkhgcB3OQVSFZ0wXnQ";
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
//            Intent MyIntent = new Intent(getApplicationContext(),Urltextview.class);
//
//            MyIntent.putExtra("url",result+ "\n\n\n************\n\n\n");
//            startActivity(MyIntent);


            FirstPageFragment1_map.ParserTask parserTask = new FirstPageFragment1_map.ParserTask();
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
//            BufferedReader br = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));

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

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> // 맵에 길찾기 한 루트를 Polyline을 이용해 그려주고 소요시간, 거리 가져오는 함수 DataParser클래스를 이용해 JSON파싱한 내용을 이용한다. Google Direction APi 이용
    {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
//루트 관련 정보 저장
            JSONObject jObject_route;
            List<List<HashMap<String,String >>> routes = null;
//            List<List<String>> DD;

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
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);


                Log.d("d_parsing", "path size: " + Integer.toString(path.size()));
                // Fetching all the points in i-th route

                for (int j = 0; j < path.size(); j++) { // 패스 수 많금 포문
                    HashMap<String, String> point = path.get(j);
                    if (point.containsKey("Distance") || point.containsKey("Duration")) { // 거리나 소요시간 키를 가지고 있으면
                        String Dis = point.get("Distance"); // 그 거리 정보 가져온다.
                        String Dur = point.get("Duration"); // 그 소요시간 정보 가져온다.
                        tv.append("목적지 까지의 거리 : "+ Dis + "\n 예상 소요 시간 : " + Dur + "\n"); // 텍스트 뷰에 그 정보들 뿌려준다.

                    } else {

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                } // for
                // Adding all the points in the route to LineOptions
//                    lineOptions.addAll(points);
                lineOptions.addAll(points);
                lineOptions.width(20);
                if(ccnt==0) {
                    lineOptions.color(Color.rgb(33, 142, 233)); //8EC7fF
                    ccnt++;
                }
                else {
                    lineOptions.color(Color.rgb(133, 142, 233));    //8EC7fF
                    ccnt=0;
                }
                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }

    }// ParserTask
// url 보내고 받아서 파싱 하는 부분 끝

}

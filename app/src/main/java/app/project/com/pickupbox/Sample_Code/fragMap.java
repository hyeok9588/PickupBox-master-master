package app.project.com.pickupbox.Sample_Code;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import app.project.com.pickupbox.Data.DataParser;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.R;

import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class fragMap extends Fragment implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);  //기본 지도 표시 위도 경도
    private static final String TAG = "google map example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    int ccnt = 0; // 길찾기 라인 색 다르게 체크
    ArrayList<LatLng> MarkerPoints; // 마커 저장

    private GoogleMap mMap = null;
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private Marker currentMarker = null;

    private Button btnRoad;
    private TextView tv; // 아래 텍스트 출력 부분 컨트롤

    MainActivity activity;

    private LatLng currentPossition;

    public fragMap ()
    {

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
        Toast.makeText(getActivity(), "위치 정보 확인 중...", Toast.LENGTH_LONG).show();




    }


    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    Location location;



    public static fragMap newInstance(List<LocationExample> locationList) {
        fragMap fragment = new fragMap();
        Bundle args = new Bundle();

        args.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) locationList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map,  null);
        mapView = (MapView)view.findViewById(R.id.map);
        mapView.getMapAsync(this);



        btnRoad = (Button)view.findViewById(R.id.btnChk);
        tv = (TextView)view.findViewById(R.id.DDtext);

        MarkerPoints = new ArrayList<>(); // 마커 저장 시켜놓을 리스트
        MarkerPoints.clear();



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        if (googleApiClient!=null && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (googleApiClient!=null)
            googleApiClient.connect();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (googleApiClient!=null && googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        googleApiClient.disconnect();


       googleApiClient.stopAutoManage(getActivity()); //fragment간에 왔다갔다 하더라도 꺼지지 않기 위함.

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될때 실행되는 함수
        MapsInitializer.initialize(getActivity().getApplicationContext());

        if(mapView != null)
        {
            mapView.onCreate(savedInstanceState);
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
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
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

                if (ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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

        Bundle bundle = getArguments(); //bundle로 인자 쓰겠다고 설정 해주고 (frag1에서 넘어온 인자를 쓰기 위함)
        List<LocationExample> locationList = bundle.getParcelableArrayList("list"); //Frag1에서 받아온 데이터를 list에 저장했었으니 꺼내주고.

        //데이터 넘어오는거 확인용
        /*String ex = locationList.get(0).getName();
        Toast.makeText(getActivity(), ex, Toast.LENGTH_LONG).show();*/

        for (LocationExample data : locationList){ //데이터에서 하나씩 위경도 뺴와서 latlng에 넣고 하나씩 위치 지정하는 코드
            LatLng latlng = new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(latlng).title(data.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            /*.icon(BitmapDescriptorFactory.fromResource(R.mipmap.boximg)));*/
        }

        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        }


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (MarkerPoints.size()==0){
                    LatLng laln = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    MarkerPoints.add(laln);
                    Toast.makeText(getActivity(),"출발 위치로 추가",Toast.LENGTH_SHORT).show();
                }
                else if (MarkerPoints.size()==1){
                    LatLng laln = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    MarkerPoints.add(laln);
                    Toast.makeText(getActivity(),"도착 위치로 추가",Toast.LENGTH_SHORT).show();


                }
                else if (MarkerPoints.size()>1){
                    Toast.makeText(getActivity(),"출발, 도착 위치가 모두 설정 되었음.",Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });


        //폴리라인 클릭 리스너
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {

            }
        });

        btnRoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 찍혀있는 마커 차례대로 두개씩 대중교통 길찾기 실행 (ex, 0->1,  1->2)
                for (int i = 0; i < MarkerPoints.size() - 1; i++) {
                    tv.setText(" 대중 교통 길찾기 실행"+ "\n");
                    String url = getUrl(MarkerPoints.get(i), MarkerPoints.get(i + 1)); // 마커 위치정보 넘겨줘서 맞는 url형식 만듬
                    fetchUrl fUrl = new fetchUrl(); // fetch할 클래스 생성
                    fUrl.execute(url); // url fetch
                }
            }
        });


    }

    private void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(getActivity())
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkLocationServicesStatus()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            if (ActivityCompat.checkSelfPermission(getActivity(),
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
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getActivity(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "주소 미발견", Toast.LENGTH_LONG).show();
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


            ParserTask parserTask = new ParserTask();
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
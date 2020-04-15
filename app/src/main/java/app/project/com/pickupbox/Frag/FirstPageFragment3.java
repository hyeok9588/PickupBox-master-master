package app.project.com.pickupbox.Frag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.FragDialog_AddList;
import app.project.com.pickupbox.FragDialog_AddMap;
import app.project.com.pickupbox.Polyline_DisDur;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.Register_Box.AddBox;
import app.project.com.pickupbox.Register_Box.AddResult;
import app.project.com.pickupbox.Register_Box.DialogMap;

public class FirstPageFragment3 extends Fragment {
    ViewGroup viewGroup;

    EditText etBoxName, etPrice;
    Button btnAddBox,btnAddBox2, btnFindDest;
    Button btnAdd_list, btnAdd_map;
    TimePicker mTimePicker;

    TextView tvShowTime, tvNotice, tvDestination;
    TextView tvDialogResult;
    int nHourDay, nMinute;
    String AmPm, BoxName, BoxSize, PickupTime, BoxPrice;
    RadioButton rbS, rbM, rbL;
    RadioGroup radioGroup;
    Double nowlatitude, nowlongitude;
    private Intent intent;

    private CardView cv1, cv2, cv3, cv4;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<LocationExample> locationList;

    private DatabaseReference mDatabase;

    private String ssDuration, ssDestLatitude, ssDestLongitude, ssDistance, myLatitude, myLongitude;

    private int PlusDelTime;
    private String getTime;
    private SimpleDateFormat simpleDate;
    private DateFormat dateFormat;

    public static final String SOME_INTENT_FILTER_NAME = "names";
    FragDialog_AddList bottomSheetDialog_addList;
    FragDialog_AddMap bottomSheetDialog_addMap;

    ArrayList<LatLng> MarkerPoints; // 마커 저장


    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //viewGroup = (ViewGroup) inflater.inflate(R.layout.firstpage_fragment3,container,false);
        View v =  inflater.inflate(R.layout.firstpage_fragment3, container, false);
        initFind(v);
        dbConnForLocation();

        // 위치 정보 확인을 위해 정의한 메소드 호출
        startLocationService(); //위치 확인 됨.


        MarkerPoints = new ArrayList<>(); // 마커 저장 시켜놓을 리스트
        MarkerPoints.clear();

        //현재 시간 구해서 걸리는 시간 생각하기
        long now = System.currentTimeMillis();
        final Date mDate = new Date(now);
        simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        getTime = simpleDate.format(mDate);
        //Log.d("현재 :", getTime);
        //Log.d("더할 시간",ssDuration);

        //checkUpdateTime();

        //번들 객체 넘어온지 확인

        RadioGroup.OnCheckedChangeListener mRadioCheck = new RadioGroup.OnCheckedChangeListener() { //라디오버튼으로 상품 크기 구하기/
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (group.getId() == R.id.radiogroup){
                    switch (checkedId){
                        case R.id.radioButton5:
                            BoxSize = "Small";

                            break;
                        case R.id.radioButton6:
                            BoxSize = "Medium";

                            break;
                        case R.id.radioButton7:
                            BoxSize = "Large";

                            break;
                    }
                }
            }
        };


        mTimePicker.setIs24HourView(false);
        //mTimePicker.setOnTimeChangedListener(this);
        radioGroup.setOnCheckedChangeListener(mRadioCheck);
        locationList = new ArrayList<>();



        intent = new Intent(getContext(), AddResult.class);




       mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
           @Override
           public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
               nHourDay = hourOfDay;
               nMinute = minute;

               if (nHourDay >= 12){
                   AmPm = "오후";
                   nHourDay = nHourDay - 12;
               }else{
                   AmPm = "오전";
               }

               PickupTime = AmPm+" "+nHourDay+" "+nMinute;

               /*-------------------------------------------------*/

//               Calendar cal = Calendar.getInstance();
//               cal.set(Calendar.HOUR_OF_DAY, nHourDay);
//               cal.set(Calendar.MINUTE, nMinute);
//
//               //final Date mDate = new Date(now);
//
//               String settingTime = dateFormat.format(cal.getTime());
//               /*simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");*/
//               simpleDate = new SimpleDateFormat("hh:mm");
//               getTime = simpleDate.format(settingTime);
//
//               Log.d("배송 가능 시간", getTime);
//               Log.d("배송 가능 시간", Integer.toString(PlusDelTime));
//
//               cal.setTime(cal.getTime());
//               cal.add(Calendar.MINUTE,PlusDelTime);
//
//               getTime = simpleDate.format(cal.getTime());
//
//               Log.d("배송 가능 시간", getTime);


               /*-------------------------------------------------*/

               tvShowTime.setText(AmPm+" "+nHourDay+"시 "+nMinute+"분 까지 배송을 원합니다.\n");
               tvNotice.setText("교통 상황에 따라 10~15분 정도 오차 범위가 있을 수 있습니다.");

           }

       });

        ;

        btnAddBox2.setOnClickListener(new View.OnClickListener() { //<지도에서 목적지 찾기> 버튼
            @Override
            public void onClick(View v) {

                String sessBoxName = etBoxName.getText().toString().trim(); //상품명
                String sessBoxSize = BoxSize;
                String sessPickupTime = PickupTime;
                String sessBoxPrice = etPrice.getText().toString().trim(); // 가격

                if (sessBoxName.getBytes().length <=0 || sessBoxSize == null || sessPickupTime ==null
                        || sessBoxPrice.getBytes().length <=0){
                    //Toast.makeText(AddBox.this, sessBoxName+"/"+sessBoxSize+"/"+sessBoxPrice+"/"+sessPickupTime, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getContext(), "다른 항목을 먼저 선택한 후 선택해주세요!", Toast.LENGTH_SHORT).show();



                }else{ //입력이라도 되어있다면


//intent로 데이터 넘기기---------------------------------------------------------------수정해야할 부분.
                    intent.putExtra("ssBoxName",sessBoxName);
                    intent.putExtra("ssBoxSize",sessBoxSize);
                    intent.putExtra("ssPickupTime",sessPickupTime);
                    intent.putExtra("ssBoxPrice",sessBoxPrice);
                    intent.putExtra("ssMyLatitude",myLatitude);
                    intent.putExtra("ssMyLongitude",myLongitude);

                    intent.putExtra("ssDestLatitude",ssDestLatitude);
                    intent.putExtra("ssDestLongitude",ssDestLongitude);

                    intent.putExtra("ssDuration",ssDuration);
                    intent.putExtra("ssDistance",ssDistance);

                    startActivity(intent);

                }

            }
        });

        btnAdd_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog_addList= new FragDialog_AddList();
                bottomSheetDialog_addList.show(getFragmentManager(),"approval");


            }
        });

        btnAdd_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog_addMap = new FragDialog_AddMap();
                Bundle bundle1 = new Bundle();
                bundle1.putParcelableArrayList("locationList",(ArrayList<? extends Parcelable>) locationList);
                bottomSheetDialog_addMap.setArguments(bundle1);
                bottomSheetDialog_addMap.show(getFragmentManager(),"approval");

            }
        });


        return v;
    }





    public void initFind(View view){
        etBoxName = view.findViewById(R.id.etBoxName);
        etPrice = view.findViewById(R.id.etPrice);
        btnAddBox = view.findViewById(R.id.btnAddBox);
        btnAddBox2 = view.findViewById(R.id.btnAddBox2);
        tvShowTime = view.findViewById(R.id.tvShowTime);
        tvNotice = view.findViewById(R.id.tvNotice);
        mTimePicker = view.findViewById(R.id.timePicker);

        rbS = view.findViewById(R.id.radioButton5);
        rbM = view.findViewById(R.id.radioButton6);
        rbL = view.findViewById(R.id.radioButton7);

        radioGroup = view.findViewById(R.id.radiogroup);

        btnAddBox = view.findViewById(R.id.btnAddBox);
        btnFindDest = (Button)view.findViewById(R.id.btnFindDest);
        tvDestination = view.findViewById(R.id.tvDestination);

        cv1 = view.findViewById(R.id.cardview1);
        cv2 = view.findViewById(R.id.cardview2);
        cv3 = view.findViewById(R.id.cardview3);
        cv4 = view.findViewById(R.id.cardview4);

        btnAdd_list = view.findViewById(R.id.btnAdd_list);
        btnAdd_map = view.findViewById(R.id.btnAdd_map);
        tvDialogResult = view.findViewById(R.id.tvDialogResult);

    }


    private void dbConnForLocation(){
        /*여기서부터-------------------------------------------------------------------------------------------*/
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("location");  //db테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//파이어베이스 데이터 받아오는 곳

                locationList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                    LocationExample location = snapshot.getValue(LocationExample.class); //만들어뒀던 article 객체에 데이터 담기
                    locationList.add(location);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("Frag1", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/
    }

    private BroadcastReceiver someBroadcastReceiver_list = new BroadcastReceiver() {//리스트에서 오는경우
        @Override
        public void onReceive(Context context, Intent intent) {
            String location_Name = intent.getStringExtra("location_Name");
            String destLati = intent.getStringExtra("location_lati");
            String destLongi = intent.getStringExtra("location_longi");

            LatLng destLL = new LatLng(Double.valueOf(destLati),Double.valueOf(destLongi));
            MarkerPoints.add(destLL);


            for (int i = 0; i < MarkerPoints.size() - 1; i++) {
                /*tv.setText(" 대중 교통 길찾기 실행"+ "\n");*/
                String url = Polyline_DisDur.getUrl(MarkerPoints.get(i), MarkerPoints.get(i + 1)); // 마커 위치정보 넘겨줘서 맞는 url형식 만듬
                Polyline_DisDur.fetchUrl fUrl = new Polyline_DisDur.fetchUrl(); // fetch할 클래스 생성
                fUrl.execute(url); // url fetch
            }

            bottomSheetDialog_addList.dismiss();

        }
    };
    private BroadcastReceiver someBroadcastReceiver_poly = new BroadcastReceiver() { //지도에서 오는경우
        @Override
        public void onReceive(Context context, Intent intent) {
            String duration = intent.getStringExtra("duration");
            String distance = intent.getStringExtra("distance");

            tvDialogResult.setText("소요거리 : "+distance +"\n소요시간 : "+duration);
            bottomSheetDialog_addMap.dismiss();

        }
    };

    private BroadcastReceiver someBroadcastReceiver_map = new BroadcastReceiver() { //지도에서 오는경우
        @Override
        public void onReceive(Context context, Intent intent) {
            String myLati = intent.getStringExtra("myLati");
            String myLongi = intent.getStringExtra("myLongi");
            String destLati = intent.getStringExtra("destLati");
            String destLongi = intent.getStringExtra("destLongi");
            String destination = intent.getStringExtra("destination");
            String duration = intent.getStringExtra("duration");
            String distance = intent.getStringExtra("distance");


            tvDialogResult.setText(destination +"\n소요시간 : "+duration);
            bottomSheetDialog_addMap.dismiss();

        }
    };
    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(someBroadcastReceiver_list, new IntentFilter("names"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(someBroadcastReceiver_map, new IntentFilter("map_result"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(someBroadcastReceiver_poly, new IntentFilter("poly_result"));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(someBroadcastReceiver_list);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(someBroadcastReceiver_map);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(someBroadcastReceiver_poly);
        super.onPause();
    }

    private void checkUpdateTime(){

        String hourRep, minRep;

        if (ssDuration.contains("시간")) {
            //String timetype = ssDuration.replace("시간","").trim();

            Log.d("시간 :", "일단 시간으로 분류");
            int case1 = ssDuration.indexOf("시");
            String hours = ssDuration.substring(0,case1); //시간을 알아냄
            Log.d("시간 :", hours);


            if (ssDuration.contains("분")) {
                Log.d("시간 :", "시간+분으로 분류");

                int case2 = ssDuration.indexOf("간")+1;
                int case3 = ssDuration.indexOf("분");
                String mins = ssDuration.substring(case2,case3); //분을 알아냄

                Log.d("시간 :", hours + "/"+mins);

            }

        }else if(ssDuration.contains("분")){
            Log.d("시간 :", "분으로만 분류");
            String times = ssDuration.replace("분","").trim();

            int case3 = ssDuration.indexOf("분");
            String mins = ssDuration.substring(0,case3); //분을 알아냄
            int DelMin = Integer.parseInt(mins.trim());
            Log.d("바뀐 시간:",Integer.toString(DelMin));

            PlusDelTime = DelMin;


        }


    }


    //** 위치 정보 확인을 위해 정의한 메소드

    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                nowlatitude = lastLocation.getLatitude();
                nowlongitude = lastLocation.getLongitude();

                //출발위치 넣기
                LatLng nowLatlng = new LatLng(nowlatitude,nowlongitude);
                MarkerPoints.add(nowLatlng);
                //Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch(SecurityException ex) {
            ex.printStackTrace();
        }

        //Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }



    //** 리스너 클래스 정의

    private class GPSListener implements LocationListener {
        //** 위치 정보가 확인될 때 자동 호출되는 메소드

        public void onLocationChanged(Location location) {
            nowlatitude = location.getLatitude();
            nowlongitude = location.getLongitude();

            String msg = "Latitude : "+ nowlatitude + "\nLongitude:"+ nowlongitude;
            Log.i("GPSListener", msg);

            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }






}

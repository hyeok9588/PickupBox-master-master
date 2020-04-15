package app.project.com.pickupbox.Register_Box;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.R;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddBox extends AppCompatActivity implements TimePicker.OnTimeChangedListener{

    EditText etBoxName, etPrice;
    Button btnAddBox, btnFindDest;
    TimePicker mTimePicker;

    TextView tvShowTime, tvNotice, tvDestination;
    int nHourDay, nMinute;
    String AmPm, BoxName, BoxSize, PickupTime, BoxPrice;
    RadioButton rbS, rbM, rbL;
    RadioGroup radioGroup;
    Double latitude, longitude;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<LocationExample> locationList;

    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_box);

        // 위치 정보 확인을 위해 정의한 메소드 호출
        startLocationService(); //위치 확인 됨.

        etBoxName = findViewById(R.id.etBoxName);
        etPrice = findViewById(R.id.etPrice);
        btnAddBox = findViewById(R.id.btnAddBox);
        tvShowTime = findViewById(R.id.tvShowTime);
        tvNotice = findViewById(R.id.tvNotice);
        mTimePicker = findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(false);
        mTimePicker.setOnTimeChangedListener(this);

        radioGroup = findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(mRadioCheck);
        rbS = findViewById(R.id.radioButton5);
        rbM = findViewById(R.id.radioButton6);
        rbL = findViewById(R.id.radioButton7);

        btnAddBox = findViewById(R.id.btnAddBox);
        btnFindDest = (Button)findViewById(R.id.btnFindDest);
        tvDestination = findViewById(R.id.tvDestination);
        locationList = new ArrayList<>();


        final Intent intent = new Intent(getBaseContext(),DialogMap.class);




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

                intent.putParcelableArrayListExtra("LocalList2",(ArrayList<LocationExample>) locationList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("Frag1", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

        btnFindDest.setOnClickListener(new View.OnClickListener() { //<지도에서 목적지 찾기> 버튼
            @Override
            public void onClick(View v) {

                String sessBoxName = etBoxName.getText().toString().trim(); //상품명
                String sessBoxSize = BoxSize;
                String sessPickupTime = PickupTime;
                String sessBoxPrice = etPrice.getText().toString().trim(); // 가격

                if (sessBoxName.getBytes().length <=0 || sessBoxSize == null || sessPickupTime ==null
                        || sessBoxPrice.getBytes().length <=0){
                    //Toast.makeText(AddBox.this, sessBoxName+"/"+sessBoxSize+"/"+sessBoxPrice+"/"+sessPickupTime, Toast.LENGTH_SHORT).show();
                    Toast.makeText(AddBox.this, "다른 항목을 먼저 선택한 후 선택해주세요!", Toast.LENGTH_SHORT).show();



                }else{ //입력이라도 되어있다면
                    String myLatitude = latitude.toString();
                    String myLongitude = longitude.toString();

                    //intent로 데이터 넘기기
                    intent.putExtra("sessBoxName",sessBoxName);
                    intent.putExtra("sessBoxSize",sessBoxSize);
                    intent.putExtra("sessPickupTime",sessPickupTime);
                    intent.putExtra("sessBoxPrice",sessBoxPrice);
                    intent.putExtra("myLatitude",myLatitude);
                    intent.putExtra("myLongitude",myLongitude);


                    startActivity(intent);

                }

            }
        });


        btnAddBox.setOnClickListener(new View.OnClickListener() { //배송 버튼 누를 시 DB등록 화면
            @Override
            public void onClick(View v) {

                BoxName = etBoxName.getText().toString(); //상품명
                //boxsize
                //PickupTime
                BoxPrice = etPrice.getText().toString(); // 가격
                Double myLatitude = latitude;
                Double myLongitude = longitude;


                HashMap result = new HashMap<>();  //여기에서 세션값을 가져가기.
                result.put("BoxName",BoxName);
                result.put("BoxSize",BoxSize);
                result.put("PickupTime",PickupTime);
                result.put("BoxPrice",BoxPrice);
                result.put("myLatitude",latitude);
                result.put("myLongitude",longitude);

                //firebase 정의
                mDatabase = FirebaseDatabase.getInstance().getReference();

                //firebase에 저장
                mDatabase.child("BoxList").child(BoxName).setValue(result); //이름을 구분으로 둠 location밑에 이름 밑에 위치 .
            }
        });



    }

    /**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
                latitude = lastLocation.getLatitude();
                 longitude = lastLocation.getLongitude();



                //Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch(SecurityException ex) {
            ex.printStackTrace();
        }

        //Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }

    /**
     * 리스너 클래스 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String msg = "Latitude : "+ latitude + "\nLongitude:"+ longitude;
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
        tvShowTime.setText(AmPm+" "+nHourDay+"시 "+nMinute+"분 까지 배송을 원합니다.");
        tvNotice.setText("시간은 교통 상황에 따라 10~15분 정도 오차 범위가 있을 수 있습니다.");
    }







}

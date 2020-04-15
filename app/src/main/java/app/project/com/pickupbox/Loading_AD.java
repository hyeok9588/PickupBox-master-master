package app.project.com.pickupbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;

import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Main_Page.BoxLocationMap;

public class Loading_AD extends AppCompatActivity {
    private ArrayList<UserBoxInfo> boxList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading__ad);

        Handler handler = new Handler();

        boxList = getIntent().getParcelableArrayListExtra("boxList");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), BoxLocationMap.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //FLAG_ACTIIVTY_CLEAR_TASK는 기존에 쌓여있던 task(stack이 모여 형성하는 작업의 단위(?))를 모두 삭제하는 조건(?)을 받는 flag 상수다.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//이때 FLAG_ACTIVITY_NEW_TASK로 task를 새로 생성한다
                intent.putParcelableArrayListExtra("boxList", boxList);
                startActivity(intent);

            }
        },700);
    }
}

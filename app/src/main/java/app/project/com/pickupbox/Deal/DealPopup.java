package app.project.com.pickupbox.Deal;

import androidx.appcompat.app.AppCompatActivity;

import app.project.com.pickupbox.Data.UserData;
import app.project.com.pickupbox.DealPopup_guest;
import app.project.com.pickupbox.Main_Page.PickupMain;
import app.project.com.pickupbox.Pay.PhoneAuth;
import app.project.com.pickupbox.R;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DealPopup extends AppCompatActivity {

    private TextView txtSenderInfo,txtDealPrice;
    private FirebaseDatabase mFirebaseDatabase;

    Button btnYes, btnNo;

    private String price, nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_deal_popup);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        txtSenderInfo = findViewById(R.id.txtSenderInfo);
        txtDealPrice = findViewById(R.id.txtDealPrice);


        Intent intent = getIntent();
        price = intent.getStringExtra("price");
        nick =  intent.getStringExtra("senderInfo");

        Log.d("펜딩 :",price);
        Log.d("펜딩 :",nick);

        txtSenderInfo.setText(nick);
        txtDealPrice.setText(price);

        btnYes = (Button) findViewById(R.id.yes);
        btnNo = (Button)findViewById(R.id.no);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),PhoneAuth.class);
                Toast.makeText(DealPopup.this, "Yes!!!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),PickupMain.class);
                Toast.makeText(DealPopup.this, "No!!!", Toast.LENGTH_SHORT).show();

                String rejectMsg = "딜을 거절";
/*---------------------------*거절이나 승낙에 대한 답장 해 줄 팝업이 필요------------------------------------------*/
/*---------------------------*거절이나 승낙에 대한 답장 해 줄 팝업이 필요------------------------------------------*/
                startActivity(intent);
            }
        });



    }



}

package app.project.com.pickupbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import app.project.com.pickupbox.Frag.FirstPageFragment2;
import app.project.com.pickupbox.Main_Page.PickupMain;

public class Explain extends AppCompatActivity {
    private Button btnGoBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain);
        btnGoBack=findViewById(R.id.btnGoBack);

        btnGoBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Explain.this, PickupMain.class);
                startActivity(intent);
            }
        });
    }
}

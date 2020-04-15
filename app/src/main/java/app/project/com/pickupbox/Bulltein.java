package app.project.com.pickupbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;

import app.project.com.pickupbox.Frag.FirstPageFragment2;
import app.project.com.pickupbox.Main_Page.PickupMain;

public class Bulltein extends AppCompatActivity {
    private Button btnGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulltein);

        btnGoBack=findViewById(R.id.btnGoBack);

        btnGoBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Bulltein.this, PickupMain.class);
                startActivity(intent);
            }
        });

        }


}


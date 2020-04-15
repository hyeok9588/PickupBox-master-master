package app.project.com.pickupbox.User_Management;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.Main_Page.PickupMain;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.mypage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class LoginResult extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_result; //닉네임 text
    private ImageView iv_profile; //이미지 뷰
    private Button btnGo;

    private static final String TAG = "LoginResult";

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView textViewUserEmail;
    private Button buttonLogout, btnMypage;
    private TextView textivewDelete;

    private String postUserId;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);

        Intent intent = getIntent();
        String nickName = intent.getStringExtra("nickName");    //MainActivity로부터 전달받음. 나중 회원 DB 완성 후 작업 할 공간
        String photoUrl = intent.getStringExtra("photoUrl");

        tv_result = findViewById(R.id.tv_result);
        tv_result.setText(nickName);

        iv_profile = findViewById(R.id.iv_profile);
        Glide.with(this).load(photoUrl).into(iv_profile); //프로필 url을 이미지 뷰에 세팅

        btnGo = findViewById(R.id.goToMain);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),FirstPage.class);
                intent.setAction("ACTION_LOGIN_BACK");
                startActivity(intent);
            }
        });

        btnMypage = findViewById(R.id.goTomypage);
        btnMypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), mypage.class);
                intent1.putExtra("userId",postUserId); //로그인된 사용자의 아이디를 가지고 마이페이지로 넘어간다.
                startActivity(intent1);
            }
        });

        /*-------------------------------------------------------로그인 작업----------------------------------------------*/

        //initializing views
        textViewUserEmail = (TextView) findViewById(R.id.textviewUserEmail);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        textivewDelete = (TextView) findViewById(R.id.textviewDelete);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //textViewUserEmail의 내용을 변경해 준다.
        textViewUserEmail.setText("반갑습니다.\n"+ user.getEmail()+"으로 로그인 하였습니다.");
        postUserId = user.getEmail().substring(0, user.getEmail().indexOf('@'));


        //logout button event
        buttonLogout.setOnClickListener(this);
        textivewDelete.setOnClickListener(this);

        /*-----------------------------------------------------------------------------------------------------*/

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),FirstPage.class);
        intent.setAction("ACTION_LOGIN_BACK");
        startActivity(intent);

        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        if (view == buttonLogout) {
            firebaseAuth.signOut();
            finish();
            //로그아웃--------------------------------------------------------------------------------------//
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = pref.edit(); //SharedPreferences에 editor 생성.
            editor.clear();
            editor.commit();
            //--------------------------------------------------------------------------------------//

            startActivity(new Intent(this, LoginActivity.class));
        }
        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄워야 겠다.
        if(view == textivewDelete) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(LoginResult.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(LoginResult.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                            finish();
                                            Intent intent = new Intent(getApplicationContext(),FirstPage.class);
                                            intent.setAction("ACTION_LOGIN_BACK");
                                            startActivity(intent);
                                        }
                                    });
                        }
                    }
            );
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(LoginResult.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        }
    }

}

package app.project.com.pickupbox.User_Management;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import app.project.com.pickupbox.Data.UserData;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class AddUser extends AppCompatActivity implements View.OnClickListener{

    //define view objects
    EditText editTextName, editTextPhone, editTextAddr;
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignup;
    TextView textviewSingin;
    TextView textviewMessage;
    ProgressDialog progressDialog;
    RadioGroup rgGender;
    RadioButton rbMan, rbWoman;

    //define firebase object
    FirebaseAuth firebaseAuth;

    DatabaseReference mDatabase;

    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            //이미 로그인 되었다면 이 액티비티를 종료함
            finish();
/*
            //이미 로그인 되었다면 해당 아이디의 토큰값과 이메일 앞 부분 아이디를 가져가서 DB에 저장한다.
            FirebaseUser user = firebaseAuth.getCurrentUser();
            UserData userData = new UserData();
            userData.userEmailID = user.getEmail().substring(0, user.getEmail().indexOf('@'));
            userData.fcmToken = FirebaseInstanceId.getInstance().getToken();

            //firebase 정의
            mDatabase = FirebaseDatabase.getInstance().getReference("users");

            //db에 넣을 정보와 path 설정
            mDatabase.child(userData.userEmailID).setValue(userData);*/


            //그리고 profile 액티비티를 연다.
            startActivity(new Intent(getApplicationContext(), LoginResult.class)); //LoginActivity 이동
        }
        //initializing views
        editTextName = (EditText)findViewById(R.id.editTextName);
        editTextPhone = (EditText)findViewById(R.id.editTextPhone);
        editTextAddr = (EditText)findViewById(R.id.editTextAddr);
        rgGender = (RadioGroup)findViewById(R.id.genderGroup);
        rbMan = (RadioButton)findViewById(R.id.genderMan);
        rbWoman = (RadioButton)findViewById(R.id.genderWomen);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textviewSingin= (TextView) findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        buttonSignup = (Button) findViewById(R.id.buttonSignup);
        progressDialog = new ProgressDialog(this);

        //button click event
        buttonSignup.setOnClickListener(this);
        textviewSingin.setOnClickListener(this);



    }

    //Firebse creating a new user
    private void registerUser(){
        //사용자가 입력하는 email, password를 가져온다.
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String addr = editTextAddr.getText().toString().trim();

        //정보 비어있는지 체크
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }else{
            int count =0;
            for (int i =0; i<password.length();i++){
                if (password.charAt(i) !=' '){
                    count++;
                }
            }
            if (count<6){
                Toast.makeText(this, "비밀번호는 6자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(TextUtils.isEmpty(name)) {
            Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "휴대폰 번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }else{
            int count =0;
            for (int i =0; i<phone.length();i++){
                if (phone.charAt(i) !=' '){
                    count++;
                }
            }
            if (count!=11){
                Toast.makeText(this, "휴대폰 번호를 11자리 정확히 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        if(TextUtils.isEmpty(addr)){
            Toast.makeText(this, "주소를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }
        if(gender==null){
            //Toast.makeText(this, "성별을 정확히 선택해 주세요.", Toast.LENGTH_SHORT).show();
            if (rbMan.isChecked()){
                gender="남";
            }else{
                gender="여";
            }
        }


        //email과 password가 제대로 입력되어 있다면 계속 진행된다.
        progressDialog.setMessage("등록중입니다. 기다려 주세요...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ //회원가입 완료
                            finish();

                            //회원가입 처음 하는 사람일 경우 FCM 토큰 값과 아이디를 같이 저장한다. -> 나중 채팅 알람 푸시를 위한.
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            UserData userData = new UserData();
                            userData.userEmailID = user.getEmail().substring(0, user.getEmail().indexOf('@'));
                            userData.fcmToken = FirebaseInstanceId.getInstance().getToken();
                            userData.userName = name;
                            userData.userPhone = phone;
                            userData.userAddr = addr;
                            userData.userGender = gender;

                            //firebase 정의
                            mDatabase = FirebaseDatabase.getInstance().getReference("users");

                            //db에 넣을 정보와 path 설정
                            mDatabase.child(userData.userEmailID).setValue(userData);


                            //그리고 profile 액티비티를 연다.
                            Intent intent = new Intent(getApplicationContext(), FirstPage.class);
                            intent.setAction("ACTION_LOGIN_OKAY");
                            startActivity(intent);
                        } else {
                            //에러발생시
                            textviewMessage.setText("에러유형\n - 이미 등록된 이메일  \n -암호 최소 6자리 이상 \n - 서버에러");
                            Toast.makeText(AddUser.this, "등록 에러!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    //button click event
    @Override
    public void onClick(View view) {
        if(view == buttonSignup) {
            //TODO
            registerUser();
        }

        if(view == textviewSingin) {
            //TODO
            startActivity(new Intent(this, LoginActivity.class)); //추가해 줄 로그인 액티비티
        }
    }






}

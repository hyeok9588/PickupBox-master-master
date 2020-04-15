package app.project.com.pickupbox.Frag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.Data.UserData;
import app.project.com.pickupbox.Delivery_Now.MainFrag;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.R;
import app.project.com.pickupbox.TermsAgree;
import app.project.com.pickupbox.User_Management.AddUser;
import app.project.com.pickupbox.User_Management.FindPW;
import app.project.com.pickupbox.User_Management.LoginActivity;
import app.project.com.pickupbox.User_Management.LoginResult;

public class FirstPageFragment5 extends Fragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private String nick, userName;
    private SignInButton btn_google; //구글 로그인 버튼
    private FirebaseAuth auth; //파이어 베이스 인증 객체
    private GoogleApiClient googleApiClient = null; // 구글 API 클라이언트 객체
    private static final int REQ_SIGN_GOOGLE = 100; //구글 결과 코드

    //define view objects
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignin;
    TextView textviewSingin;
    TextView textviewMessage;
    TextView textviewFindPassword;
    ProgressDialog progressDialog;

    //define firebase object
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference mDatabase = null;

    Map<String,Object> userMap = null;
    HashMap<String,Object> userUpdates = null;
    ViewGroup viewGroup;

    UserData userData;
    UserData new_userData;

    private String ex_emailID, ex_userName, ex_userGender, ex_userAddr, ex_userPhone, ex_fcmToken;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //viewGroup = (ViewGroup) inflater.inflate(R.layout.firstpage_fragment5,container,false);
        View v =  inflater.inflate(R.layout.firstpage_fragment5, container, false);


      /*  Intent intent = new Intent(getContext(),LoginActivity.class);
        startActivity(intent);*/

        /*-----------------------------------일반 로그인 코드----------------------------------------*/

        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            //이미 로그인 되었다면 이 액티비티를 종료함
            getActivity().finish();

            //그리고 profile 액티비티를 연다.
            Intent intent = new Intent(getContext(), FirstPage.class);
            intent.setAction("ACTION_LOGIN_OKAY");
            startActivity(intent);
        }

        //initializing views
        editTextEmail = (EditText) v.findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) v.findViewById(R.id.editTextPassword);
        textviewSingin= (TextView) v.findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) v.findViewById(R.id.textviewMessage);
        textviewFindPassword = (TextView) v.findViewById(R.id.textViewFindpassword);
        buttonSignin = (Button) v.findViewById(R.id.buttonSignup);
        progressDialog = new ProgressDialog(getContext());

        //button click event
        buttonSignin.setOnClickListener(this);
        textviewSingin.setOnClickListener(this);
        textviewFindPassword.setOnClickListener(this);

        /*--------------------------------------------------------------------------------------------------*/


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();



        googleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(),1, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        auth = FirebaseAuth.getInstance(); //파이어베이스 인증 객체 초기화.

        btn_google = v.findViewById(R.id.btn_google);
        btn_google.setOnClickListener(new View.OnClickListener() { //구글 로그인 버튼 클릭 시 수행
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, REQ_SIGN_GOOGLE);
            }
        });

        return v;
    }



    /*-------------------------------------------------------일반 로그인 코드-----------------------------------------------------------------------*/

    //firebase userLogin method
    private void userLogin(){
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(getContext(), "email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(getContext(), "password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("로그인f중입니다. 잠시 기다려 주세요...");
        progressDialog.show();

        //logging in the user

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressDialog.dismiss();

                        if(task.isSuccessful()) { //로그인 성공적.

                            //로그인 할 경우, 로그인 정보를 계속 가지고 있는다.
                            Context context = getActivity();
                            SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            String emailAd = email.substring(0, email.indexOf('@')); //이메일 로그인이기에 @ 뒷부분 제거 후 아이디만 key값으로 가지고 있는다.
                            editor.putString("email", emailAd);
                            editor.commit();

                            /*-------------------------------------------*/
                            MyAsyncTask mAsyncTask = new MyAsyncTask();
                            String new_token = FirebaseInstanceId.getInstance().getToken();
                            Log.d("로그인 확인", emailAd+"/// token"+new_token);
                            mAsyncTask.execute(emailAd, new_token);




                            Intent intent = new Intent(getContext(), FirstPage.class);
                            intent.setAction("ACTION_LOGIN_OKAY");
                            startActivity(intent);

                           // startActivity(new Intent(getContext(), LoginResult.class));

                        } else {
                            Toast.makeText(getContext(), "로그인 실패!", Toast.LENGTH_LONG).show();
                            textviewMessage.setText("로그인 실패 유형\n - password가 맞지 않습니다.\n -서버에러");
                        }
                    }
                });
    }



    @Override
    public void onClick(View view) {
        if(view == buttonSignin) {
            userLogin();
        }
        if(view == textviewSingin) {
            getActivity().finish();
            startActivity(new Intent(getActivity(), TermsAgree.class));
        }
        if(view == textviewFindPassword) {
            getActivity().finish();
            startActivity(new Intent(getActivity(), FindPW.class));
        }
    }

    /*------------------------------------------------------------------------------------------------------------------------------*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //구글 로그인 인증을 요청 했을 때 결과 값을 되돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) { //인증결과가 성공?
                GoogleSignInAccount account = result.getSignInAccount(); //account라는 데이터는 구글로그인 정보를 담고 있다. (닉넴,프로필,이메일주소..)
                Toast.makeText(getContext(), "로그인 성공 1", Toast.LENGTH_SHORT).show();
                resultLogin(account); //로그인 결과 값 출력 수행하는 메소드
            }
        }


    }

    private void resultLogin(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(),new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { //로그인 성공이라면
                            Toast.makeText(getActivity(), "로그인 성공", Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(getContext(),FirstPage.class);

                            intent1.setAction("ACTION_LOGIN_FALSE");
                            intent1.putExtra("nickName", account.getDisplayName());
                            intent1.putExtra("photoUrl", String.valueOf(account.getPhotoUrl())); //String.valueOf 특정 자료형을 String형으로 변환.
                            startActivity(intent1);


                            //로그인 할 경우, 로그인 정보를 계속 가지고 있는다.
                            SharedPreferences pref = getContext().getSharedPreferences("MyPref", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            String emailAd = account.getEmail().substring(0, account.getEmail().indexOf('@')); //이메일 로그인이기에 @ 뒷부분 제거 후 아이디만 key값으로 가지고 있는다.
                            editor.putString("email", emailAd);
                            editor.commit();



                            startActivity(intent1);

                        } else { //로그인 실패라면
                            Toast.makeText(getContext(),  "로그인 실패", Toast.LENGTH_SHORT).show();


                        }
                    }
                });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*-------------------------------------------------------------------------*/

    //AsyncTask<doInBackground, onPreexecute, onPostexecute>
    public class MyAsyncTask extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... strings) {
            try {
                String emailIDS = strings[0];
                final String newToken = strings[1];
                Log.d("로그인 확인_비동기 데이터 전달", emailIDS+"/// token"+newToken);

                database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
                database.getReference("users")
                        .child(emailIDS)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                userData = dataSnapshot.getValue(UserData.class);

                                //토큰만 갱신
                                userData.fcmToken = newToken;

                                //업데이트 메소드 호출
                                upDateNewFCM(userData);


                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);



        }


    }


    public void upDateNewFCM(UserData userData){
        //firebase 정의
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        //db에 넣을 정보와 path 설정
        mDatabase.child(userData.userEmailID).setValue(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("fcm login","db success");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("fcm login","db negative");

                    }
                });


    }






}

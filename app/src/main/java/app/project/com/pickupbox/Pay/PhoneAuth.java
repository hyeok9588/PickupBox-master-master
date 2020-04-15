package app.project.com.pickupbox.Pay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import app.project.com.pickupbox.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {

    EditText etPhone, etOtp;
    Button btSendOtp, btResendOtp, btVerifyOtp;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth fbAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        etPhone = findViewById (R.id. et_phone );
        etOtp = findViewById (R.id. et_otp );
        btSendOtp = findViewById (R.id. bt_send_otp );
        btResendOtp = findViewById (R.id.bt_resend_otp );
        btVerifyOtp = findViewById (R.id. bt_verify_otp );


        btVerifyOtp.setEnabled(false);
        btResendOtp.setEnabled(false);

        fbAuth = FirebaseAuth.getInstance();

    }

    public void sendCode(View view){
        String phoneNumber = etPhone.getText().toString();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks
        );
    }

    private void setUpVerificationCallbacks(){
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                } else if (e instanceof FirebaseTooManyRequestsException) {

                }
            }



            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                phoneVerificationId = verificationId;
                resendToken = token;

                btSendOtp.setEnabled(true);
                btResendOtp.setEnabled(true);
                btVerifyOtp.setEnabled(true);

            }
        };
    }

    public void verifyCode(View view){
        String code = etOtp.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            etOtp.setText("");
                            btResendOtp.setEnabled(false);
                            btVerifyOtp.setEnabled(false);
                            FirebaseUser user = task.getResult().getUser();
                            String a = user.getEmail();
                            Toast.makeText(getApplicationContext(),a+"님 인증 완료되었습니다.",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(),Payment.class);
                            startActivity(intent);

                        }else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){

                            }
                        }
                    }
                });
    }

    public void resendCode(View view){
        String phoneNumber = etPhone.getText().toString();
        setUpVerificationCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }









}

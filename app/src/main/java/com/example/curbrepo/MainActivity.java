package com.example.curbrepo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.curbrepo.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //if code send failed, will used to resend code OTP
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId; //will hold OTP/verification code

    private static final String TAG = "MAIN_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneL1.setVisibility(View.VISIBLE); //show phone layout
        binding.codeL1.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        //init progress dialog

        pd = new ProgressDialog(this);
        pd.setTitle("Please wait.....");
        pd.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
            pd.dismiss();
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.d(TAG,"onCodeSent: "+ verificationId);
                mVerificationId = verificationId;
               forceResendingToken =  token;
               pd.dismiss();
              //hide phone layout, show code layout
                binding.phoneL1.setVisibility(View.GONE);
                binding.codeL1.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, "verification code sent....", Toast.LENGTH_SHORT).show();

                binding.codeSentDescription.setText("Please type the verification code we sent \nto" +binding.phoneEt.getText().toString().trim());
            }
        };

        binding.phoneContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String phone = binding.phoneEt.getText().toString().trim();
            if(TextUtils.isEmpty(phone)){
                Toast.makeText(MainActivity.this, "Please enter phone number....", Toast.LENGTH_SHORT).show();
            }
            else
            {
                startPhoneNumberVerification(phone);
            }
            }
        });
        binding.resendcodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(MainActivity.this, "Please enter phone number....", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    resendVerificationCode(phone, forceResendingToken);
                }

            }
        });
        binding.codeSubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             String code = binding.codeEt.getText().toString().trim();
             if(TextUtils.isEmpty(code)){
                 Toast.makeText(MainActivity.this, "Please enter verification code...", Toast.LENGTH_SHORT).show();
             }
             else
             {
                 verifyPhoneNumberwithCode(mVerificationId, code);
             }
            }
        });
    }

    private void verifyPhoneNumberwithCode(String VerificationId, String code) {
        pd.setMessage("Verifying Code");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationId, code);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    pd.setMessage("Logging In");

    firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                //sucessfully signed In
                    pd.dismiss();
                    String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                    Toast.makeText(MainActivity.this, "Logged In as" + phone, Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
               //failed signing in
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token)
    {
     pd.setMessage("Resending Code");
     pd.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void startPhoneNumberVerification(String phone) {
        pd.setMessage("Verifying Phone Number");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

}
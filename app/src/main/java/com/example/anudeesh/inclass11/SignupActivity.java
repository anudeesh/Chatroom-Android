package com.example.anudeesh.inclass11;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText emailField, pwdField, cpwdField, fnameField, lnameField;
    private Button signUpButton, cancelButton;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailField = (EditText) findViewById(R.id.editTextEmailValSignup);
        pwdField = (EditText) findViewById(R.id.editTextPwdValSignup);
        cpwdField = (EditText) findViewById(R.id.editTextRepeatPwdValSignup);
        fnameField = (EditText) findViewById(R.id.editTextFNameValSignup);
        lnameField = (EditText) findViewById(R.id.editTextLNameValSignup);
        signUpButton = (Button) findViewById(R.id.buttonSignup);
        cancelButton = (Button) findViewById(R.id.buttonCancelSignup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                String pwd = pwdField.getText().toString();
                String cpwd = cpwdField.getText().toString();
                if(pwd.equals(cpwd)) {
                    createAccount(email, pwd);
                } else {
                    Toast.makeText(SignupActivity.this, "Passwords mismatch", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignupActivity.this.finish();
                //finish();
            }
        });
    }

    private void createAccount(String email, String password) {
        Log.d("demo", "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        //showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "createUserWithEmail:onComplete:" + task.isSuccessful());


                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Account not created. Choose a different email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String uid = getUid();
                            String key = mDatabase.child("users").push().getKey();
                            /*Message msg = new Message();
                            msg.setFname(fnameField.getText().toString());
                            msg.setLname(lnameField.getText().toString());
                            msg.setUid(uid);
                            Map<String, Object> msgValues = msg.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();*/
                            //childUpdates.put("/expenses/" + key, expValues);
                            //childUpdates.put("/user-messages/" + uid + "/" + key, msgValues);

                            //mDatabase.updateChildren(childUpdates);
                            mDatabase.child("Users").child(String.valueOf(task.getResult().getUser().getUid())).child("First Name").setValue(fnameField.getText().toString());
                            mDatabase.child("Users").child(String.valueOf(task.getResult().getUser().getUid())).child("Last Name").setValue(lnameField.getText().toString());
                            Toast.makeText(SignupActivity.this, "User has been created", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(AddExpensesActivity.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                            //finish();
                            Intent intn = new Intent(SignupActivity.this,MainActivity.class);
                            //intn.putExtra("User",msg);
                            startActivity(intn);
                        }
                        //hideProgressDialog();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String password = pwdField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            pwdField.setError("Required.");
            valid = false;
        } else {
            pwdField.setError(null);
        }

        String fname = fnameField.getText().toString();
        if (TextUtils.isEmpty(fname)) {
            fnameField.setError("Required.");
            valid = false;
        } else {
            fnameField.setError(null);
        }

        String lname = lnameField.getText().toString();
        if (TextUtils.isEmpty(lname)) {
            lnameField.setError("Required.");
            valid = false;
        } else {
            lnameField.setError(null);
        }
        return valid;
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}

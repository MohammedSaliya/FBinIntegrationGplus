package com.example.fbinintegration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    // for facebook
    CallbackManager callbackManager;

    //For Google Plus
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private SignInButton btn_SignIN;
    private Button btn_SignOut;
    private LinearLayout ll_profileLayout;
    private ImageView iv_profilePic;
    private TextView tv_Name, tv_Email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //For Google Plus
        btn_SignIN = findViewById(R.id.btn_sign_in);
        btn_SignOut = findViewById(R.id.btn_sign_out);
        ll_profileLayout = findViewById(R.id.ll_Profile);
        iv_profilePic = findViewById(R.id.iv_ProfilePic);
        tv_Name = findViewById(R.id.tv_Name);
        tv_Email = findViewById(R.id.tv_Email);

        //For Google Plus
        btn_SignIN.setOnClickListener(this);
        btn_SignOut.setOnClickListener(this);

        //For Google Plus
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        // Customizing GooglePlus button
        btn_SignIN.setSize(SignInButton.SIZE_STANDARD);
        btn_SignIN.setScopes(googleSignInOptions.getScopeArray());


        // for facebook
        callbackManager = CallbackManager.Factory.create();

        // for facebook
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }


    //For GooglePlus
    private void signIn() {
        Intent signInintent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInintent, RC_SIGN_IN);
    }


    //For GooglePlus
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                UpdateUI(false);
            }
        });
    }


    //For GooglePlus
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();

            Log.e(TAG, "display name: " + googleSignInAccount.getDisplayName());

            String user_name = googleSignInAccount.getDisplayName();
//            String user_profilePic = googleSignInAccount.getPhotoUrl().toString();
            String user_email = googleSignInAccount.getEmail();

            Log.e(TAG, "Name:" + user_name + ",user_email:" + user_email);
//                    + ",Image:" + user_profilePic);

            tv_Name.setText(user_name);
            tv_Email.setText(user_email);

//            Picasso.with(getApplicationContext()).load(user_profilePic).into(iv_profilePic);
            UpdateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            UpdateUI(false);
        }
    }

    //For GooglePlus
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_sign_in:
                signIn();
            case R.id.btn_sign_out:
                signOut();
        }
    }

    // for facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        //For GooglePlus
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(googleSignInResult);
        }
    }

    //For GooglePlus
    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> googleSignInResultOptionalPendingResult = Auth.GoogleSignInApi
                .silentSignIn(mGoogleApiClient);
        if (googleSignInResultOptionalPendingResult.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = googleSignInResultOptionalPendingResult.get();
//            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            googleSignInResultOptionalPendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    hideProgressDialog();
                    handleSignInResult(result);
                }
            });
        }

    }

    //For GooglePlus
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    //For GooglePlus
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    //For GooglePlus
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    //For GooglePlus
    private void UpdateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btn_SignIN.setVisibility(View.GONE);
            btn_SignOut.setVisibility(View.VISIBLE);
            ll_profileLayout.setVisibility(View.VISIBLE);
        } else {
            btn_SignIN.setVisibility(View.VISIBLE);
            btn_SignOut.setVisibility(View.GONE);
            ll_profileLayout.setVisibility(View.GONE);
        }
    }


}
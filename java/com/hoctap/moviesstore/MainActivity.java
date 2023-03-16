package com.hoctap.moviesstore;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // google
    GoogleSignInClient googleSignInClient;
    SignInButton googleSignin;
    private int REQUES_CODE = 123;
    // facebook
    LoginButton facebookLogin;
    CallbackManager callbackManager;
    // inten cho phép chuyển màn hình sang màn hình thứ 2 là moviesActivity
    Intent intent;
    private int REQUES_CODE_MOVIES = 1234;
    // sharedprefence dùng lưu tài khoản đã đăng nhập.
    SharedPreferences sharedPreferences;
    // id để lưu giá trị người dùng đăng nhập id = 1 google, 2 facebook.
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Chuyển sang màn hình moviesActivity
        intent = new Intent(MainActivity.this, MoviesActivity.class);

        // Yêu cầu kết nối đến google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignin = findViewById(R.id.googleSignIn);

        googleSignin.setOnClickListener(this);

        // Tạo kết nối với facebook
        callbackManager = CallbackManager.Factory.create();

        facebookLogin = findViewById(R.id.facebookSignIn);
        facebookLogin.setReadPermissions("email");

        facebookLogin();

        // Shareprefence dùng để lưu tài khoản người dùng cho những lần đăng nhập tiếp theo.

        sharedPreferences = getSharedPreferences("saveaccount", MODE_PRIVATE);

        id = sharedPreferences.getInt("account", 0);

        // Kiểm tra trạng thái đăng nhập từ trước
        if (id == 1) {
            signInGoogle();
        }else if (id == 2) {
            facebookLogin.setVisibility(View.INVISIBLE);
            googleSignin.setVisibility(View.VISIBLE);
            intent.putExtra("key", "facebook");
            startActivityForResult(intent, REQUES_CODE_MOVIES);
        }
    }

    /**
     * bắt sự kiện khi người dùng nhấp vào button đăng nhập bằng google
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.googleSignIn:
                signInGoogle();
                break;
        }
    }

    /**
     * Kết nối với google
     */
    private void signInGoogle() {
        Intent googleIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(googleIntent, REQUES_CODE);
    }

    /**
     * Dữ liệu gửi từ ActivityForResult sẽ được nhận lại qua onActiviResult
     * @param requestCode gửi mã yêu cầu, dùng để phân biệt các intent được gửi theo cùng phương thức
     * @param resultCode mã nhận lại, phân biệt các kết quả được trả lại
     * @param data dữ liệu trả về
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Lấy thông tin đăng nhập nếu người dùng đăng nhập bằng tài khoản google
        if (requestCode == REQUES_CODE) {
            // Nhận dữ liệu từ google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else {
            // Nhận dữ liệu từ facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // Đăng xuất ứng dụng facebook và google.
        if (requestCode == REQUES_CODE_MOVIES && resultCode == RESULT_OK && data != null) {
            // phân biệt đăng xuất tài khoản google và facebook thông qua keyExit trả vè.
            if (data.getStringExtra("keyExit").equals("google")) {
                signOutGoogle();
            }else if (data.getStringExtra("keyExit").equals("facebook")) {
                logoutFacebook();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Hoàn thành đăng nhập trả về giao diện với UI
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Đăng nhập thành công lưu lại tài khoản người sử dụng, khi người vào ứng dụng lần sau sẽ tự động đăng nhập
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("account", 1);
            editor.commit();

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            /* Khi người dùng đăng nhập trước bằng facebook và ấn back sau đó đăng nhập tiếp bằng tài khoản google thì
            sẽ logout tài khoản facebook trước đó ra.
            */
            AccessToken token = AccessToken.getCurrentAccessToken();
            if (token != null) {
                logoutFacebook();
            }
            googleSignin.setVisibility(View.INVISIBLE);
            facebookLogin.setVisibility(View.VISIBLE);
            intent.putExtra("key", "google");
            startActivityForResult(intent, REQUES_CODE_MOVIES);

        } catch (ApiException e) {
            Log.d("err", "" + e.getStatusCode());
        }
    }

    /**
     * Đăng nhập bằng tài khoản facebook
     */
    private void facebookLogin() {
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Lưu thông tin tài khoản facebook
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("account", 2);
                editor.commit();
                // Đăng nhập thành công đồng thời logout tài khoản google đã đăng nhập
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                if (account != null) {
                    signOutGoogle();
                }
                facebookLogin.setVisibility(View.INVISIBLE);
                googleSignin.setVisibility(View.VISIBLE);
                intent.putExtra("key", "facebook");
                startActivityForResult(intent, REQUES_CODE_MOVIES);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    /**
     * Đăng xuất tài khoản google khỏi thiết bị.
     */
    public void signOutGoogle() {
        // Thiết lập id về trạng thái không còn lưu tài khoản đăng nhập
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("account", 0);
        editor.commit();
        facebookLogin.setVisibility(View.VISIBLE);
        googleSignin.setVisibility(View.VISIBLE);
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    /**
     * Đăng xuất tài khoản facebook khỏi thiết bị.
     */
    public void logoutFacebook() {
        // Người dùng logout thì thiết lập id về trạng thái ban đầu
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("account", 0);
        editor.commit();
        facebookLogin.setVisibility(View.VISIBLE);
        googleSignin.setVisibility(View.VISIBLE);
        LoginManager.getInstance().logOut();
    }
}
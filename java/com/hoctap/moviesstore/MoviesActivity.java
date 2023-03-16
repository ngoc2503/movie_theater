package com.hoctap.moviesstore;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity {
    // gridViewMovies hiển thị danh sách các bộ phim dưới dạng các hàng và cột
    GridView gridViewMovies;
    // adapter
    MoviesAdapter adapter;
    // Danh sách các bộ phim
    List<Movies> moviesList;
    //Intent
    Intent myIntent;
    // key Phân biệt người dùng đăng nhập bằng google hay facebook
    private String key;
    // Share image
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        myIntent = getIntent();
        key = myIntent.getStringExtra("key");

        moviesList = new ArrayList<>();
        gridViewMovies = findViewById(R.id.gridViewMovies);

        new ReadJson().execute("https://api.androidhive.info/json/movies_2017.json");

        shareDialog = new ShareDialog(this);

    }

    // class đọc dữ liệu từ json
    private class ReadJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            try {
                // url là địa chỉ được truyền vào
                URL url = new URL(strings[0]);
                InputStreamReader inputStreamReader = new InputStreamReader(url.openConnection().getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                // Tạo jsonArray từ chuổi s
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    // tạo jsonObjet
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Movies movies = new Movies(jsonObject.getString("image"), jsonObject.getString("title"),
                            jsonObject.getString("price"));
                    moviesList.add(movies);
                }

                adapter = new MoviesAdapter(MoviesActivity.this, R.layout.custom_row_movies, moviesList);
                gridViewMovies.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }

    /**
     * Tạo menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Bắt sự kiện trong menu khi người dùng chọn một item trong menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile:
                showDialog();
                break;
            case R.id.menu_exit:
                Intent intent = new Intent();
                intent.putExtra("keyExit", key);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Dialog cho phép hiển thị thông tin của người đăng nhập bao gồm hình ảnh,
     * tên và email.
     */
    public void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCanceledOnTouchOutside(false);

        // ánh xạ các VIew qua dialog
        ImageView imgProfile = dialog.findViewById(R.id.imgProfile);
        TextView tvName = dialog.findViewById(R.id.tvName);
        TextView tvEmail = dialog.findViewById(R.id.tvEmail);
        Button btnOkDialog = dialog.findViewById(R.id.btnOkDialog);

        // Lấy thông tin khi người dùng đăng nhập bằng tài khoản google hiển thị lên UI
        if (key.equals("google")) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MoviesActivity.this);
            tvEmail.setText(account.getEmail());
            tvName.setText(account.getDisplayName());
            Picasso.get().load(account.getPhotoUrl()).into(imgProfile);
        }else if (key.equals("facebook")) {
            // khi người dùng đăng nhập bằng tài khoản facebook hiển thị lên UI
            AccessToken token = AccessToken.getCurrentAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        tvEmail.setText(object.getString("email"));
                        tvName.setText(object.getString("name"));
                        Picasso.get().load("http://graph.facebook.com/"+object.getString("id")+"/picture?type=large")
                                .into(imgProfile);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields","email, id,name,link");
            request.setParameters(parameters);
            request.executeAsync();
        }
        // sự kiện khi người dùng nhấn vào button OK
        btnOkDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        // THiết lập độ rộng của dialog bằng với chiều rộng của Activity/\.
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void shareImage(int index) {
        if (key.equals("facebook")) {
            showAlertDialog(index);
        } else {
            Toast.makeText(getApplicationContext(), "Chức năng này chỉ dùng cho đăng nhập bằng facebook",
                    Toast.LENGTH_LONG).show();
        }

    }
    public void showAlertDialog(int index) {
        Movies movies = moviesList.get(index);
        String resource = movies.getImgSource();

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Thông báo!");
        dialog.setMessage("Bạn có chắc muốn thêm hình ảnh này vào dòng thời gian không?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new GetBitmap().execute(resource);
            }
        });
        dialog.show();
    }

    /**
     * lấy bitmap và share ảnh lên facebook
     * @param bitmap
     */
    public void shareImage(Bitmap bitmap) {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        shareDialog.show(content);
    }

    /*
     * Đọc hình ảnh từ URL dưới dạng bitmap
     * */
    public class GetBitmap extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(strings[0]);
                InputStream inputStream = url.openConnection().getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            shareImage(bitmap);
            super.onPostExecute(bitmap);
        }
    }
}

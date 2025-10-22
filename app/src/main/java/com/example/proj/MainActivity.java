package com.example.proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private EditText editname;
    private EditText editpasswd;
    private CheckBox cbrem;
    private Button btnlogin;
    private Button btnregis;
    //TextInputLayout MaterialDesign控件
    private TextInputLayout layoutname;
    private TextInputLayout layoutpasswd;
    private LinearLayout layoutmain;
    private LinearLayout layoutcenter;
    private SharedHelper sh;
    private TextView textfree;
    private boolean flag=true;
    private MyDBOpenHelper dbOpenHelper;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(MainActivity.this, "可能无法记录步行距离，如要使用请手动赋予权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseApplication.remeberActivity(this);
        mContext = getApplicationContext();
        editname = (EditText)findViewById(R.id.editTextName);
        editpasswd = (EditText)findViewById(R.id.editTextPassword);
        cbrem=(CheckBox)findViewById(R.id.checkboxrem);
        btnlogin = (Button)findViewById(R.id.loginbutton);
        btnregis = (Button)findViewById(R.id.regisbutton);
        layoutname=(TextInputLayout)findViewById(R.id.layoutName);
        layoutpasswd=(TextInputLayout)findViewById(R.id.layoutPasswd);
        layoutmain=(LinearLayout)findViewById(R.id.layoutMain);
        layoutcenter=(LinearLayout)findViewById(R.id.layoutCenter);
        textfree=(TextView)findViewById(R.id.textfree);
        dbOpenHelper=new MyDBOpenHelper(getApplicationContext(), "my.db", null, 1);
        //获取gps权限以记录步行距离
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        sh = new SharedHelper(mContext);
        if(sh.getAuto()==false){
            sh.clear();
        }
        //如已经记住，自动进入
        if(!sh.readUsername().equals("")&&sh.readUsername()!=null){
            layoutname.setVisibility(View.GONE);
            layoutpasswd.setVisibility(View.GONE);
            cbrem.setVisibility(View.GONE);
            btnlogin.setVisibility(View.GONE);
            btnregis.setVisibility(View.GONE);
            textfree.setVisibility(View.GONE);
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, WalkActivity.class);
            MainActivity.this.startActivity(intent);
        }else {
            layoutname.setVisibility(View.VISIBLE);
            layoutpasswd.setVisibility(View.VISIBLE);
            cbrem.setVisibility(View.VISIBLE);
            btnlogin.setVisibility(View.VISIBLE);
            btnregis.setVisibility(View.VISIBLE);
            textfree.setVisibility(View.VISIBLE);
        }
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag;
                //用户名未填写
                if(editname.getText().toString().equals("")){
                    layoutname.setError("用户名还未填写");
                    flag=false;
                }else {
                    layoutname.setErrorEnabled(false);
                    flag=true;
                }
                //密码未填写
                if(editpasswd.getText().toString().equals("")){
                    layoutpasswd.setError("密码还未填写");
                    flag=false;
                }else {
                    layoutpasswd.setErrorEnabled(false);
                    flag=true;
                }
                if(flag==true) {
                    SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
                    Cursor cursor=db.query("user", new String[]{"pwd"}, "uname=?", new String[]{editname.getText().toString()}, null, null, null);
                    if (cursor.getCount()==0){
                        layoutname.setError("请检查该用户是否已经注册");
                    }else {
                        //密码正误判断
                        cursor.moveToNext();
                        if(cursor.getString(cursor.getColumnIndex("pwd")).equals(editpasswd.getText().toString())){
                            //是否选中记住
                            if(cbrem.isChecked()==true){
                                sh.setAuto(true);
                            }else{
                                sh.setAuto(false);
                            }
                            //用户名保存到sharepreference
                            sh.save(editname.getText().toString());
                            flag=false;
                            //为了界面转换的效果调用layoutmain的click函数
                            layoutmain.callOnClick();
                        }else{
                            layoutpasswd.setError("请检查密码是否正确");
                        }
                    }
                }
            }
        });
        editname.addTextChangedListener(new TextWatcher() {
            //填写过程中判断
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editname.getText().toString().equals("")||editname.getText()==null){
                    layoutname.setError("用户名还未填写");
                }else {
                    layoutname.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editname.getText().toString().equals("")||editname.getText()==null){
                    layoutname.setError("用户名还未填写");
                }else {
                    layoutname.setErrorEnabled(false);
                }
            }

        });
        editpasswd.addTextChangedListener(new TextWatcher() {
            //填写过程中判断
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editpasswd.getText().toString().equals("")||editpasswd.getText()==null){
                    layoutpasswd.setError("密码还未填写");
                }else {
                    layoutpasswd.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editpasswd.getText().toString().equals("")||editpasswd.getText()==null){
                    layoutpasswd.setError("密码还未填写");
                }else {
                    layoutpasswd.setErrorEnabled(false);
                }
            }

        });
        layoutmain.setOnClickListener(new View.OnClickListener(){
            //无登录使用，蓝色框体的shared element动画变换
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WalkActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, v, "mainLayout");
                startActivity(intent, options.toBundle());
            }
        });
        layoutcenter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });
        btnregis.setOnClickListener(new View.OnClickListener(){
            //注册，注册按钮shared element动画
            @Override
            public void onClick(View v) {
                if(flag==true){
                    sh.save("");
                }
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RegisActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, v, "regisLayout");
                startActivity(intent, options.toBundle());
            }
        });
    }

}
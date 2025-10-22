package com.example.proj;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserActivity extends AppCompatActivity {
    private Context mContext;
    TextView nametext;
    private MyDBOpenHelper dbOpenHelper;
    LinearLayout sexlayout;
    LinearLayout birthlayout;
    TextView sextext;
    TextView birthtext;
    TextView pwdtext;
    TextView pwdtext2;
    TextView heighttext;
    TextView weighttext;
    TextInputLayout pwdlayout;
    TextInputLayout pwdlayout2;
    TextInputLayout heightlayout;
    TextInputLayout weightlayout;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    Button btnupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mContext = UserActivity.this;
        SharedHelper sh = new SharedHelper(getApplicationContext());
        nametext = (TextView) findViewById(R.id.usernametext);
        sexlayout = (LinearLayout) findViewById(R.id.usersexlayout);
        sextext = (TextView) findViewById(R.id.usersextext);
        birthlayout = (LinearLayout) findViewById(R.id.userbirthlayout);
        birthtext = (TextView) findViewById(R.id.userbirthtext);
        pwdlayout = (TextInputLayout) findViewById(R.id.userpasswdlayout);
        pwdtext = (TextView) findViewById(R.id.userpasswdtext);
        pwdlayout2 = (TextInputLayout) findViewById(R.id.userpasswdlayout2);
        pwdtext2 = (TextView) findViewById(R.id.userpasswdtext2);
        heightlayout = (TextInputLayout) findViewById(R.id.userheightlayout);
        heighttext = (TextView) findViewById(R.id.userheighttext);
        weightlayout = (TextInputLayout) findViewById(R.id.userweightlayout);
        weighttext = (TextView) findViewById(R.id.userweighttext);
        btnupdate = (Button) findViewById(R.id.userokbutton);
        nametext.setText(sh.readUsername() + "的个人信息");
        dbOpenHelper = new MyDBOpenHelper(getApplicationContext(), "my.db", null, 1);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("user", new String[]{"height", "weight", "sex", "birth"}, "uname=?", new String[]{sh.readUsername()}, null, null, null);
        cursor.moveToNext();
        heighttext.setText(cursor.getString(cursor.getColumnIndex("height")));
        weighttext.setText(cursor.getString(cursor.getColumnIndex("weight")));
        sextext.setText(cursor.getString(cursor.getColumnIndex("sex")));
        birthtext.setText(cursor.getString(cursor.getColumnIndex("birth")));
        btnupdate.setOnClickListener(new View.OnClickListener() {
            //修改前的最终检查
            @Override
            public void onClick(View view) {
                boolean flag;
                //密码有输入才会检查密码格式正确性
                if(!pwdtext.getText().toString().equals("")&&(pwdtext.getText().toString().length()<8||pwdtext.getText().toString().length()>16||!pwdtext.getText().toString().matches("^[a-z0-9A-Z]+$"))){
                    pwdlayout.setError("密码长度应当介于8到16位，且同时包含拉丁子母和数字");
                    flag=false;
                }else{
                    pwdlayout.setErrorEnabled(false);
                    flag=true;
                }
                if(!pwdtext.getText().toString().equals("")&&pwdtext2.getText().toString().equals("")){
                    pwdlayout2.setError("请再填写一次密码");
                    flag=false;
                }else{
                    if(!pwdtext2.getText().toString().equals(pwdtext.getText().toString())){
                        pwdlayout2.setError("两次填写的密码不一致");
                        flag=false;
                    }else {
                        pwdlayout2.setErrorEnabled(false);
                        flag=true;
                    }
                    pwdlayout.setErrorEnabled(false);
                }
                if(heighttext.getText().toString().equals("")){
                    heightlayout.setError("身高还未填写");
                    flag=false;
                }else{
                    heightlayout.setErrorEnabled(false);
                    flag=true;
                }
                if(weighttext.getText().toString().equals("")){
                    weightlayout.setError("体重还未填写");
                    flag=false;
                }else{
                    weightlayout.setErrorEnabled(false);
                    flag=true;
                }
                if(sextext.getText().toString().equals("")||sextext.getText().toString().equals("性别还未填写")){
                    sextext.setText("性别还未填写");
                    sextext.setTextColor(Color.parseColor("#FF7043"));
                    flag=false;
                }else{
                    sextext.setTextColor(Color.parseColor("#6C6C6C"));
                    flag=true;
                }
                if(birthtext.getText().toString().equals("")||birthtext.getText().toString().equals("生日还未填写")){
                    birthtext.setText("生日还未填写");
                    birthtext.setTextColor(Color.parseColor("#FF7043"));
                    flag=false;
                }else{
                    birthtext.setTextColor(Color.parseColor("#6C6C6C"));
                    flag=true;
                }
                if(flag==true) {
                    //更新数据库
                    ContentValues values = new ContentValues();
                    if(!pwdtext.getText().toString().equals("")) {
                        values.put("pwd", pwdtext.getText().toString());
                    }
                    values.put("height", heighttext.getText().toString());
                    values.put("weight", weighttext.getText().toString());
                    values.put("sex", sextext.getText().toString());
                    values.put("birth", birthtext.getText().toString());
                    SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
                    if(db.update("user", values,"uname=?",new String[]{sh.readUsername()})==-1){
                        Toast.makeText(UserActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                    }else {
                        //保存信息并跳转到WalkActivity
                        Toast.makeText(UserActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(UserActivity.this, WalkActivity.class);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(UserActivity.this, view, "mainLayout");
                        startActivity(intent, options.toBundle());
                    }
                }
            }
        });
        pwdtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!pwdtext.getText().toString().equals("")&&(pwdtext.getText().toString().length()<8||pwdtext.getText().toString().length()>16||!pwdtext.getText().toString().matches("^[a-z0-9A-Z]+$"))){
                    pwdlayout.setError("密码长度应当介于8到16位，且同时包含拉丁子母和数字");
                }else {
                    pwdlayout.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!pwdtext.getText().toString().equals("")&&(pwdtext.getText().toString().length()<8||pwdtext.getText().toString().length()>16||!pwdtext.getText().toString().matches("^[a-z0-9A-Z]+$"))){
                    pwdlayout.setError("密码长度应当介于8到16位，且同时包含拉丁子母和数字");
                }else {
                    pwdlayout.setErrorEnabled(false);
                }
            }
        });
        pwdtext2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!pwdtext.getText().toString().equals("")&&pwdtext2.getText().toString().equals("")){
                    pwdlayout2.setError("请再填写一次密码");
                }else {
                    if(!pwdtext2.getText().toString().equals(pwdtext.getText().toString())){
                        pwdlayout2.setError("两次填写的密码不一致");
                    }else {
                        pwdlayout2.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!pwdtext.getText().toString().equals("")&&pwdtext2.getText().toString().equals("")){
                    pwdlayout2.setError("请再填写一次密码");
                }else {
                    if(!pwdtext2.getText().toString().equals(pwdtext.getText().toString())){
                        pwdlayout2.setError("两次填写的密码不一致");
                    }else {
                        pwdlayout2.setErrorEnabled(false);
                    }
                }
            }
        });
        sexlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] sex = new String[]{"男", "女"};
                alert = null;
                builder = new AlertDialog.Builder(mContext);
                alert = builder.setTitle("性别")
                        .setItems(sex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sextext.setText(sex[which]);
                                sextext.setTextColor(Color.parseColor("#6C6C6C"));
                            }
                        }).create();
                alert.show();
            }
        });
        birthlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar=Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(mContext,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker arg0, int year, int month, int day) {
                        calendar.set(year, month, day);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        birthtext.setText(format.format(calendar.getTime()));
                        birthtext.setTextColor(Color.parseColor("#6C6C6C"));
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
        heighttext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(heighttext.getText().toString().equals("")||heighttext.getText()==null){
                    heightlayout.setError("身高还未填写");
                }else {
                    heightlayout.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(heighttext.getText().toString().equals("")||heighttext.getText()==null){
                    heightlayout.setError("身高还未填写");
                }else {
                    heightlayout.setErrorEnabled(false);
                }
            }
        });
        weighttext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(weighttext.getText().toString().equals("")||weighttext.getText()==null){
                    weightlayout.setError("体重还未填写");
                }else {
                    weightlayout.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(weighttext.getText().toString().equals("")||weighttext.getText()==null){
                    weightlayout.setError("体重还未填写");
                }else {
                    weightlayout.setErrorEnabled(false);
                }
            }
        });
    }
}
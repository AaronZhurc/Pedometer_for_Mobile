package com.example.proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RegisActivity extends AppCompatActivity {
    private Context mContext;
    Button btnregis;
    LinearLayout sexlayout;
    LinearLayout birthlayout;
    TextView sextext;
    TextView birthtext;
    TextView nametext;
    TextView pwdtext;
    TextView pwdtext2;
    TextView heighttext;
    TextView weighttext;
    TextInputLayout namelayout;
    TextInputLayout pwdlayout;
    TextInputLayout pwdlayout2;
    TextInputLayout heightlayout;
    TextInputLayout weightlayout;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private MyDBOpenHelper dbOpenHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regis);
        BaseApplication.remeberActivity(this);
        mContext = RegisActivity.this;
        btnregis = (Button)findViewById(R.id.regisokbutton);
        sexlayout=(LinearLayout)findViewById(R.id.regissexlayout);
        sextext=(TextView) findViewById(R.id.regissextext);
        birthlayout=(LinearLayout)findViewById(R.id.regisbirthlayout);
        birthtext=(TextView) findViewById(R.id.regisbirthtext);
        namelayout=(TextInputLayout)findViewById(R.id.regisnamelayout);
        nametext=(TextView) findViewById(R.id.regisnametext);
        pwdlayout=(TextInputLayout)findViewById(R.id.regispasswdlayout);
        pwdtext=(TextView) findViewById(R.id.regispasswdtext);
        pwdlayout2=(TextInputLayout)findViewById(R.id.regispasswdlayout2);
        pwdtext2=(TextView) findViewById(R.id.regispasswdtext2);
        heightlayout=(TextInputLayout)findViewById(R.id.regisheightlayout);
        heighttext=(TextView) findViewById(R.id.regisheighttext);
        weightlayout=(TextInputLayout)findViewById(R.id.regisweightlayout);
        weighttext=(TextView) findViewById(R.id.regisweighttext);
        dbOpenHelper=new MyDBOpenHelper(getApplicationContext(), "my.db", null, 1);
        btnregis.setOnClickListener(new View.OnClickListener(){
            //注册前的最终检查
            @Override
            public void onClick(View v) {
                boolean flag;
                if(nametext.getText().toString().equals("")){
                    namelayout.setError("用户名还未填写");
                    flag=false;
                }else if(nametext.getText().toString().length()>16){
                    namelayout.setError("用户名不得超过16字");
                    flag=false;
                }else{
                    //确保用户名的唯一性
                    SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
                    Cursor cursor=db.query("user", null, "uname=?", new String[]{nametext.getText().toString()}, null, null, null);
                    if (cursor.getCount()!=0){
                        namelayout.setError("已存在相同用户名");
                        flag=false;
                    }else {
                        namelayout.setErrorEnabled(false);
                        flag=true;
                    }
                }
                if(pwdtext.getText().toString().equals("")){
                    pwdlayout.setError("密码还未填写");
                    flag=false;
                }else if(pwdtext.getText().toString().length()<8||pwdtext.getText().toString().length()>16||!pwdtext.getText().toString().matches("^[a-z0-9A-Z]+$")){
                    pwdlayout.setError("密码长度应当介于8到16位，且只包含拉丁子母和数字");
                    flag=false;
                }else{
                    pwdlayout.setErrorEnabled(false);
                    flag=true;
                }
                if(pwdtext2.getText().toString().equals("")){
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
                    sextext.setTextColor(Color.parseColor("#FFFFFF"));
                    flag=true;
                }
                if(birthtext.getText().toString().equals("")||birthtext.getText().toString().equals("生日还未填写")){
                    birthtext.setText("生日还未填写");
                    birthtext.setTextColor(Color.parseColor("#FF7043"));
                    flag=false;
                }else{
                    birthtext.setTextColor(Color.parseColor("#FFFFFF"));
                    flag=true;
                }
                if(flag==true) {
                    //插入数据库
                    ContentValues values = new ContentValues();
                    values.put("uname", nametext.getText().toString());
                    values.put("pwd", pwdtext.getText().toString());
                    values.put("height", heighttext.getText().toString());
                    values.put("weight", weighttext.getText().toString());
                    values.put("sex", sextext.getText().toString());
                    values.put("birth", birthtext.getText().toString());
                    SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
                    if(db.insert("user", null, values)==-1){
                        Toast.makeText(RegisActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(RegisActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        SharedHelper sh=new SharedHelper(getApplicationContext());
                        sh.save(nametext.getText().toString());
                        Intent intent = new Intent();
                        intent.setClass(RegisActivity.this, WalkActivity.class);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(RegisActivity.this, v, "mainLayout");
                        startActivity(intent, options.toBundle());
                    }
                }
            }
        });
        sexlayout.setOnClickListener(new View.OnClickListener() {
            //AlertDialog以输入性别
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
                                sextext.setTextColor(Color.parseColor("#FFFFFF"));
                            }
                        }).create();
                alert.show();
            }
        });
        birthlayout.setOnClickListener(new View.OnClickListener() {
            //DatePickerDialog以输入生日
            @Override
            public void onClick(View view) {
                Calendar calendar=Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(mContext,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker arg0, int year, int month, int day) {
                        calendar.set(year, month, day);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        birthtext.setText(format.format(calendar.getTime()));
                        birthtext.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
        nametext.addTextChangedListener(new TextWatcher() {
            //输入时检查
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(nametext.getText().toString().equals("")||nametext.getText()==null){
                    namelayout.setError("用户名还未填写");
                }else if(nametext.getText().toString().length()>16){
                    namelayout.setError("用户名不得超过16字");
                }else {
                    namelayout.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(nametext.getText().toString().equals("")||nametext.getText()==null){
                    namelayout.setError("用户名还未填写");
                }else if(nametext.getText().toString().length()>16){
                    namelayout.setError("用户名不得超过16字");
                }else {
                    SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
                    Cursor cursor=db.query("user", null, "uname=?", new String[]{nametext.getText().toString()}, null, null, null);
                    if (cursor.getCount()!=0){
                        namelayout.setError("已存在相同用户名");
                    }else {
                        namelayout.setErrorEnabled(false);
                    }
                }
            }
        });
        pwdtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(pwdtext.getText().toString().equals("")||pwdtext.getText()==null){
                    pwdlayout.setError("密码还未填写");
                }else if(pwdtext.getText().toString().length()<8||pwdtext.getText().toString().length()>16||!pwdtext.getText().toString().matches("^[a-z0-9A-Z]+$")){
                    pwdlayout.setError("密码长度应当介于8到16位，且只包含拉丁子母和数字");
                }else {
                    pwdlayout.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(pwdtext.getText().toString().equals("")||pwdtext.getText()==null){
                    pwdlayout.setError("密码还未填写");
                }else if(pwdtext.getText().toString().length()<8||pwdtext.getText().toString().length()>16||!pwdtext.getText().toString().matches("^[a-z0-9A-Z]+$")){
                    pwdlayout.setError("密码长度应当介于8到16位，且只包含拉丁子母和数字");
                }else {
                    pwdlayout.setErrorEnabled(false);
                }
            }
        });
        pwdtext2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(pwdtext2.getText().toString().equals("")||pwdtext2.getText()==null){
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
                if(pwdtext2.getText().toString().equals("")||pwdtext2.getText()==null){
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
        pwdtext2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(pwdtext2.getText().toString().equals("")||pwdtext2.getText()==null){
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
                if(pwdtext2.getText().toString().equals("")||pwdtext2.getText()==null){
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
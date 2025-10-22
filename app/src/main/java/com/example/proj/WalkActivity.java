package com.example.proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WalkActivity extends AppCompatActivity implements KeyEvent.Callback {
    LinearLayout walklayout;
    LinearLayout sharelayout;
    LinearLayout exitlayout;
    LinearLayout viewlayout;
    LinearLayout userlayout;
    TextView usertitle;
    private boolean isBind;
    Intent walkintent;
    MyDBOpenHelper dbOpenHelper;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        //后台可以继续运行
        super.onDestroy();
        if(isBind){
            this.unbindService(serviceConnection);
        }
    }

    TextView usersubtitle;
    TextView steptext;
    TextView disttext;
    boolean loginflag=false;
    int countclick=0;
    String[] clickletter={"在健了在健了","摸了","好酷","遨游","恰","好气啊","笑了","我的超人"};
    private WalkService walkService;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);
        //清空ActivityList以确保按返回键不会回到登录页面
        BaseApplication.clearActivityList();
        BaseApplication.remeberActivity(this);
        walklayout=(LinearLayout)findViewById(R.id.walklayout);
        sharelayout=(LinearLayout)findViewById(R.id.walksharelayout);
        exitlayout=(LinearLayout)findViewById(R.id.walkexitlayout);
        userlayout=(LinearLayout)findViewById(R.id.walkuserlayout);
        viewlayout=(LinearLayout)findViewById(R.id.walkviewlayout);
        usertitle=(TextView) findViewById(R.id.walkusertitle);
        usersubtitle=(TextView)findViewById(R.id.walkusersubstitle);
        steptext=(TextView)findViewById(R.id.walksteptext);
        disttext=(TextView)findViewById(R.id.walkdisttext);
        dbOpenHelper=new MyDBOpenHelper(getApplicationContext(), "my.db", null, 1);
        SharedHelper sh=new SharedHelper(getApplicationContext());
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        //显示数据的初始化
        if(!sh.readUsername().equals("")) {
            Cursor cursor = db.query("user", new String[]{"uid"}, "uname=?", new String[]{sh.readUsername()}, null, null, null);
            cursor.moveToNext();
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            cursor=db.query("walk",new String[]{"step","distance"},"uid=? and date=?",new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex("uid"))),format.format(date)},null,null,null);
            if(cursor.getCount()!=0) {
                cursor.moveToNext();
                steptext.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("step"))));
                disttext.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("distance"))));
            }else{
                Map<String,Integer> data=sh.getDataWalk();
                steptext.setText(String.valueOf(data.get("step")));
                disttext.setText(String.valueOf(data.get("dist")));
            }
            cursor.close();
        }else{
            Map<String,Integer> data=sh.getDataWalk();
            steptext.setText(String.valueOf(data.get("step")));
            disttext.setText(String.valueOf(data.get("dist")));
        }
        //调用WalkService
        walkintent=new Intent(WalkActivity.this,WalkService.class);
        isBind=bindService(walkintent,serviceConnection, Context.BIND_AUTO_CREATE);
        startService(walkintent);
        //登录/免登录判断
        if(sh.readUsername().equals("")){
            //免登录，个人信息按钮文字变化和查看数据、退出登录按钮隐藏
            usertitle.setText("返回登陆");
            usersubtitle.setText("返回登录界面登录或注册以保存信息");
            exitlayout.setVisibility(View.GONE);
            viewlayout.setVisibility(View.GONE);
            loginflag=false;
        }else{
            //个人信息按钮文字变化和查看数据、退出登录按钮显示
            usertitle.setText("个人信息");
            usersubtitle.setText("管理个人信息");
            exitlayout.setVisibility(View.VISIBLE);
            viewlayout.setVisibility(View.VISIBLE);
            loginflag=true;
            //发送广播给WalkService接收以确保数据即时初始化
            sendBroadcast(new Intent("com.example.proj.USER_LOGIN"));
        }
        walklayout.setOnClickListener(new View.OnClickListener() {
            //彩蛋
            @Override
            public void onClick(View view) {
                countclick++;
                if(countclick>19){
                    Toast.makeText(getApplicationContext(), clickletter[new Random().nextInt(clickletter.length)],Toast.LENGTH_SHORT).show();
                }
            }
        });
        sharelayout.setOnClickListener(new View.OnClickListener() {
            //分享数据
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "我行走了"+steptext.getText().toString()+"步，合计"+disttext.getText().toString()+"米");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "分享到"));
            }
        });
        exitlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //发送广播给WalkService接收以确保用户登出时信息保存
                sendBroadcast(new Intent("com.example.proj.USER_EXIT"));
                intent.setClass(WalkActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        userlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginflag==false){
                    //未登录，返回登录
                    sh.clear();
                    Intent intent = new Intent();
                    intent.setClass(WalkActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //登录，进入UserActivity，个人信息
                    Intent intent = new Intent();
                    intent.setClass(WalkActivity.this, UserActivity.class);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(WalkActivity.this, view, "userLayout");
                    startActivity(intent, options.toBundle());
                }
            }
        });
        viewlayout.setOnClickListener(new View.OnClickListener() {
            //进入ViewActivity，查看数据
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(WalkActivity.this, ViewActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(WalkActivity.this, view, "viewLayout");
                startActivity(intent, options.toBundle());
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //返回按钮直接返回到桌面
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            //Handler获取WalkService传递的数据
            if(msg.what==1){
                steptext.setText(String.valueOf(msg.arg1));
                Log.e("walkactivity","get"+msg.arg1+" "+msg.arg2);
                disttext.setText(String.valueOf(msg.arg2));
                SharedHelper sh=new SharedHelper(getApplicationContext());
                sh.setDataWalk(msg.arg1,msg.arg2);
            }
        }
    };
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //ServiceConnection绑定WalkService
            WalkService.WalkBinder walkBinder=(WalkService.WalkBinder) iBinder;
            walkService=walkBinder.getService();
            walkService.registerCallback(new UpdateUICallback() {
                @Override
                public void updateUI(int step,double dist) {
                    Message message=Message.obtain();
                    message.what=1;
                    message.arg1=step;
                    message.arg2= (int) dist;
                    handler.sendMessage(message);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    };

}
package com.example.proj;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewActivity extends AppCompatActivity {
    private MyDBOpenHelper dbOpenHelper;
    private ArrayList<Map<String,String>> mData = null;
    private BaseAdapter mAdapter = null;
    private GridView viewlayout;
    private TextView nametext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        viewlayout=(GridView)findViewById(R.id.viewlayout);
        nametext = (TextView) findViewById(R.id.viewnametext);
        dbOpenHelper=new MyDBOpenHelper(getApplicationContext(), "my.db", null, 1);
        SharedHelper sh=new SharedHelper(getApplicationContext());
        nametext.setText(sh.readUsername() + "的运动数据");
        //从数据库读数据
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("user", new String[]{"uid"}, "uname=?", new String[]{sh.readUsername()}, null, null, null);
        cursor.moveToNext();
        cursor=db.query("walk",new String[]{"step","distance","date"},"uid=?",new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex("uid")))},null,null,null);
        mData=new ArrayList<Map<String,String>>();
        while(cursor.moveToNext()){
            Map<String,String> map=new HashMap<String,String>();
            map.put("step",String.valueOf(cursor.getInt(cursor.getColumnIndex("step"))));
            map.put("dist",String.valueOf(cursor.getInt(cursor.getColumnIndex("distance"))));
            map.put("date",cursor.getString(cursor.getColumnIndex("date")));
            mData.add(map);
        }
        //使用Adapter构建GridView
        mAdapter = new MyAdapter<Map<String,String>>(mData, R.layout.list_item) {
            @Override
            public void bindView(ViewHolder holder, Map<String,String> obj) {
                holder.setText(R.id.txt_item, "步数"+obj.get("step")+" 距离"+obj.get("dist"));
                holder.setText(R.id.txt_date, obj.get("date"));
            }
        };
        viewlayout.setAdapter(mAdapter);
        viewlayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }
}
package com.example.proj;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedHelper {
    private Context mContext;

    public SharedHelper() {
    }

    public SharedHelper(Context mContext) {
        this.mContext = mContext;
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("step", 0);
        editor.putInt("dist", 0);
        editor.putBoolean("last", false);
        editor.commit();
    }


    public void save(String username) {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", username);
        editor.commit();
    }

    public String readUsername() {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        return sp.getString("username", "");
    }

    public void clear() {
        SharedPreferences sp = mContext.getSharedPreferences("mysp", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }
    public void setAuto(boolean a){
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(a==true){
            editor.putBoolean("auto", true);
        }else{
            editor.putBoolean("auto",false);
        }
        editor.commit();
    }
    public boolean getAuto(){
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        if(sp.getBoolean("auto", Boolean.parseBoolean(""))==true){
            return true;
        }else{
            return false;
        }
    }
    public void setDataWalk(int step,int dist){
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("step", step);
        editor.putInt("dist", dist);
        editor.commit();
    }
    public Map<String, Integer> getDataWalk(){
        Map<String, Integer> data = new HashMap<String, Integer>();
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        int a=0,b=0;
        data.put("step", sp.getInt("step",a));
        data.put("dist", sp.getInt("dist", b));
        return data;
    }
    public void initWalk(){
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("step", 0);
        editor.putInt("dist", 0);
        editor.commit();
    }
    public void setLast(boolean last){
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("last", last);
        editor.commit();
    }
    public boolean getLast(){
        SharedPreferences sp = mContext.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        return sp.getBoolean("auto", Boolean.parseBoolean(""));
    }
}

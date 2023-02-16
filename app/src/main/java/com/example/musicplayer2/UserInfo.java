package com.example.musicplayer2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class UserInfo extends AppCompatActivity {

    TextView username;
    TextView telenum;
    TextView joinnum;
    Mysql mysql;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTitle("关于个人");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_user); // 设置该Activity使用的布局
        String name = getIntent().getStringExtra("name");//读取当前登录的用户名
        String telenumber = null;
        String id = null;
        String regtime = null;
        mysql = new Mysql(this,"Userinfo",null,1);      //建数据库或者取数据库
        db = mysql.getReadableDatabase();
        username = findViewById(R.id.textview);
        telenum = findViewById(R.id.textView2);
        joinnum = findViewById(R.id.textView3);
        Cursor cursor = db.query("logins",new String[]{"phonum","id","regtime"}," usname=?",new String[]{name},null,null,null);
        cursor.moveToFirst();
        telenumber = cursor.getString(0);
        id = cursor.getString(1);
        regtime = cursor.getString(2);
        username.setText("用户名: \n"+ name);
        telenum.setText("账号: " + telenumber);
        joinnum.setText("您的账号注册于"+ regtime+ "\n是我们的第" +id+"位用户");
        db = mysql.getReadableDatabase();

}

}
package com.example.musicplayer2;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;

public class registerActivity extends AppCompatActivity {
    EditText usename,usepwd,usepwd2,phonenumber;
    Button submit;
    Mysql mysql;
    SQLiteDatabase db;
    SharedPreferences sp;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register); // 设置该Activity使用的布局
        usename = this.findViewById(R.id.usename);			    //用户名编辑框
        usepwd =  this.findViewById(R.id.usepwd);				//设置初始密码编辑框
        usepwd2 = this.findViewById(R.id.usepwd2);			    //二次输入密码编辑框
        phonenumber = this.findViewById(R.id.phonenum);         //电话号码编辑框
        submit =   this.findViewById(R.id.submit);				//注册按钮
        mysql = new Mysql(this,"Userinfo",null,1);      //建数据库
        db = mysql.getReadableDatabase();
        sp = this.getSharedPreferences("useinfo",this.MODE_PRIVATE);


        submit.setOnClickListener(new View.OnClickListener() {
            boolean flag = true;            //判断用户是否已存在的标志位
            @Override
            public void onClick(View v) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");//获取当前时间
                Long currTime = System.currentTimeMillis();//设置日期显示格式并转换成字符串格式
                String regTimeStr = formatter.format(currTime); //注册时间
                String name = usename.getText().toString();				//用户名
                String pwd01 = usepwd.getText().toString();				//密码
                String pwd02 = usepwd2.getText().toString();			//二次输入的密码
                String phonum = phonenumber.getText().toString(); //输入的电话号码
                String sex = "";										//性别
                if(name.equals("")||pwd01 .equals("")||pwd02.equals("")){
                    Toast.makeText(registerActivity.this, "用户名或密码不能为空!！", Toast.LENGTH_LONG).show();
                }
                else{
                    Cursor cursor = db.query("logins",new String[]{"usname"},null,null,null,null,null);

                    while (cursor.moveToNext()){
                        if(cursor.getString(0).equals(name)){
                            flag = false;
                            break;
                        }
                    }
                    if(flag==true){                                             //判断用户是否已存在
                        if (pwd01.equals(pwd02)) {								//判断两次输入的密码是否一致，若一致则继续，不一致则提醒密码不一致
                            ContentValues cv = new ContentValues();
                            cv.put("usname",name);
                            cv.put("uspwd",pwd01);
                            cv.put("phonum",phonum);
                            cv.put("regtime",regTimeStr);
                            db.insert("logins",null,cv);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("usname",name);
                            editor.putString("uspwd",pwd01);
                            editor.putString("phonum",phonum);
                            editor.putString("regtime",regTimeStr);
                            editor.commit();
                            Intent intent = new Intent();
                            intent.setClass(registerActivity.this,loginActivity.class);      //跳转到登录页面
                            startActivity(intent);
                            Toast.makeText(registerActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(registerActivity.this, "密码不一致！", Toast.LENGTH_LONG).show();			//提示密码不一致
                        }
                    }
                    else{
                        Toast.makeText(registerActivity.this, "用户已存在！", Toast.LENGTH_LONG).show();			//提示密码不一致
                    }

                }
            }


        });


    }
    private class MyRadioButtonListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            RadioButton r = (RadioButton) findViewById(checkedId);//获取被选中的Id
            Log.i("单选按钮监听", "选择性别为：" + r.getText().toString());
        }
    }
}

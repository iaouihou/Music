package com.example.musicplayer2;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Songlist extends AppCompatActivity implements View.OnClickListener{


    private SearchView musicSearch;
    private Timer timer;
    private SeekBar seekBar;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    ImageView nextIv,playIv,lastIv,albumIv,playwayIv,ilikeIv;
    TextView singerTv,songTv,musicLength,musicCur;
    RecyclerView musicRv;
    List<LocalMusicBean>mDatas;
    private LocalMusicAdapter adapter;
    public static List<Sheet> setSheet;
    public static List<LocalMusicBean> list;
    public static Sheet sheet;

    //    记录当前正在播放的音乐的位置
    int currentPlayPosition = -1;
    //    记录暂停音乐时进度条的位置
    int currentPausePositionInSong = 0;
    //0是列表循环，1是单曲循环，2是随机播放
    int currentplayway = 0;
    MediaPlayer mediaPlayer;
    SimpleDateFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTitle("本地音乐");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_song);
        initView();
        setSheet = new ArrayList<>();
        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        format = new SimpleDateFormat("mm:ss");
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();
        list = new ArrayList<>();
//     创建适配器对象
        adapter = new LocalMusicAdapter(this,list);
        musicRv.setAdapter(adapter);
//        设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
//        加载本地数据源
        loadLocalMusicData();
//        设置每一项的点击事件
        setEventListener();

        setSearchList();



    }

    private void setEventListener() {
        /* 设置每一项的点击事件*/
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                currentPlayPosition = position;
                LocalMusicBean musicBean = list.get(position);
                singerTv.setText(musicBean.getSinger());
                songTv.setText(musicBean.getSong());
                musicCur.setText("00:00");
                musicLength.setText(musicBean.getDuration());
                if(loveflag.lovetag[position]==0)
                    ilikeIv.setImageResource(R.drawable.heart);
                if(loveflag.lovetag[position]==1)
                    ilikeIv.setImageResource(R.drawable.redheart);
                stopMusic();
                //重置多媒体播放器
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(musicBean.getPath());
                    //监听播放时回调函数
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        Runnable updateUI = new Runnable() {
                            @Override
                            public void run() {
                                musicCur.setText(format.format(mediaPlayer.getCurrentPosition()) + "");
                            }
                        };
                            @Override
                            public void run() {
                                if (!isSeekBarChanging) {
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    runOnUiThread(updateUI);
                                }
                            }
                            }, 0, 50);
                    playMusic();
                    seekBar.setMax(mediaPlayer.getDuration());
                    autoplay();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });
    }
public void autoplay()
{
    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer arg0) {
            if(currentplayway==0) {
                currentPlayPosition = currentPlayPosition + 1;
                if(currentPlayPosition==mDatas.size())
                    currentPlayPosition=0;
            }
            if(currentplayway==1)
                currentPlayPosition=currentPlayPosition;
            if(currentplayway==2)
                currentPlayPosition=(int)(Math.random()*mDatas.size());
            LocalMusicBean nextBean0 = mDatas.get(currentPlayPosition);
            singerTv.setText(nextBean0.getSinger());
            songTv.setText(nextBean0.getSong());
            musicCur.setText("00:00");
            musicLength.setText(nextBean0.getDuration());
            stopMusic();
            mediaPlayer.reset();
            try{
                mediaPlayer.setDataSource(nextBean0.getPath());
                playMusic();
                seekBar.setMax(mediaPlayer.getDuration());//获取当前播放的音乐的长度
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    });
    // 因为直接切歌会发生错误，所以增加错误监听器。返回true。就不会回调onCompletion方法了。
    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            return true;
        }
    });
}

    public void playMusicInMusicBean(LocalMusicBean musicBean) {
        /*根据传入对象播放音乐*/
        //设置底部显示的歌手名称和歌曲名
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
        stopMusic();
//                重置多媒体播放器
        mediaPlayer.reset();
//                设置新的播放路径
        try {
            mediaPlayer.setDataSource(musicBean.getPath());
            String albumArt = musicBean.getAlbumArt();
            Log.i("lsh123", "playMusicInMusicBean: albumpath=="+albumArt);
            Bitmap bm = BitmapFactory.decodeFile(albumArt);
            Log.i("lsh123", "playMusicInMusicBean: bm=="+bm);
            albumIv.setImageBitmap(bm);
            playMusic();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 点击播放按钮播放音乐，或者暂停从新播放
     * 播放音乐有两种情况：
     * 1.从暂停到播放
     * 2.从停止到播放
     * */

    private void playMusic() {
        /* 播放音乐的函数*/
        if (mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
//                从暂停到播放
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }
            playIv.setImageResource(R.drawable.icon_pause);
        }
    }

    private void pauseMusic() {
        /* 暂停音乐的函数*/
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.drawable.icon_play);
        }
    }
    private void stopMusic() {
        /* 停止音乐的函数*/
        if (mediaPlayer!=null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.drawable.icon_play);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void loadLocalMusicData() {
        /* 加载本地存储当中的音乐mp3文件到集合当中*/
//        1.获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
//        2.获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        3 开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
//        4.遍历Cursor
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time = sdf.format(new Date(duration));
//          获取专辑图片主要是通过album_id进行查询
            String album_id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            String albumArt = getAlbumArt(album_id);
            if(time.equals("00:00")){
                //Log.d("MainActivity.this","没法运行到这的哦");
                id--;
                continue;
            }else {
//            将一行当中的数据封装到对象当中
                LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path, albumArt,false);
                list.add(bean);
                mDatas.add(bean);

            }
        }
//        数据源变化，提示适配器更新
        adapter.notifyDataSetChanged();
    }


    private String getAlbumArt(String album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = this.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + album_id),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    private void initView(){
        nextIv=findViewById(R.id.local_music_bottom_iv_next);
        playIv=findViewById(R.id.local_music_bottom_iv_play);
        lastIv=findViewById(R.id.local_music_bottom_iv_last);
        singerTv=findViewById(R.id.local_music_bottom_tv_singer);
        songTv=findViewById(R.id.local_music_bottom_tv_song);
        albumIv=findViewById(R.id.local_music_bottom_iv_icon);
        musicRv=findViewById(R.id.local_music_rv);
        playwayIv=findViewById(R.id.play_way_btn);
        ilikeIv=findViewById(R.id.ilike);
        musicLength = (TextView) findViewById(R.id.music_length);
        musicCur = (TextView) findViewById(R.id.music_cur);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        musicSearch = findViewById(R.id.music_search);

        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        albumIv.setOnClickListener(this);
        playwayIv.setOnClickListener(this);
        ilikeIv.setOnClickListener(this);

    }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.local_music_bottom_iv_last:
                    int n=mDatas.size();
                if (currentPlayPosition ==0&&currentplayway==0) {
                    Toast.makeText(this,"已经是第一首了，没有上一曲！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentPlayPosition ==-1) {
                    Toast.makeText(this,"没有选中歌曲，无法播放上一首",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentplayway==1)
                {
                    currentPlayPosition = currentPlayPosition;
                }
                else if(currentplayway==2)
                {
                    currentPlayPosition = (int)(Math.random()*mDatas.size());
                }
                else if(currentplayway==0){
                    currentPlayPosition = currentPlayPosition-1;
                }
                LocalMusicBean lastBean = mDatas.get(currentPlayPosition);
                //playMusicInMusicBean(lastBean);
                    if(loveflag.lovetag[currentPlayPosition]==0)
                        ilikeIv.setImageResource(R.drawable.heart);
                    if(loveflag.lovetag[currentPlayPosition]==1)
                        ilikeIv.setImageResource(R.drawable.redheart);
                    singerTv.setText(lastBean.getSinger());
                    songTv.setText(lastBean.getSong());
                    musicCur.setText("00:00");
                    musicLength.setText(lastBean.getDuration());
                    stopMusic();
                    //重置多媒体播放器
                    mediaPlayer.reset();
                    try{
                        mediaPlayer.setDataSource(lastBean.getPath());
                        playMusic();
                        seekBar.setMax(mediaPlayer.getDuration());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                case R.id.local_music_bottom_iv_next:
                if (currentPlayPosition ==mDatas.size()-1&&currentplayway==0) {
                    Toast.makeText(this,"已经是最后一首了，正在为您播放第一首！",Toast.LENGTH_SHORT).show();
                    //return;
                    currentPlayPosition=-1;
                }
                    else if(currentplayway==1)
                    {
                        currentPlayPosition = currentPlayPosition;
                    }
                    else if(currentplayway==2)
                    {
                        currentPlayPosition = (int)(Math.random()*mDatas.size());
                    }
                    else if(currentplayway==0){
                    currentPlayPosition = currentPlayPosition+1;
                }

                LocalMusicBean nextBean = mDatas.get(currentPlayPosition);
                //playMusicInMusicBean(nextBean);
                    if(loveflag.lovetag[currentPlayPosition]==0)
                        ilikeIv.setImageResource(R.drawable.heart);
                    if(loveflag.lovetag[currentPlayPosition]==1)
                        ilikeIv.setImageResource(R.drawable.redheart);
                    singerTv.setText(nextBean.getSinger());
                    songTv.setText(nextBean.getSong());
                    musicCur.setText("00:00");
                    musicLength.setText(nextBean.getDuration());
                    stopMusic();
                    //重置多媒体播放器
                    mediaPlayer.reset();
                    try{
                        mediaPlayer.setDataSource(nextBean.getPath());
                        playMusic();
                        seekBar.setMax(mediaPlayer.getDuration());//获取当前播放的音乐的长度
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                case R.id.local_music_bottom_iv_play:
                    if (currentPlayPosition == -1) {
//                    并没有选中要播放的音乐
                        Toast.makeText(this, "请选择想要播放的音乐", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mediaPlayer.isPlaying()) {
//                    此时处于播放状态，需要暂停音乐
                        pauseMusic();
                    } else {
//                    此时没有播放音乐，点击开始播放音乐
                        playMusic();
                    }
                    break;
                case R.id.local_music_bottom_iv_icon:
                    Intent intent = new Intent();
                    intent.setClass(Songlist.this,playinterface.class);
                    startActivity(intent);
                    break;
                case R.id.play_way_btn:
                    if(currentplayway==0)
                    {
                        playwayIv.setImageResource(R.drawable.xunhuanbofang);
                        currentplayway=1;
                    }
                    else if(currentplayway==1)
                    {
                        playwayIv.setImageResource(R.drawable.suijibofang);
                        currentplayway=2;
                    }
                    else if(currentplayway==2)
                    {
                        playwayIv.setImageResource(R.drawable.bofang);
                        currentplayway=0;
                    }
                    break;
                case R.id.ilike:
                    //LocalMusicBean Beanlike = mDatas.get(currentPlayPosition);
                    if(loveflag.lovetag[currentPlayPosition]==0) {
                        ilikeIv.setImageResource(R.drawable.redheart);
                        //Beanlike.setLove(true);
                        loveflag.lovetag[currentPlayPosition]=1;
                    }
                    else if(loveflag.lovetag[currentPlayPosition]==1) {
                        ilikeIv.setImageResource(R.drawable.heart);
                        //Beanlike.setLove(false);
                        loveflag.lovetag[currentPlayPosition]=0;
                    }


            }
        }
    public String capitalize(String string) {//搜索 首字母大写
        String finalstr="";
        String[] a=   string.split(" ");
        for(int i=0;i<a.length;i++){
            a[i]=a[i].substring(0, 1).toUpperCase()+a[i].substring(1);
            finalstr=finalstr+a[i]+" ";
        }
        finalstr=finalstr.substring(0, finalstr.length()-1);
        return finalstr;
    }
    public String capitalize2(String string) {//搜索 和上面反过来
        String finalstr="";
        String[] a=   string.split(" ");
        for(int i=0;i<a.length;i++){
            a[i]=a[i].substring(0, 1)+a[i].substring(1).toLowerCase();
            finalstr=finalstr+a[i]+" ";
        }
        finalstr=finalstr.substring(0, finalstr.length()-1);
        return finalstr;
    }
    private void setSearchList() {
        //设置SearchView默认是否自动缩小为图标
        musicSearch.setIconifiedByDefault(true);
        musicSearch.setFocusable(false);
        //设置搜索框监听器
        musicSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //点击搜索按钮时激发
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {


                //输入时激发
                if(TextUtils.isEmpty(s)){
                    //没有过滤条件内容
                    list.clear();
                    list.addAll(mDatas);
                    adapter.notifyDataSetChanged();
                } else {
                    //根据输入内容对RecycleView搜索
                    list.clear();
                    for (LocalMusicBean bean:mDatas){
                        if(bean.getSong().contains( capitalize(s))|bean.getSong().contains( capitalize2(s))|bean.getSong().contains(s.toLowerCase()) | bean.getSong().contains(s.toUpperCase()) |bean.getSong().contains(s)) {
                            list.add(bean);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });

    }



    /*进度条处理*/
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }
        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());
        }
    }

}

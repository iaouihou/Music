package com.example.musicplayer2;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//歌单表
public class MusicListSheet implements Serializable {
    private static MusicListSheet musicListSheet;
    private List<Sheet> sheet;
    private MusicListSheet(Context context){
        sheet=new ArrayList<>();
        Sheet listAllMusic=new Sheet("全部音乐");
        //listAllMusic.setList(com.example.musicplayer2.MusicUtils.getMusicData(context));
        Sheet listHistory=new Sheet("播放历史",1);
        //Sheet listRecommendation=new Sheet("推荐歌单",2);
        sheet.add(0,listAllMusic);
        sheet.add(1,listHistory);
        //  sheet.add(2,listRecommendation);


    }

    public static MusicListSheet get(Context context){
        if(musicListSheet ==null){
            musicListSheet =new MusicListSheet(context);
        }
        return musicListSheet;
    }

    public static boolean set(Object obj){
        if(obj instanceof MusicListSheet) {
            musicListSheet = (MusicListSheet) obj;
            return true;
        }
        return false;
    }

    public void add(Sheet musicList){
        sheet.add(musicList);
    }

    public List<Sheet> getSheet(){
        return sheet;
    }

    public int size(){
        return sheet.size();
    }


}
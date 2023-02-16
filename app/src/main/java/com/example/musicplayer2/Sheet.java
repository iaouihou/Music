package com.example.musicplayer2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Sheet implements Serializable {
    private String name;
    private List<com.example.musicplayer2.LocalMusicBean> list;

    public Sheet() {
        name="New List";
        list=new ArrayList<>();
    }
    public Sheet(String name){
        this.name=name;
        list=new ArrayList<>();
    }

    public Sheet(String name, int type){
        this.name=name;
        switch(type){
            case 0:
                list=new ArrayList<>();
                break;
            case 1:
                list=new LinkedList<>();
                break;
            default:break;
        }
    }

    public String getName() {
        return name;
    }

    public List<com.example.musicplayer2.LocalMusicBean> getList() {
        return list;
    }

    public void setList(List<com.example.musicplayer2.LocalMusicBean> musicList){
        list=musicList;
    }

    public void add(com.example.musicplayer2.LocalMusicBean music){
        list.add(music);
    }

    public void remove(com.example.musicplayer2.LocalMusicBean music){
        list.remove(music);
    }

    public String getNumString(){
        if(list!=null) {
            return " " + list.size() + " 首音乐";
        }
        return " " + 0 + " 首音乐";
    }


    public boolean contains(com.example.musicplayer2.LocalMusicBean song){

        return list.contains(song);
    }

    public void addFirst(com.example.musicplayer2.LocalMusicBean music){
        if(list instanceof LinkedList) {
            ((LinkedList<com.example.musicplayer2.LocalMusicBean>) list).addFirst(music);
        }
    }
}

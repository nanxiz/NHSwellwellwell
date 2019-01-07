package com.example.jakesetton.myfirstapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater inflater;

    public String[] titleArray = {"Hello","What is WellWellWell?","Privacy"};
    public String[] discriptionArray = {"...","......","........."};

    public SliderAdapter(Context context){
        this.context= context;


    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view==o);
    }

    @Override
    public int getCount() {
        return titleArray.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        //View view = inflater.inflate()
        return super.instantiateItem(container, position);
    }
}

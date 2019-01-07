package com.example.jakesetton.myfirstapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** * Created by Supriya on 9/11/2016. */
public class ViewPage1 extends Fragment {
    @Nullable    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.welcome_page_fragment_1,null);
        return v;
    }
}
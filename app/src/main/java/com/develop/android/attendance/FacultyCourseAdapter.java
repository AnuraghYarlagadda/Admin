package com.develop.android.attendance;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class FacultyCourseAdapter extends ArrayAdapter<Courses> {
    private Context mContext;

    public FacultyCourseAdapter(Context context, int resource, List<Courses> objects) {
        super(context, resource, objects);
        this.mContext = context;
    }
}
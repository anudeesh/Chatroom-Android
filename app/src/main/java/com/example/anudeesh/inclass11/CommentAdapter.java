package com.example.anudeesh.inclass11;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Anudeesh on 12/6/2016.
 */
public class CommentAdapter extends ArrayAdapter<Message> {
    List<Message> mData;
    Context mContext;
    int mResource;
    String uname;

    public CommentAdapter(Context context, int resource, List<Message> objects, String uname) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mData = objects;
        this.uname = uname;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }
        Message c = mData.get(position);

        TextView n = (TextView) convertView.findViewById(R.id.textViewComUname);
        TextView t = (TextView) convertView.findViewById(R.id.textViewComTime);
        TextView m = (TextView) convertView.findViewById(R.id.textViewComVal);

        String na[] = uname.split(" ");
        //Log.d("commentadapter",n[0]);
        n.setText(na[0] + " " + na[1]);
        PrettyTime prettyTime = new PrettyTime();
        String d = prettyTime.format(new Date(c.getTime()));
        t.setText(d);
        m.setText(c.getText());
        return convertView;
    }
}

package com.example.pizzasplit;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by a on 2/7/15.
 */
public class PizzaItemAdapter extends BaseAdapter {

    List<PizzaItem> list;
    Context mCtx;

    String mId;


    public PizzaItemAdapter(List<PizzaItem> list, Context ctx, String userId){
        this.list = list;
        mCtx = ctx;
        mId = userId;
    }



    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pizza_item, viewGroup, false);

        }



        TextView brand = (TextView) convertView.findViewById(R.id.brand);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        TextView time = (TextView) convertView.findViewById(R.id.time);


        PizzaItem item = (PizzaItem)getItem(i);
        brand.setText(item.brand);
        type.setText(item.type);
        time.setText(item.time + " min left");

        if(item.userId.equals(mId)){
            brand.setTextColor(Color.LTGRAY);
            type.setTextColor(Color.LTGRAY);
            time.setTextColor(Color.LTGRAY);
        }
        else {
            brand.setTextColor(Color.BLACK);
            type.setTextColor(Color.BLACK);
            time.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    public void update(List<PizzaItem> list){
        this.list = list;
        notifyDataSetChanged();
    }
}

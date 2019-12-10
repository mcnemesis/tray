package com.nuchwezi.tray;

/**
 * Created by AK1N Nemesis Fixx on 3/6/2018.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class TrayAdapter extends ArrayAdapter<Cell> {

    private final Activity context;
    private ArrayList<Cell> items;
    private final  int[] icons = {
            R.drawable.star_1,
            R.drawable.star_2,
            R.drawable.star_3,
            R.drawable.star_4,
            R.drawable.star_5
    };


    public TrayAdapter(Activity context, ArrayList<Cell>  cells) {
        super(context, R.layout.cell_entry_preview, cells);

        this.context=context;
        items = cells;
    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.cell_entry_preview, null,true);

        TextView txtTitle = rowView.findViewById(R.id.txtTitle);
        ImageView imageView =  rowView.findViewById(R.id.imgIcon);
        rowView.setTag(items.get(position));

        Random random = new Random();

        txtTitle.setText(items.get(position).getItem());
        imageView.setImageResource(icons[random.nextInt(icons.length)]);

        return rowView;

    }
}
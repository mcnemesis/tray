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
import java.util.Date;
import java.util.Random;

import io.noties.markwon.Markwon;

public class TrayAdapter extends ArrayAdapter<Cell> {

    private final Activity context;
    private ArrayList<Cell> items;
    private final  int[] icons = {
            R.drawable.symbol_1,
            R.drawable.symbol_2,
            R.drawable.symbol_3,
            R.drawable.symbol_4,
            R.drawable.symbol_5
    };

    // obtain an instance of Markwon
    final Markwon markwon;

    public TrayAdapter(Activity context, ArrayList<Cell>  cells) {
        super(context, R.layout.cell_entry_preview, cells);

        this.context=context;
        items = cells;

        markwon = Markwon.create(context);
    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.cell_entry_preview, null,true);

        TextView txtTitle = rowView.findViewById(R.id.txtTitle);
        TextView txtAge = rowView.findViewById(R.id.txtAge);
        ImageView imageView =  rowView.findViewById(R.id.imgIcon);
        rowView.setTag(items.get(position));

        Random random = new Random();

        Date moment = items.get(position).getMoment();
        txtAge.setText(String.format("%s ago.\nsince %s", Utility.computeAge(moment),Utility.humaneDate(moment,true)));

        //txtTitle.setText(items.get(position).getItem());
        // let's support markdown
        String text = items.get(position).getItem();
        // found that typical user will use single \n in text, but markdown somewhat doesn't respect that,
        // so we alter text by default to turn single \n to \n\n
        text = text.replaceAll("\n", "\n\n");
        markwon.setMarkdown(txtTitle,text);

        imageView.setImageResource(icons[random.nextInt(icons.length)]);

        return rowView;

    }
}
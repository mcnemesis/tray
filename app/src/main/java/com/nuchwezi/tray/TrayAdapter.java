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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;

public class TrayAdapter extends ArrayAdapter<Cell> {

    protected enum EggRenderStyle {
        NORMAL_MICRO,
        NORMAL_SMALL,
        NORMAL_LARGE,
        NORMAL_DEFAULT
    }

    private EggRenderStyle DEFAULT_EGG_RENDER_STYLE = EggRenderStyle.NORMAL_DEFAULT;
    protected EggRenderStyle activeEggRenderStyle = DEFAULT_EGG_RENDER_STYLE; // clients can modify..
    public EggRenderStyle getActiveEggRenderStyle(){
        return activeEggRenderStyle;
    }

    public void setActiveEggRenderStyle(EggRenderStyle activeEggRenderStyle) {
        this.activeEggRenderStyle = activeEggRenderStyle;
    }

    private final Activity context;
    private ArrayList<Cell> items;
    private final  int[] icons = {
            R.drawable.symbol_1,
            R.drawable.symbol_2,
            R.drawable.symbol_3,
            R.drawable.symbol_4,
            R.drawable.symbol_5,
            R.drawable.symbol_6,
            R.drawable.symbol_7,
            R.drawable.symbol_8,
            R.drawable.symbol_9
    };

    // obtain an instance of Markwon
    final Markwon markwon;

    public TrayAdapter(Activity context, ArrayList<Cell> cells, EggRenderStyle appliedEggRenderStyle) {
        super(context, R.layout.cell_entry_preview, cells);

        this.context=context;
        items = cells;

        markwon = Markwon.builder(context)
                .usePlugin(HtmlPlugin.create())
                .build();

        activeEggRenderStyle = appliedEggRenderStyle;
    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();

        int appliedEggLayout;
        switch (activeEggRenderStyle){
            case NORMAL_LARGE:{
                appliedEggLayout = R.layout.cell_entry_preview;
                break;
            }
            case NORMAL_SMALL:{
                appliedEggLayout = R.layout.cell_entry_preview_sm;
                break;
            }
            case NORMAL_MICRO:{
                appliedEggLayout = R.layout.cell_entry_preview_mi;
                break;
            }
            default:
            case NORMAL_DEFAULT:{
                appliedEggLayout = R.layout.cell_entry_preview;
            }

        }

        View rowView=inflater.inflate(appliedEggLayout, null,true);

        TextView txtTitle = rowView.findViewById(R.id.txtTitle);
        TextView txtAge = rowView.findViewById(R.id.txtAge);
        TextView txtMetrics = rowView.findViewById(R.id.txtMetrics);
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
       // text = text.replaceAll("\n", "\n\n");
        markwon.setMarkdown(txtTitle,text);


        JSONObject metrics = Utility.computeMetrics(text);
        try {
            if(metrics != null) {
                txtMetrics.setText(String.format("L:%s | C: %s | U: %s", metrics.getInt("L"), metrics.getInt("C"), metrics.getInt("U")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        imageView.setImageResource(icons[random.nextInt(icons.length)]);

        return rowView;

    }
}
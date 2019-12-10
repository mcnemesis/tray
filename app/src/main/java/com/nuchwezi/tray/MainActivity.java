package com.nuchwezi.tray;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TRAY";
    private DBAdapter adapter;
    ArrayList<Cell> tray = new ArrayList<>();
    private TrayAdapter trayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add New Item", Snackbar.LENGTH_LONG)
                        .setAction("NEW", null).show();

                triggerAddCell();
            }
        });

        // other....
        adapter = new DBAdapter(this);
        adapter.open();

        initTrayStream();
        initStatusUpdate();
    }

    private void triggerAddCell() {
        Utility.showAlertPrompt(
                String.format("to %s", getString(R.string.app_name)),
                false,
                false,
                R.drawable.item_add,
                this, new ParametricCallback() {
                    @Override
                    public void call(String item) {
                        createAndSaveNewCell(new Date(), item);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        // do nothing...
                    }
                });
    }

    private void createAndSaveNewCell(Date date, String item) {
        Cell newCell = new Cell(date, item);
        tray.add(newCell);
        updateTrayCache();
        initTrayStream();
    }

    private void updateTrayCache() {

        Gson gson = new Gson();
        Type trayType = new TypeToken<ArrayList<Cell>>() {
        }.getType();

        String jTray = gson.toJson(tray, trayType);

        if (adapter.existsDictionaryKey(Utility.DICT_KEYS.TRAY_STORE)) {
            adapter.updateDictionaryEntry(new DBAdapter.DictionaryKeyValue(Utility.DICT_KEYS.TRAY_STORE, jTray));
        } else {
            adapter.createDictionaryEntry(new DBAdapter.DictionaryKeyValue(Utility.DICT_KEYS.TRAY_STORE, jTray));
        }

    }

    private void initTrayStream() {

        if(adapter.existsDictionaryKey(Utility.DICT_KEYS.TRAY_STORE)){

            String jTray = adapter.fetchDictionaryEntry(Utility.DICT_KEYS.TRAY_STORE);
            Gson gson = new Gson();
            Type trayType = new TypeToken<ArrayList<Cell>>() {
            }.getType();

            tray = gson.fromJson(jTray, trayType);

        }else {
            Utility.showToast("Sorry, but you haven't created any bookmarks yet.", this, Toast.LENGTH_LONG);
        }

        if(tray.size() == 0)
            tray.add(new Cell(new Date(), String.format("To store something in your %s, merely click the + button.", getString(R.string.app_name))));

        // sort tray so latest cells are at the top
        Collections.sort(tray, new Comparator<Cell>() {
            @Override
            public int compare(Cell c1, Cell c2) {
                return c2.getMoment().compareTo(c1.getMoment());
            }
        });

        trayAdapter = new TrayAdapter(this, tray);

        ListView trayListview =  findViewById(R.id.listItems);

        trayListview.setAdapter(trayAdapter);

        registerForContextMenu(trayListview);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.listItems) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.contextmenu_cells, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.bm_delete: {
                tray.remove(info.position);
                updateTrayCache();
                initTrayStream();
                return true;
            }
            case R.id.bm_share: {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, tray.get(info.position).getItem());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                return true;
            }
            case R.id.bm_copy: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.default_label_item), tray.get(info.position).getItem());
                clipboard.setPrimaryClip(clip);
                Utility.showToast("Copied to Clipboard", this);
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void initStatusUpdate() {
        //runs without timer, re-invoking itself using the handler posting mechanism on itself
        final Handler h2 = new Handler();
        Runnable run = new Runnable() {

            @Override
            public void run() {
                long delayMillis = 1000;
                updateStatus();
                h2.postDelayed(this, delayMillis);
            }
        };

        h2.postDelayed(run, 0);

    }

    private void updateStatus() {
        TextView txtStatus = findViewById(R.id.txtStatus);
        txtStatus.setText(String.format("It's %s \nand you have %s in your %s.", Utility.humaneDate(new Date(), true),
                Utility.pluralizeThis(getTrayStreamSize(), getString(R.string.label_items)), getString(R.string.app_name).toLowerCase()));
    }

    private int getTrayStreamSize() {
        return tray.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false; // change to true, to display in-app menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

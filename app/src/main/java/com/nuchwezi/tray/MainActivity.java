package com.nuchwezi.tray;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TRAY";
    private static final String DATACACHE_BASEDIR = "TRAYDATA";
    private DBAdapter adapter;
    ArrayList<Cell> tray = new ArrayList<>();
    ArrayList<Cell> filteredTray = new ArrayList<>();
    HashMap<Integer, Integer> filteredToMainTrayIndexMap = new HashMap<>();
    private TrayAdapter trayAdapter;
    private int shownEggCount = 0;
    private boolean filtersOn; // when in a search, we reference the filtered tray for example
    EditText eTxtSearchFilter;
    private ArrayList<Cell> activeTray = tray; // change this to determine meta-egg subset to render

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // for the animated background...
        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.rootLayout).getBackground();
        animDrawable.setEnterFadeDuration(10);
        animDrawable.setExitFadeDuration(5000);
        animDrawable.start();


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

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleIncomingText(intent); // Handle text being sent
            }
        }

        initTrayStream();
        initStatusUpdate();
        initSearchFilterMechanism();
    }

    private void initSearchFilterMechanism() {
        eTxtSearchFilter = findViewById(R.id.eTxtFilter);
        eTxtSearchFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                renderSearchResults();

            }
        });
    }

    private void renderSearchResults() {
        String searchFilter = eTxtSearchFilter.getText().toString();
        applySearchFilter(searchFilter);
    }

    private void applySearchFilter(String searchFilter) {

        if(tray == null)
            tray = new ArrayList<>();

        if(tray.size() == 0)
            tray.add(new Cell(new Date(), String.format("To store something in your %s, merely click the + button.", getString(R.string.app_name))));


        if(searchFilter == null){
            filtersOn = false;
            activeTray = tray;
            renderTray();
            return;
        }

        if(searchFilter.trim().length() == 0)
        {
            filtersOn = false;
            activeTray = tray;
            renderTray();
            return;
        }

        filteredTray = new ArrayList<>();
        filteredToMainTrayIndexMap = new HashMap<>();

        int trayIndex = 0;
        String activeSearchFilter = searchFilter.trim();
        for(Cell egg : tray){
            String item = egg.getItem();
            try {
                if (item.matches(activeSearchFilter) || item.contains(activeSearchFilter)) {
                    filteredTray.add(egg);
                    filteredToMainTrayIndexMap.put(filteredTray.size() - 1, trayIndex);
                }else
                if (item.toLowerCase().matches(activeSearchFilter) || item.toLowerCase().contains(activeSearchFilter)) {
                    filteredTray.add(egg);
                    filteredToMainTrayIndexMap.put(filteredTray.size() - 1, trayIndex);
                }
            }catch (Exception e){
            }
            trayIndex += 1;
        }

        filtersOn = true;
        activeTray = filteredTray;
        renderTray();
    }

    private void handleIncomingText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Utility.showAlertPrompt(
                    String.format("to %s", getString(R.string.app_name)),
                    false,
                    false,
                    R.drawable.item_add,
                    this, new ParametricCallback() {
                        @Override
                        public void call(String item) {
                            createAndSaveNewCell(new Date(), item);
                            Utility.showToast(String.format("Imported egg into %s", getString(R.string.app_name), getString(R.string.app_name)), MainActivity.this);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            // do nothing...
                            Utility.showToast("Import ignored.", MainActivity.this);
                        }
                    }, String.format("%s\n\n_#imported_",sharedText));

        }
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
                }, null);
    }

    private void createAndSaveNewCell(Date date, String item) {
        Cell newCell = new Cell(date, item);
        tray.add(newCell);
        updateTrayCache();
        initTrayStream();
    }

    private void updateTrayCache(ArrayList<Cell> latestTray) {
       tray = latestTray;
       updateTrayCache();
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

        initTrayFromCache();


        if(tray == null)
            tray = new ArrayList<>();

        if(tray.size() == 0)
            tray.add(new Cell(new Date(), String.format("To store something in your %s, merely click the + button.", getString(R.string.app_name))));

        if(filtersOn)
        {
            activeTray = filteredTray;
            renderSearchResults();
            return;
        }

        activeTray = tray;
        renderTray();
    }

    private void renderTray() {

        trayAdapter = new TrayAdapter(this, activeTray, trayAdapter != null? trayAdapter.getActiveEggRenderStyle() : TrayAdapter.EggRenderStyle.NORMAL_DEFAULT);

        ListView trayListview =  findViewById(R.id.listItems);

        trayListview.setAdapter(null);
        trayListview.setAdapter(trayAdapter);

        registerForContextMenu(trayListview);

        shownEggCount = activeTray.size();
    }

    private ArrayList<Cell> initTrayFromCache() {
        String jTray = null;

        if(adapter.existsDictionaryKey(Utility.DICT_KEYS.TRAY_STORE)){

            jTray = adapter.fetchDictionaryEntry(Utility.DICT_KEYS.TRAY_STORE);

        }else {
            Utility.showToast("Sorry, but you haven't stored any eggs yet!", this, Toast.LENGTH_LONG);
        }


        Gson gson = new Gson();
        Type trayType = new TypeToken<ArrayList<Cell>>() {
        }.getType();

        try {
            tray = gson.fromJson(jTray, trayType);
        }catch (JsonSyntaxException syntaxException){
            Log.e(TAG,syntaxException.toString());
            tray = Utility.obtainCellArrayListFromString(jTray); // let's try with the JSONObject mechanism
        }

        if(tray == null)
            tray = new ArrayList<>();

        // sort tray so latest cells are at the top
        Collections.sort(tray, new Comparator<Cell>() {
            @Override
            public int compare(Cell c1, Cell c2) {
                return c2.getMoment().compareTo(c1.getMoment());
            }
        });

        return tray;
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
                tray.remove(filtersOn ? filteredToMainTrayIndexMap.get(info.position) : info.position);
                updateTrayCache();
                initTrayStream();
                return true;
            }
            case R.id.bm_share: {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, tray.get(filtersOn ? filteredToMainTrayIndexMap.get(info.position) : info.position).getItem());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                return true;
            }
            case R.id.bm_copy: {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.default_label_item), tray.get(filtersOn ? filteredToMainTrayIndexMap.get(info.position) : info.position).getItem());
                clipboard.setPrimaryClip(clip);
                Utility.showToast(String.format("Copied %s to Clipboard", getString(R.string.default_label_item), getString(R.string.app_name)), this);
                return true;
            }
            case R.id.bm_clone: {
                createAndSaveNewCell(new Date(), tray.get(filtersOn ? filteredToMainTrayIndexMap.get(info.position) : info.position).getItem());
                Utility.showToast(String.format("Cloned %s into %s", getString(R.string.default_label_item), getString(R.string.app_name)), this);
                return true;
            }
            case R.id.bm_alter: {
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
                        }, tray.get(filtersOn ? filteredToMainTrayIndexMap.get(info.position) : info.position).getItem());
                Utility.showToast(String.format("Cloned %s into %s", getString(R.string.default_label_item), getString(R.string.app_name)), this);
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
        int traySize = getTrayStreamSize();
        if(shownEggCount == traySize) {
            txtStatus.setText(String.format(this.getString(R.string.status_pattern),
                    Utility.humaneDate(new Date(), true),
                    Utility.pluralizeThis(traySize,
                            getString(R.string.label_items)),
                    getString(R.string.app_name).toLowerCase()));
        }else{
            double filterRatio = shownEggCount * 1.0 / traySize;
            String filterKPIs = String.format("%s. Active filter has a %s%% Significance (%s)",
                    getString(R.string.app_name).toLowerCase(),
                    Math.round(Utility.computePercentage(1 - filterRatio)),
                    String.format("%s/%s", shownEggCount, traySize)
                    );
            txtStatus.setText(String.format(this.getString(R.string.status_pattern),
                    Utility.humaneDate(new Date(), true),
                    Utility.pluralizeThis(traySize,
                            getString(R.string.label_items)),
                    filterKPIs
                    ));
        }
    }

    private int getTrayStreamSize() {
        return tray == null ? 0 : tray.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; // change to true, to display in-app menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            /*case R.id.action_settings: {
                return true;
            }*/
            case R.id.action_export: {
                exportRecordsToFile();
                return true;
            }
            case R.id.action_import: {
                importRecordsFromFile();
                return true;
            }
            case R.id.action_about: {
                showAbout();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean getOrRequestWriteStoragePermission() {
        if(hasPermissionWriteStorage()){
            return true;
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }

        return false;
    }

    private boolean hasPermissionWriteStorage() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean getOrRequestReadStoragePermission() {
        if(hasPermissionReadStorage()){
            return true;
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }

        return false;
    }

    private boolean hasPermissionReadStorage() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void importRecordsFromFile() {

        if(!getOrRequestReadStoragePermission()){
            Utility.showToast("Please allow the app to read from your storage first.", this);
            return;
        }

        String personaMimeType = getString(R.string.mimeType_tray_datafile);

        // Create the ACTION_GET_CONTENT Intent
        Intent getContentIntent = FileUtils.createGetContentIntent();

        getContentIntent.setType(personaMimeType);
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent chooserIntent = Intent.createChooser(getContentIntent, getString(R.string.label_traydata_from_file));


        try {
            startActivityForResult(chooserIntent, INTENT_MODE.CHOOSE_TRAYDATA_FILE_REQUESTCODE);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error_no_file_manager_found, Toast.LENGTH_SHORT).show();
        }
    }


    private void exportRecordsToFile() {
        if(!getOrRequestWriteStoragePermission()){
            Utility.showToast("Please allow the app to write to your storage first.", this);
            return;
        }


        if(adapter.existsDictionaryKey(Utility.DICT_KEYS.TRAY_STORE)) {

            String sCacheRecords = adapter.fetchDictionaryEntry(Utility.DICT_KEYS.TRAY_STORE);

            String dataPath = null;

            try {
                dataPath = Utility.createSDCardDir(DATACACHE_BASEDIR, getFilesDir());
            } catch (Exception e) {
                Log.e(TAG, "DATA Path Error : " + e.getMessage());
                Utility.showToast(e.getMessage(), getApplicationContext(),
                        Toast.LENGTH_LONG);
            }

            if(dataPath != null) {

                String SESSION_GUUID = java.util.UUID.randomUUID().toString();
                String dataCacheFile = String.format("%s/%s-%s.%s", dataPath, Utility.humaneDate(new Date(), true), SESSION_GUUID,
                        "json");

                Writer output = null;
                File file = new File(dataCacheFile);
                try {
                    output = new BufferedWriter(new FileWriter(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output.write(sCacheRecords);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Utility.showToast(String.format("%s Data Cached at : %s", getString(R.string.app_name), dataCacheFile), this);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode){
            case INTENT_MODE.CHOOSE_TRAYDATA_FILE_REQUESTCODE: {
                if(intent == null) {
                    Utility.showToast("Failed to perform action", this);
                    break;
                }
                //String selectedPath = intent.getDataString();

                final Uri uri = intent.getData();

                // Get the File path from the Uri
                // Get the File path from the Uri
                String selectedPath = FileUtils.getPath(this, uri);

                loadImportedTRAYDataFromPath(selectedPath);
                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void loadImportedTRAYDataFromPath(String selectedPath) {
        String sCacheRecords_Imported  = null;
        try {

            sCacheRecords_Imported = Utility.readFileToString(selectedPath);

            Gson gson = new Gson();
            Type trayType = new TypeToken<ArrayList<Cell>>() {
            }.getType();

            ArrayList<Cell> parsedTray;
            try {
                parsedTray = gson.fromJson(sCacheRecords_Imported, trayType);
            }catch (JsonSyntaxException syntaxException){
                Log.e(TAG,syntaxException.toString());
                parsedTray = Utility.obtainCellArrayListFromString(sCacheRecords_Imported); // let's try with the JSONObject mechanism
            }


            HashSet<Cell> dbTray = Utility.listToSet(initTrayFromCache());
            boolean imported = false;
            if(parsedTray != null){
                for(Cell cell : parsedTray){
                    if(!dbTray.contains(cell)){
                        dbTray.add(cell);
                        imported = true;
                    }
                }
            }

            if(imported){
                updateTrayCache(Utility.setToCellList(dbTray));
                initTrayStream();
            }
            Utility.showToast("Refreshing Records...", this);
        } catch (Exception e) {
            e.printStackTrace();
            Utility.showAlert("Import File Error","Sorry, but loading the eggs from file has failed! Ensure the file can be read, and is legitimate!",
                    R.drawable.warning,this);
            return;
        }
    }


    private void showAbout() {

        Utility.showAlert(
                this.getString(R.string.app_name),
                String.format("Version %s (Build %s)\n\n%s",
                        Utility.getVersionName(this),
                        Utility.getVersionNumber(this),
                        this.getString(R.string.powered_by)),
                R.mipmap.ic_launcher, this);
    }

    public void setEggRenderStyleTiny(View view) {
        trayAdapter.setActiveEggRenderStyle(TrayAdapter.EggRenderStyle.NORMAL_SMALL);
        renderTray();
    }

    public void setEggRenderStyleNormal(View view) {
        trayAdapter.setActiveEggRenderStyle(TrayAdapter.EggRenderStyle.NORMAL_DEFAULT);
        renderTray();
    }

    private static class INTENT_MODE {

        public static final int CHOOSE_TRAYDATA_FILE_REQUESTCODE = 3;

    }
}

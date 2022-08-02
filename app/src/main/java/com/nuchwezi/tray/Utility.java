package com.nuchwezi.tray;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

class Utility {
    public static String Tag = MainActivity.TAG;

    public static int getRandomColor() {
        RandomColors randomColors = new RandomColors();
        return  randomColors.getColor();
    }

    public static int getContrastVersionForColor(int color) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color),
                hsv);
        if (hsv[2] < 0.5) {
            hsv[2] = 0.99f;//0.7f;
        } else {
            hsv[2] = 0.3f;
        }
        hsv[1] = hsv[1] * 0.25f;//0.2f
        return Color.HSVToColor(hsv);
    }

    /*
     * Display a toast with the default duration : Toast.LENGTH_SHORT
     */
    public static void showToast(String message, Context context) {
        showToast(message, context, Toast.LENGTH_SHORT);
    }

    /*
     * Display a toast with given Duration
     */
    public static void showToast(String message, Context context, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void showAlert(String title, String message, Context context) {
        showAlert(title, message, R.mipmap.ic_launcher, context, null, null,null);
    }

    public static void showAlert(String title, String message, int iconId, Context context) {
        showAlert(title, message, iconId, context,  null, null,null);
    }

    public static void showAlert(String title, String message, Context context, Runnable yesCallback,  Runnable noCallback, Runnable cancelCallback ) {
        showAlert(title, message, R.mipmap.ic_launcher, context, yesCallback, noCallback,cancelCallback);
    }

    public static void showAlert(String title, String message, int iconId, Context context, Runnable yesCallback,  Runnable noCallback, Runnable cancelCallback ) {
        showAlertFactory(title, message,iconId, context, yesCallback, noCallback,cancelCallback);
    }

    public static void showAlertFactory(String title, String message, int iconId,
                                        Context context, final Runnable yesCallback, final Runnable noCallback, final Runnable cancelCallback) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setIcon(iconId);
            builder.setTitle(title);

            LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogContent = mInflater.inflate(R.layout.alert_view, null);
            ((TextView)dialogContent.findViewById(R.id.msgText)).setText(message);
            builder.setView(dialogContent);

            if(yesCallback != null){
                builder.setPositiveButton( noCallback == null ? "OK"  : "YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        yesCallback.run();
                    }
                });
            }

            if(noCallback != null){
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        noCallback.run();
                    }
                });
            }

            if(cancelCallback != null){
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cancelCallback.run();
                    }
                });
            }

            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            Log.e(Tag, "Alert Error : " + e.getMessage());
        }

    }

    public static void showAlertPrompt(String title, final boolean allowEmpty, boolean addMask, int iconId,
                                       final Context context, final ParametricCallback yesCallback, final Runnable cancelCallback, String defaultText) {
        try {
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
            final View dialogView = layoutInflaterAndroid.inflate(R.layout.alert_prompt, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);

            builder.setIcon(iconId);
            builder.setTitle(title);

            if(addMask){
                EditText editText = dialogView.findViewById(R.id.eTxtPromptValue);
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setTransformationMethod(new PasswordTransformationMethod());
            }

            if(yesCallback != null){
                builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = dialogView.findViewById(R.id.eTxtPromptValue);
                        String value = editText.getText().toString();
                        if(!allowEmpty){
                            if(value.trim().length() == 0){
                                Utility.showToast("Please set a value!", context);
                            }
                        }
                        yesCallback.call(value);
                    }
                });
            }

            if(cancelCallback != null){
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cancelCallback.run();
                    }
                });
            }

            if(defaultText != null){
                EditText editText = dialogView.findViewById(R.id.eTxtPromptValue);
                editText.setText(defaultText);
            }

            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            Log.e(Tag, "Alert Error : " + e.getMessage());
        }

    }


    public static int getVersionNumber(Context context) {
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pinfo != null ? pinfo.versionCode : 1;
    }

    public static String getVersionName(Context context) {
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pinfo != null ? pinfo.versionName : "DEFAULT";
    }

    public static JSONArray removeField(JSONArray jsonArray, int index) {

        JSONArray newArray = new JSONArray();

        for(int i = 0; i < jsonArray.length(); i++)
            if(i != index)
                try {
                    newArray.put(jsonArray.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        return newArray;
    }

    public static ArrayList<String> setToList(HashSet<String> set) {
        ArrayList<String> items = new ArrayList<>();
        for(String s: set)
            items.add(s);

        return items;
    }

    public static String selectRandom(JSONObject categoriesMap, String category) {
        try {
            JSONArray items = categoriesMap.getJSONArray(category);
            if(items.length() == 0)
                return null;
            else{
                Random random = new Random();
                int rIndex = random.nextInt(items.length());
                return items.getString(rIndex);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> toList(Iterator<String> iterator) {
        ArrayList<String> items = new ArrayList<>();
        for(; iterator.hasNext();) {
            items.add(iterator.next());
        }
        return items;
    }

    /*
     * Will create directory on the External Storage Card with the given dirName
     * name.
     *
     * Throws an exception is dirName is null, and returns the name of the
     * created directory if successful
     */
    public static String createSDCardDir(String dirName, File internalFilesDir) {

        Log.d(Tag, "Creating Dir on sdcard...");

        if (dirName == null) {
            Log.e(Tag, "No Directory Name Specified!");
            return null;
        }

        File exDir = Environment.getExternalStorageDirectory();

        if (exDir != null) {

            File folder = new File(exDir, dirName);

            boolean success = false;

            if (!folder.exists()) {
                folder.mkdirs();
                success = folder.exists(); // seems a better approach...
                if(success)
                Log.d(Tag, "Created Dir on sdcard...");

            } else {
                success = true;
                Log.d(Tag, "Dir exists on sdcard...");
            }

            if (success) {
                return folder.getAbsolutePath();
            } else {
                Log.e(Tag, "Failed to create on sdcard...");
                return null;
            }
        } else {

            File folder = new File(internalFilesDir, dirName);

            boolean success = false;

            if (!folder.exists()) {
                success = folder.mkdirs();
                Log.d(Tag, "Created Dir on sdcard...");
            } else {
                success = true;
                Log.d(Tag, "Dir exists on sdcard...");
            }

            if (success) {
                return folder.getAbsolutePath();
            } else {
                Log.e(Tag, "Failed to create on sdcard...");
                return null;
            }
        }
    }

    public static String humaneDateStripped(Date date,boolean withSeconds) {
        DateFormat df = new SimpleDateFormat(withSeconds ?"yyyyMMMdd__HH_mm_ss" : "yyyyMMMdd__HH_mm");
        return df.format(date);
    }

    public static String humaneDate(Date date,boolean withSeconds) {
        DateFormat df = new SimpleDateFormat(withSeconds ?"MMM dd, yyyy HH:mm:ss" : "MMM dd, yyyy HH:mm");
        return df.format(date);
    }


    public static String readFileToString(FileDescriptor fileDescriptor)  {
        FileInputStream fin = null;
        fin = new FileInputStream(fileDescriptor);
        String ret = null;
        try {
            ret = convertStreamToString(fin);
        } catch (IOException e) {
            return null;
        }
        //Make sure you close all streams.
        try {
            fin.close();
        } catch (IOException e) {
            return null;
        }
        return ret;
    }

    public static String readFileToString(String filePath)  {
        File fl = new File(filePath);
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fl);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        String ret = null;
        try {
            ret = convertStreamToString(fin);
        } catch (IOException e) {
            return null;
        }
        //Make sure you close all streams.
        try {
            fin.close();
        } catch (IOException e) {
            return null;
        }
        return ret;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        // http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        Boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if(firstLine){
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }

    public static HashSet<String> JSONArrayToSet(JSONArray jsonArray) {
        HashSet<String> set = new HashSet<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                set.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return set;
    }

    public static JSONArray setToJSONArray(HashSet<String> hashSet) {
        JSONArray jsonArray = new JSONArray();
        for(String item: hashSet)
            jsonArray.put(item);

        return jsonArray;
    }

    public static String pluralizeThis(int count, String label) {
        return count == 1 ? String.format("%s %s", count, label) : String.format("%s %ss", count, label);
    }

    public static CharSequence computeAge(Date moment) {
        Date now = new Date();
        long diffMillis = now.getTime() - moment.getTime();
        long diffSecs = (long) Math.round(diffMillis / 1000);
        long diffMins = (long) Math.round(diffSecs / 60);
        long diffHrs = (long) Math.round(diffMins / 60);
        long diffDays = (long) Math.round(diffHrs/24);
        long diffYears = (long) Math.round(diffDays/365);
        StringBuilder ageSB = new StringBuilder();
        if(diffYears > 0){
            ageSB.append(String.format("%d Year%s", diffYears, diffYears == 1 ? "" : "s"));
            diffDays = (diffDays % 365);
        }
        if(diffDays > 0){
            ageSB.append(String.format(" %d Day%s", diffDays, diffDays == 1 ? "" : "s"));
            diffHrs = diffHrs - ((diffYears * 365 * 24)+ (diffDays * 24));
        }
        if(diffHrs > 0){
            ageSB.append(String.format(" %d Hr%s", diffHrs, diffHrs == 1 ? "" : "s"));
            diffMins = diffMins - ((diffYears * 365 * 24 * 60)+ (diffDays * 24 * 60) + (diffHrs * 60) );
        }
        if(diffMins > 0){
            if(diffMins > 60){
                diffMins = Math.round((diffMillis - ((diffYears * 365 * 24 * 60 * 60 * 1000)+ (diffDays * 24 * 60 * 60 * 1000) +  (diffHrs * 60 * 60 * 1000)))/(60 * 1000));
            }
            ageSB.append(String.format(" %d Min%s", diffMins, diffMins == 1 ? "" : "s"));
            diffSecs = diffSecs - (((diffYears * 365 * 24 * 60)+ (diffDays * 24 * 60 * 60) + (diffHrs * 60 * 60) ) + (diffMins * 60));
        }
        if(diffSecs > 0){
            long bestSecDiff = Math.round((diffMillis - ((diffYears * 365 * 24 * 60 * 60 * 1000)+ (diffDays * 24 * 60 * 60 * 1000) + (diffHrs * 60 * 60 * 1000)+ (diffMins * 60 * 1000)))/1000);
            ageSB.append(String.format(" %d Sec%s", bestSecDiff, bestSecDiff == 1 ? "" : "s"));
        }

        return ageSB.toString().trim();
    }

    public static ArrayList<Cell> obtainCellArrayListFromString(String jTray) {
        ArrayList<Cell> outTray = new ArrayList<>();
        JSONArray parsedTray = null;
        try {
            parsedTray = new JSONArray(jTray);

            for(int c =0; c < parsedTray.length(); c++){
                JSONObject parsedCell = null;
                try {
                    parsedCell = new JSONObject(parsedTray.getString(c));

                    Cell cell = null;

                    try {
                        Date moment = Utility.parseDate(parsedCell.getString("moment")); // NOTE: moment could be null!
                        cell = new Cell(moment == null ? new Date() : moment, parsedCell.getString("item"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(cell != null)
                        outTray.add(cell);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outTray;
    }

    private static Date parseDate(String moment) {
        DateFormat df = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        try {
            return df.parse(moment);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static HashSet<Cell> listToSet(ArrayList<Cell> cellArrayList) {
        HashSet<Cell> tray = new HashSet<>();
        for(Cell cell : cellArrayList)
            tray.add(cell);

        return tray;
    }

    public static ArrayList<Cell> setToCellList(HashSet<Cell> cellHashSet) {
        ArrayList<Cell> tray = new ArrayList<>();
        for(Cell cell : cellHashSet)
            tray.add(cell);

        return tray;
    }

    public static double computePercentage(double ratio) {
        return 100 * ratio;
    }

    public static JSONObject computeMetrics(String text) {
        JSONObject metrics = new JSONObject();
        int nLines = text.split("\\n").length;
        int nChar = text.length();
        HashSet<Character> characterHashSet = new LinkedHashSet<>();
        text.toLowerCase().chars().forEach(chr -> characterHashSet.add((char)chr));
        int nUniqChar = characterHashSet.size();

        try {
            metrics.put("L", nLines); // number of lines
            metrics.put("C", nChar); // number of all characters
            metrics.put("U", nUniqChar); // number of unique characters - case insensitive

            return metrics;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public class DICT_KEYS {
        public static final String TRAY_STORE = "TRAY_STORE";
    }
}

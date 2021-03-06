package com.projecttango.examples.java.pointcloud;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import com.google.atap.tangoservice.TangoPointCloudData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PointCloudExporter {
    /* Define the header of the PCD file
     * WIDTH and VIEWPOINT are defined dynamically based on the cloud
     */
    private static final String VERSION = "VERSION .7\n";
    private static final String FIELDS = "FIELDS x y z\n";
    private static final String SIZE = "SIZE 4 4 4\n";
    private static final String TYPE = "TYPE F F F\n";
    private static final String COUNT = "COUNT 1 1 1\n";
    private static final String HEIGHT = "HEIGHT 1\n";
    private static final String DATA = "DATA ascii\n";
    private final Context context;
    private String mfilename;

    public PointCloudExporter(Context context) {
        this.context = context;
    }


    public Boolean export(TangoPointCloudData data, String fname) {
        mfilename = fname;
        try {
            Boolean b =  new ExportAsyncTask().execute(data).get();
            return b;
        } catch(Exception e){
            Log.e("Export Error","Export Async Task Failed");
            e.printStackTrace();
        }
        return false;
    }

    private class ExportAsyncTask extends AsyncTask<TangoPointCloudData, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(TangoPointCloudData... params) {
            if (params.length == 0) {
                return null;
            }
            //Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US);
            TangoPointCloudData data = params[0];

            String fileName = mfilename + ".pcd";
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/pointclouds");
            if (!f.exists()) {
                f.mkdirs();
            }
            final File file = new File(f, fileName);
            try {
                OutputStream os = new FileOutputStream(file);
                int size = data.numPoints;
                FloatBuffer floatBuffer = data.points;
                floatBuffer.rewind();

                // Initialize and write header fields
                String WIDTH = "WIDTH " + String.valueOf(size) + "\n"; //In PCD .7, WIDTH is the number of points for unorganized data
                String POINTS = "POINTS " + String.valueOf(size) + "\n"; //As of PCD .7, POINTS is redundant but still included
                String VIEWPOINT = "VIEWPOINT 0 0 0 1 0 0 0\n"; //To be changed
                String HEADER = VERSION + FIELDS + SIZE + TYPE + COUNT + WIDTH + HEIGHT + VIEWPOINT + POINTS + DATA;
                os.write(HEADER.getBytes());

                for (int i = 0; i < size; i++) {
                    String x = String.valueOf(floatBuffer.get());
                    String y = String.valueOf(floatBuffer.get());
                    String z = String.valueOf(floatBuffer.get());
                    String confidence = String.valueOf(floatBuffer.get());
                    String row = x + " " + y + " " + z + "\n";
                    os.write(row.getBytes());
                }
                os.close();
                return true;
            } catch (IOException e) {
                Log.e("PointCloudExporterIO","IO Exception");
                return false;
            }
        }
    }
}

package utils;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogManager {

    private static final String LOG_FOLDER = "TECUIDA";
    private static final String LOG_FILE = "TECUIDA_log.txt";

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void logFile(String key, String log) {
        logFile(key + " : " + log);
    }

    public static void logFile(String log) {

        if(isExternalStorageWritable()) {
            File storage = Environment.getExternalStorageDirectory();
            File folder = new File(storage.getPath() + "/" + LOG_FOLDER);
            if(!folder.exists()) {
                if (!folder.mkdirs()) {
                    Log.i("LOG_FILE", "Can't create log folder");
                    throw new RuntimeException("Can't create log folder");
                }
//                else {
//                    MediaScannerConnection.scanFile(this,
//                        new String[] { folder.toString() }, null,
//                        new MediaScannerConnection.OnScanCompletedListener() {
//                            @Override
//                            public void onScanCompleted(String path, Uri uri) {
//                                Log.i("ExternalStorage", "Scanned " + path + ":");
//                                Log.i("ExternalStorage", "-> uri=" + uri);
//                            }
//                        });
//                }
            }

            try {
                FileWriter file = new FileWriter(folder.getPath() + "/" + LOG_FILE, true);
                String logLine = DateFormatter.getFormattedDate() + ", " + log + "\n";
                Log.i("LOG_FILE", "FOLDER="+folder.getPath()+" LOG="+logLine);
                file.append(logLine);
                file.close();

            } catch (IOException e) {
                Log.i("LOG_FILE", "Can't write file");
                throw new RuntimeException("Can't write file");
            }

        }
        else {
            Log.i("LOG_FILE", "Can't write on storage device");
            throw new RuntimeException("Can't write on storage device");
        }


    }


}

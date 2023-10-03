// Custom Java bindings to Fujifilm/camlib
// Copyright 2023 Daniel C - https://github.com/petabyt/fujiapp

package dev.danielc.fujiapp;
import android.util.Log;
import android.os.Environment;
import java.io.File;
import org.json.JSONObject;
import java.util.Arrays;
import dev.petabyt.camlib.*;

public class Backend extends CamlibBackend {
    static {
        System.loadLibrary("fujiapp");
    }

    public static MyWiFiComm wifi;

    // Block all communication in UsbComm and WiFiComm
    // Write reason + code, and reconnect popup
    public static void reportError(int code, String reason) {
        if (wifi.killSwitch == false) {
            wifi.killSwitch = true;

            print("Safely killed connection: " + code + "\n");
            if (reason != null) {
                print("Reason: " + reason + "\n");
            }

            Backend.wifi.close();
        }
    }

    // In order to give the backend access to the static methods, new objects must be made
    private static boolean haveInited = false;
    public static void init() {
        if (haveInited == false) {
            wifi = new MyWiFiComm();
            cInit(new Backend(), wifi);
        }
        haveInited = true;
    }

    // Clear entire backend for a new connection
    public static void clear() {
        //wifi.connection = WiFiComm.Status.OFF;
    }

    // Constants
    //public static final String FUJI_IP = "192.168.1.33";
    public static final String FUJI_IP = "192.168.0.1";
    public static final int FUJI_CMD_PORT = 55740;
    public static final int FUJI_EVENT_PORT = 55741;
    public static final int FUJI_VIDEO_PORT = 55742;
    public static final int OPEN_TIMEOUT = 1000;
    public static final int TIMEOUT = 2000;

    // Note: 'synchronized' means only one of these methods can be used at time -
    // java's version of a mutex
    public native synchronized static void cInit(Backend b, MyWiFiComm c);
    public native synchronized static int cPtpFujiInit();
    public native synchronized static int cPtpFujiPing();
    public native synchronized static int cPtpGetPropValue(int code);
    public native synchronized static int cPtpFujiWaitUnlocked();
    public native synchronized static int cFujiConfigVersion();
    public native synchronized static int cFujiConfigInitMode();
    public native synchronized static String cPtpRun(String req);
    public native synchronized static byte[] cPtpGetThumb(int handle);
    public native synchronized static byte[] cFujiGetFile(int handle);
    public native synchronized static boolean cIsMultipleMode();
    public native synchronized static boolean cIsUntestedMode();
    public native synchronized static boolean cCameraWantsRemote();
    public native synchronized static int[] cGetObjectHandles();
    public native synchronized static int cFujiTestStartRemoteSockets();
    public native synchronized static int cFujiEndRemoteMode();
    public native synchronized static int cFujiConfigImageGallery();
    public native synchronized static String cFujiGetUncompressedObjectInfo(int handle);

    // For test suite only
    public native synchronized static void cTesterInit(Tester t);
    public native synchronized static String cTestFunc();
    public native synchronized static int cFujiTestSetupImageGallery();
    public native synchronized static int cTestStuff();
    public native synchronized static int cFujiTestSuiteSetup();

    // Enable disable verbose logging to file
    public native synchronized static int cRouteLogs(String filename);
    public native synchronized static void cEndLogs();

    public native static boolean cIsUsingEmulator();

    // Runs a request with integer parameters
    public static JSONObject run(String req, int[] arr) throws Exception {
        // Build camlib request string (see docs/)
        req += ";";
        for (int i = 0; i < arr.length; i++) {
            req += String.valueOf(arr[i]);
            if (i != arr.length - 1) {
                req += ",";
            }
        }
        req += ";";

        String resp = cPtpRun(req);
        try {
            JSONObject jsonObject = new JSONObject(resp);
            if (jsonObject.getInt("error") != 0) {
                Backend.print("Non zero error: " + Integer.toString(jsonObject.getInt("error")) + "\n");
                throw new Exception("Error code");
            }

            return jsonObject;
        } catch (Exception e) {
            throw e;
        }
    }

    public static JSONObject run(String req) throws Exception {
        return run(req, new int[]{});
    }

    public static JSONObject fujiGetUncompressedObjectInfo(int handle) throws Exception {
        try {
            String resp = cFujiGetUncompressedObjectInfo(handle);
            if (resp == null) throw new Exception("Failed to get obj info");
            return new JSONObject(resp);
        } catch (Exception e) {
            throw e;
        }
    }

    // JNI -> UI log communication
    public static String logLocation = "main";

    public static void clearPrint() {
        basicLog = "";
        MainActivity.getInstance().setErrorText("");
    }

    // debug function for both Java frontend and JNI backend
    private static String basicLog = "";
    public static void print(String arg) {
        Log.d("fudge", arg);
        basicLog += arg;

        String[] lines = basicLog.split("\n");
        if (lines.length > 5) {
            basicLog = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length)) + "\n";
        }

        updateLog();
    }

    public static void updateLog() {
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().setErrorText(basicLog);
        }
        if (Gallery.getInstance() != null) {
            Gallery.getInstance().setErrorText(basicLog);
        }
    }

    public static String getDownloads() {
        String mainStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fujifilm = mainStorage + File.separator + "DCIM" + File.separator + "fuji";
        return fujifilm;
    }

    public static String getLogPath() {
        String mainStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fujifilm = mainStorage + File.separator + "Documents" + File.separator + "fujiapp.txt";
        return fujifilm;
    }
}


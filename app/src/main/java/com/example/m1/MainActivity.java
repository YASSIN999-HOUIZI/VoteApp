package com.example.m1;
import com.example.m1.ClientEndpoint;
import com.example.m1.WebSocketClientEndpoint;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("tfhemain");
    }
    public static File path;
    public static String Pk_Path;
    public static String CloudKey_Path;
    public static String CloudData_Path;
    public static String SecretKey_Path;
    private final String cppVerifierUrl = "192.168.1.7:8080";
    private final String attribute = "sin";
    //private final ClientEndpoint verificationEndpoint = new ClientEndpoint();
    //private final WebSocketClientEndpoint Endpoint = new WebSocketClientEndpoint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = getFilesDir();
        Pk_Path = path+"/PK.key";
        CloudKey_Path = path+"/CK.key";
        CloudData_Path = path+"/CD.data";
        SecretKey_Path = path+"/SK.key";
    }
   /* public native int send(String Path,int port);
   *public void test(View view) throws InterruptedException, ParseException, IOException, ClassNotFoundException {
        int status = init(SecretKey_Path,Pk_Path,CloudKey_Path,CloudData_Path,18);
        System.out.println("status:"+status);
        send(CloudKey_Path,8060);
        send(CloudData_Path,8060);
        send(Pk_Path,8060);
    }*/
    //public native String encrypt(int x1, int y1, int z1, String );
   public native int Decrypt(String ClaimPath, String SK_Path, int nbit);
   public native int init(String SecretKey_Path,String Pk_Path, String CloudKey_Path,String CloudData_Path, int x1, int y1, int z1);

    public void verify(View view) throws InterruptedException, ParseException, IOException, ClassNotFoundException {
        //Start time
        long begin = System.currentTimeMillis();
        String path = String.valueOf(MainActivity.path);
        int x1=2;
        int y1=1;
        int z1=1;
        int status = init(SecretKey_Path,Pk_Path,CloudKey_Path,CloudData_Path,x1,y1,z1);
        //compressFile(path + "/CK.key",path + "/CK_Compressed.key");
        System.out.println("status:"+status);
        ClientEndpoint proofEndpoint = new ClientEndpoint();
        proofEndpoint.createWebSocketClient("ws://" + cppVerifierUrl);
        //proofEndpoint.latch.await();
        proofEndpoint.webSocketClient.connect();
        proofEndpoint.latch.await();
        proofEndpoint.latch = new CountDownLatch(1);
        proofEndpoint.pos = 0;
        proofEndpoint.webSocketClient.send("VOTING");
        proofEndpoint.sendFile(path + "/CK.key", "CK.key");
        proofEndpoint.sendFile(path + "/CD.data", "CD.data");
        proofEndpoint.sendFile(path + "/PK.key", "PK.key");
        int A = (x1*x1*x1) + (y1*y1*y1) + (z1*z1*z1);
        proofEndpoint.webSocketClient.send(String.valueOf(A));
        //proofEndpoint.webSocketClient.send("DONE");
        proofEndpoint.latch.await();
        int r = Decrypt(path+"/vot_answer.data", path+"/SK.key",1);
        int n = Decrypt(path+"/H_NULL.data", path+"/SK.key",5);
        System.out.println("r:"+r);
        System.out.println("null:"+n);
        proofEndpoint.webSocketClient.send(String.valueOf(r));
        proofEndpoint.webSocketClient.send(String.valueOf(n));

        proofEndpoint.webSocketClient.close();
        //End time
        long end = System.currentTimeMillis();

        long time = end-begin;
        System.out.println();
        System.out.println("Elapsed Time: "+time +" milli seconds");
    }
    private static final String TAG = "FileCompressionUtil";

    public void compressFile(String inputFile,String outputFile) {
        //String inputFile = path+"/CK.key";       // Replace with the actual input file path
        //String outputFile = path+"/CK_Compressed.key";  // Replace with the desired output file path

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, bytesRead);
            }

            Log.d(TAG, "File compressed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void decompressFile(View view) throws IOException {
        String outputFile = path+"/CK_Decompressed.key";       // Replace with the actual input file path
        String compressedFile = path+"/CK_Compressed.key";
        try (FileInputStream fis = new FileInputStream(compressedFile);
             GZIPInputStream gzipIS = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipIS.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            Log.d(TAG, "File decompressed successfully.");

        }
    }
}
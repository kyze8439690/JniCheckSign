package me.yugy.example.jnichecksign;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by gzyanghui on 2014/8/15.
 */
public class MD5Utils {

    private static final String TAG = MD5Utils.class.getName();
    private static final MessageDigest md5MessageDigest = getMD5MessageDigest();

    /**
     * generate 32 bit md5 encrypted string
     * @param raw
     * @return
     */
    public static String md5(String raw){
        if(md5MessageDigest == null){
            return null;
        }
        // Encryption algorithm
        md5MessageDigest.update(raw.getBytes(), 0, raw.length());
        String md5 = new BigInteger(1, md5MessageDigest.digest()).toString(16);
        while ( md5.length() < 32 ) {
            md5 = "0"+md5;
        }
        return md5;
    }

    public static String md5(File updateFile) {
        if(md5MessageDigest == null){
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                md5MessageDigest.update(buffer, 0, read);
            }
            byte[] md5sum = md5MessageDigest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = md5(updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        Log.v(TAG, "Calculated digest: " + calculatedDigest);
        Log.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    private static MessageDigest getMD5MessageDigest(){
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }
    }

}

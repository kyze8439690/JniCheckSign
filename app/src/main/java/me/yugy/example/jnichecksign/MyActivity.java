package me.yugy.example.jnichecksign;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;


public class MyActivity extends ActionBarActivity {

    private TextView mJavaText;
    private TextView mJniText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mJavaText = (TextView) findViewById(R.id.java_sign);
        mJniText = (TextView) findViewById(R.id.jni_sign);

        try {
            mJavaText.setText("Signature md5 get from java:" + loadSignatureMd5ByJava());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mJavaText.setText("PackageName not found.");
        }

        System.loadLibrary("check-sign");

        mJniText.setText("Get token from jni:" + getToken());


    }

    private String loadSignatureMd5ByJava() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        return MD5Utils.md5(packageInfo.signatures[0].toCharsString());
    }

    private native String getToken();

}

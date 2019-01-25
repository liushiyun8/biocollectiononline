package com.emptech.biocollectiononline.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.emptech.biocollectiononline.utils.LogUtils;


public class BaseActivity extends AppCompatActivity {
    public String TAG = BaseActivity.class.getSimpleName();

    public static String PARAM_MAGIC = "magic";

    public static String life="lifecircle:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        LogUtils.v(TAG, life+"onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.v(TAG, life+"onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtils.v(TAG, life+"onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.v(TAG, life+"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.v(TAG, life+"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.v(TAG, life+"onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.v(TAG, life+"onDestroy");
    }

    public View getContentView() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);
        return content.getChildAt(0);
    }

    protected void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void startActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void startActivity(Class<?> clazz, int magic) {
        Intent intent = new Intent(this, clazz);
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_MAGIC, magic);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void startActivity(Class<?> clazz, String magic) {
        Intent intent = new Intent(this, clazz);
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_MAGIC, magic);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void startActivityForResult(Class<?> clazz, int requestCode) {
        Intent intent = new Intent(this, clazz);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
        // Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, requestCode);
    }

    protected void startActivityForResult(Class<?> clazz, int requestCode, int magic) {
        Intent intent = new Intent(this, clazz);
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_MAGIC, magic);
        intent.putExtras(bundle);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
        // Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, requestCode);
    }

    protected void startActivityForResult(Class<?> clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
        // Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                hideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    protected void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

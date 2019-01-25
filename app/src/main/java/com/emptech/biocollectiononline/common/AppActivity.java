package com.emptech.biocollectiononline.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;

import butterknife.ButterKnife;

@SuppressLint("InflateParams")
public abstract class AppActivity extends BaseActivity {

    protected ActionBar mActionBar;

    protected View mBarRootView;

    protected int streamId = 0;

    protected WeakReferenceHandler mWeakReferenceHandler;

    protected final ActionBar.LayoutParams LAYOUT_PARAMS = new ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        EventBus.getDefault().register(this);
//        App.get().Component().inject(this);
        setupTopBar();
        setContentView(getContentViewId());
        ButterKnife.bind(this);
        WeakReferenceHandler.MyHandleMessage myHandleMessage = setHandlerMessage();
        if (myHandleMessage != null) {
            mWeakReferenceHandler = new WeakReferenceHandler(this);
            mWeakReferenceHandler.setHandleMessage(myHandleMessage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    protected abstract int getContentViewId();

    @StringRes
    public abstract int getTitleString();

    protected void setupTopBar() {
        View topBar = getLayoutInflater().inflate(R.layout.topbar, null);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        mActionBar.setCustomView(topBar, LAYOUT_PARAMS);
        Toolbar parent = (Toolbar) topBar.getParent();
        parent.setContentInsetsAbsolute(0, 0);
        parent.setPadding(0, 0, 0, 0);
//        String actionTitle = getTitle().toString();
        if(getTitleString()!=0){
            ((TextView) topBar.findViewById(R.id.actionbar_title)).setText(getTitleString());
        }
//        if (!TextUtils.isEmpty(actionTitle)) {
//            ((TextView) topBar.findViewById(R.id.actionbar_title)).setText(actionTitle);
//        }
        topBar.findViewById(R.id.actionbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    protected void setTitle(String SN) {
        TextView view = (TextView) mBarRootView.findViewById(R.id.title);
        view.setText("SN:" + SN);
    }


    protected void hideActionBar() {
        if (mActionBar == null) {
            mActionBar = getSupportActionBar();
        }
        mActionBar.hide();
    }

    protected void showActionBar() {
        if (mActionBar == null) {
            mActionBar = getSupportActionBar();
        }
        mActionBar.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        finishListener();
        if(mWeakReferenceHandler!=null){
            mWeakReferenceHandler.removeCallbacksAndMessages(this);
            mWeakReferenceHandler=null;
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    @Subscribe
//    public void onEventMainThread(EventMsg event) {
//        switch (event.what) {
//            case EventMsg.EVENT_MSG_TIME_CHANGED:
//                // 时间变化
//                setTitleDataAndTime();
//                break;
//            default:
//                break;
//        }
//    }

    public void finishListener() {

    }

    private void hideNavigate() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 设置handler处理器；
     *
     * @return
     */
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return null;
    }

}

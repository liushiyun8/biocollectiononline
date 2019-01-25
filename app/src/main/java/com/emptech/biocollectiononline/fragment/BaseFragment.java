package com.emptech.biocollectiononline.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.utils.LogUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class BaseFragment extends Fragment {
    public Activity activity;
    protected String TAG;
    private View contentView;
    protected LayoutInflater inflater;
    protected WeakReferenceHandler mWeakReferenceHandler;
    public onFinishListener mfinishListener;
    public PreferencesManager sp;
    private Unbinder bind;


    public interface onFinishListener {
        void requestFinishFragmentListener();
    }

    public void setFinishListener(onFinishListener finishListener) {
        mfinishListener = finishListener;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        activity = getActivity();
        sp=App.get().getmPreferencesManager();
        TAG = getClass().getSimpleName();
        contentView = inflater.inflate(getLayout(), container, false);
        bind = ButterKnife.bind(this, contentView);
        WeakReferenceHandler.MyHandleMessage myHandleMessage = setHandlerMessage();
        if (myHandleMessage != null) {
            mWeakReferenceHandler = new WeakReferenceHandler(activity);
            mWeakReferenceHandler.setHandleMessage(myHandleMessage);
        }
        initView(contentView);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
        bind=null;
        if(mWeakReferenceHandler!=null){
            mWeakReferenceHandler.removeCallbacksAndMessages(null);
            mWeakReferenceHandler=null;
        }
        Log.e(TAG, "onDestroyView");
    }


    protected abstract int getLayout();

    protected abstract void initView(View view);

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(@IdRes int res) {
        return (T) contentView.findViewById(res);
    }

    public void finishListener() {

    }

    public void requestFinishActivity() {
        if (mfinishListener != null) {
            LogUtils.v(TAG,"当前fragment申请销毁Activity");
            mfinishListener.requestFinishFragmentListener();
        }
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

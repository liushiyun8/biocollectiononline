package com.emptech.biocollectiononline.common;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.utils.LogUtils;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public abstract class AppFragmentActivity extends AppBarActivity implements BaseFragment.onFinishListener {
    protected BaseFragment currentFragment;//当前显示的fragment；


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseFragment mBaseFragment = setFragment();
        if (mBaseFragment != null) {
            mBaseFragment.setFinishListener(this);
            switchFragment(mBaseFragment);
        }

    }


    /**
     * 放置Fragment的viewID；
     **/
    protected abstract BaseFragment setFragment();


    @Override
    protected final int getContentViewId() {
        return R.layout.activity_fragment_content;
    }

    /**
     * 切换fragment
     *
     * @param targetFragment 目标Fragment
     */
    private void switchFragment(BaseFragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.addToBackStack(null);
        transaction
                .add(R.id.fragment, targetFragment).commitAllowingStateLoss();
//        if (currentFragment != null) {
//            transaction.hide(currentFragment);
//        }
//        if (!targetFragment.isAdded()) {
//            transaction
//                    .add(R.id.fragment, targetFragment)
//                    .commit();
//        } else {
//            transaction
//                    .show(targetFragment)
//                    .commit();
//        }
        currentFragment = targetFragment;
    }

    @Override
    public void requestFinishFragmentListener() {
    }

    @Override
    public void finish() {
        LogUtils.e(TAG,"finish" );
        if(currentFragment!=null)
            currentFragment.finishListener();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.remove(currentFragment).commitAllowingStateLoss();
        super.finish();
    }
}

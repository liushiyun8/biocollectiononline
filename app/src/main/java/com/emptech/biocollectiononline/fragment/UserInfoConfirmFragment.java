package com.emptech.biocollectiononline.fragment;

import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.dao.DbHelper;
import com.emptech.biocollectiononline.manager.ListUserAdapter;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.utils.BitmapUtil;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class UserInfoConfirmFragment extends BaseSocketFragment {

    @BindView(R.id.listview_userinfo_show)
    ListView listview_userinfo;
    @BindView(R.id.image_user_photo_show)
    ImageView image_user_photo;
    @BindView(R.id.imageview_fingerprint_left_show)
    ImageView imageview_fingerprint_left_show;
    @BindView(R.id.imageview_fingerprint_right_show)
    ImageView imageview_fingerprint_right_show;

    Map<String, String> userInfo;//人物信息；
    private Bitmap userPhoto;//人物照片;
    private Bitmap userLeftFinger;
    private Bitmap userRightFinger;
    ListUserAdapter mListUserAdapter;//人物信息适配列表；
    private final int MSG_UPDATE = 1001;

    @Override
    protected int getLayout() {
        return R.layout.fragment_userinfo_confirm;
    }

    @Override
    protected void initView(View view) {
        mListUserAdapter = new ListUserAdapter(activity);
        listview_userinfo.setAdapter(mListUserAdapter);
        new Thread(findUserInfoRunnable).start();
    }

    private Runnable findUserInfoRunnable = new Runnable() {
        @Override
        public void run() {
            String userID = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
            userInfo = DbHelper.get(App.get()).findUserInfoByUserID(userID);
            if(userInfo!=null){
                userInfo = handleUserMap(userInfo);
                mWeakReferenceHandler.sendEmptyMessage(MSG_UPDATE);
            }
        }
    };

    @OnClick({R.id.btn_confirm_show_user})
    void OnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm_show_user:
                Log.e("TAG","confirm被调用");
                if (mSocketSession != null) {
//                    SessionUserInfoSocket userSocket = new SessionUserInfoSocket();
//                    userSocket.transmitIoSession(mSocketSession.getmIoSession());
                    byte[] success = {0x00};
                    boolean isSend = sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(success));
                    if (isSend) {
                        requestFinishActivity();
                    }
                }
                break;
        }
    }

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                if (isDetached() || isRemoving() || activity.isFinishing()) {
                    return;
                }
                switch (msg.what) {
                    case MSG_UPDATE:
                        mListUserAdapter.setData(userInfo);
                        if (userPhoto != null) {
                            image_user_photo.setImageBitmap(userPhoto);
                        }
                        if (userLeftFinger != null) {
                            imageview_fingerprint_left_show.setImageBitmap(userLeftFinger);
                        }
                        if (userRightFinger != null) {
                            imageview_fingerprint_right_show.setImageBitmap(userRightFinger);
                        }
                        break;
                }
            }
        };
    }

    private Map<String, String> handleUserMap(Map<String, String> map) {
        Map<String, String> newMap = new HashMap<>();
        Set<String> set = map.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            LogUtils.v(TAG, "data[" + key + ":" + map.get(key) + "]");
            switch (key) {
                case AppConfig.PREFERENCE_KEY_LEFTFINGER:
                        userLeftFinger = BitmapUtil.decodeFile(map.get(key));
                    break;
                case AppConfig.PREFERENCE_KEY_RIGHTFINGER:
                        userRightFinger = BitmapUtil.decodeFile(map.get(key));
                    break;
                case AppConfig.PREFERENCE_KEY_PHOTO:
                        userPhoto = BitmapUtil.decodeFile(map.get(key));
                    break;
                default:
                    newMap.put(key, map.get(key));
                    break;
            }
        }
        return newMap;
    }

}

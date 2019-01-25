package com.emptech.biocollectiononline.fragment;

import android.graphics.Bitmap;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.bean.UserInfoMsg;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.dao.DbHelper;
import com.emptech.biocollectiononline.manager.ListUserAdapter;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.socket.message.SessionUserInfoSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.BitmapUtil;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.util.Map;


import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class UserInfoFragment extends BaseSocketFragment {

    public static final String IDKey = "ID";
    @BindView(R.id.listview_userinfo)
    ListView listview_userinfo;
    @BindView(R.id.image_user_photo)
    ImageView image_user_photo;


    Map<String, String> userInfo;//人物信息；
    private byte[] userPhoto;//人物照片;
    ListUserAdapter mListUserAdapter;//人物信息适配列表；
    private final int MSG_UPDATE = 1001;

    @Override
    protected int getLayout() {
        return R.layout.fragment_userinfo;
    }

    @Override
    protected void initView(View view) {
        mListUserAdapter = new ListUserAdapter(activity);
        listview_userinfo.setAdapter(mListUserAdapter);
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
//                            byte[] decodePhoto= Base64.decode(userPhoto, Base64.DEFAULT);
                            Bitmap photo = BitmapUtil.Bytes2Bimap(userPhoto);
                            if (photo != null) {
                                image_user_photo.setImageBitmap(photo);
                            }
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void setSocketSession(SocketSession mSocketSession) {
        super.setSocketSession(mSocketSession);
        if (mSocketSession instanceof SessionUserInfoSocket) {
            userPhoto = null;
            userInfo = null;
            UserInfoMsg mUserInfo = ((SessionUserInfoSocket) mSocketSession).getmUserInfoMsg();
            Map<String, String> userInfo = mUserInfo.getmUserInfo();
            if (mListUserAdapter == null || userInfo.get(IDKey) == null) {
                byte[] failed = {0x01};
                //向服务器返回失败信息；
                sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(failed));
                requestFinishActivity();
                LogUtils.e(TAG, "no user data");
                return;
            }
            this.userInfo = userInfo;
            userPhoto = mUserInfo.getPhotoIconByte();
            if (userPhoto == null) {
                LogUtils.v(TAG, "没有照片信息");
            } else {
                LogUtils.v(TAG, "存在照片信息:图片长度：" + userPhoto.length + "图片数据：" + Converter.BytesToHexString(userPhoto, userPhoto.length));
            }
            mWeakReferenceHandler.sendEmptyMessage(MSG_UPDATE);

        }
    }

    @OnClick({R.id.btn_confirm_user})
    void OnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm_user:
                if (mSocketSession != null) {
                    byte[] success = {0x00};
                    boolean isSend = sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(success));
                    if (isSend) {
                        saveUserInfoToDB();
                        requestFinishActivity();
                    }
                }
                break;
        }
    }

    private boolean saveUserInfoToDB() {
        String UserID = userInfo.get(IDKey);
        LogUtils.v(TAG, "保存ID：" + UserID);
        if (UserID == null) {
            return false;
        }
        userInfo.remove(IDKey);
        PreferencesManager.getIns(App.get()).setStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER, UserID);
        DbHelper.get(App.get()).createIDUserInfo(UserID, userInfo);
        return true;
    }

}

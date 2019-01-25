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
        import com.emptech.biocollectiononline.socket.message.SessionUserAllInfoSocket;
        import com.emptech.biocollectiononline.socket.message.SocketSession;
        import com.emptech.biocollectiononline.utils.BitmapUtil;
        import com.emptech.biocollectiononline.utils.Converter;
        import com.emptech.biocollectiononline.utils.FileUtil;
        import com.emptech.biocollectiononline.utils.LogUtils;

        import java.io.File;
        import java.util.Map;


        import butterknife.BindView;
        import butterknife.OnClick;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class UserInfoShowFragment extends BaseSocketFragment {

    public static final String IDKey = "ID";
    @BindView(R.id.listview_userinfo)
    ListView listview_userinfo;
    @BindView(R.id.image_user_photo)
    ImageView image_user_photo;


    Map<String, String> userInfo;//人物信息;
    private byte[] userPhoto;//人物照片;
    ListUserAdapter mListUserAdapter;//人物信息适配列表；
    private final int MSG_UPDATE = 1001;
    private Bitmap photo;

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
    public void closePreview() {
        super.closePreview();
        activity.finish();
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
//                        mListUserAdapter.setData(userInfo);
                        LogUtils.e(TAG,"userPhoto:"+userPhoto.length );
                        if (userPhoto != null) {
                            photo = BitmapUtil.Bytes2Bimap(userPhoto);
//                            Picasso.with(activity).load(file).memoryPolicy(MemoryPolicy.NO_CACHE).into(image_user_photo);
                            LogUtils.e(TAG,"bytes2Bitmap:"+photo);
                            if (photo != null) {
                                FileUtil.byte2File(userPhoto, AppConfig.WORK_TEMP_PATH, "show.jpg");
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
        if (mSocketSession instanceof SessionUserAllInfoSocket) {
            userPhoto = null;
            userInfo = null;
            UserInfoMsg mUserInfo = ((SessionUserAllInfoSocket) mSocketSession).getUserInfoMsg();
            Map<String, String> userInfo = mUserInfo.getmUserInfo();
//            if (mListUserAdapter == null || userInfo.get(IDKey) == null) {
//                byte[] failed = {0x01};
//                //向服务器返回失败信息；
//                sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(failed));
//                requestFinishActivity();
//                LogUtils.e(TAG, "没有用户ID信息,直接申请失败");
//                return;
//            }
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

    @OnClick({R.id.btn_confirm_user,R.id.btn_cancle_user})
    void OnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm_user:
                if (mSocketSession != null) {
                    byte[] success = {0x00};
                    if(photo==null)
                        success[0] = 0x01;
                    boolean isSend = sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(success));
                    if (isSend) {
//                        saveUserInfoToDB();
                        requestFinishActivity();
                    }
                }
                break;
            case R.id.btn_cancle_user:
                if (mSocketSession != null) {
                    byte[] success = {0x01};
                    boolean isSend = sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(success));
                    if (isSend) {
//                        saveUserInfoToDB();
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


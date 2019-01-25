package com.emptech.biocollectiononline.fragment;

import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.activity.ModePhotoActivity;
import com.emptech.biocollectiononline.activity.UserInfoConfirmActivity;
import com.emptech.biocollectiononline.bean.IDPictureTable;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.dao.DbHelper;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.SocketSessionManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.socket.message.IPreview;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.BitmapUtil;
import com.emptech.biocollectiononline.utils.LogUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by linxiaohui on 2018/1/15.
 */

public class SelectPhotoFragment extends BaseSocketFragment implements IPreview {
    private Map<String, Bitmap> mData;
    private final static int MSG_INIT_PICTURE = 1001;
    @BindViews({R.id.imageview_photo_select_1, R.id.imageview_photo_select_2, R.id.imageview_photo_select_3})
    List<ImageView> ImageViewPhoto;
    Map<Integer, String> ImageViewNumber = new HashMap<>();

    @BindView(R.id.btn_select_confirm)
    Button btn_select_confirm;
    @BindView(R.id.btn_reshoot)
    Button btn_reshoot;
    @BindView(R.id.imageview_photo_show)
    ImageView imageview_photo_show;

    String successNumber = null;

    String userId;//用户ID；

    @Override
    public boolean isInitPreviewMode(SocketSession mSocketSession) {
        return mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Photo;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_selectphoto;
    }

    @Override
    protected void initView(View view) {
        setIPreviewListener(this);
        userId = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mData = findPhotoTable();
                mWeakReferenceHandler.sendEmptyMessage(MSG_INIT_PICTURE);
            }
        }).start();
    }

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_INIT_PICTURE:
                        int index = 0;
                        if (mData == null || mData.size() == 0) {
                            return;
                        }
                        LogUtils.v(TAG, "图片总共数量：" + mData.size());
                        for (String key : mData.keySet()) {
                            ImageViewPhoto.get(index).setImageBitmap(mData.get(key));
                            ImageViewNumber.put(ImageViewPhoto.get(index).getId(), key);
                            index++;
                        }
                        if (mData.size() >= 3) {
                            btn_reshoot.setClickable(false);
                        }
                        selectPhoto(R.id.imageview_photo_select_1);
                        break;
                }
            }
        };
    }

    private Map<String, Bitmap> findPhotoTable() {
        List<IDPictureTable> idPictureTables = DbHelper.get(App.get()).findPhotoInfoByUserID(userId);
        if (idPictureTables == null) {
            return null;
        }
        Map<String, Bitmap> map = new HashMap<>();
        for (int i = 0; i < idPictureTables.size(); i++) {
                Bitmap bitmap = BitmapUtil.decodeFile(idPictureTables.get(i).getmIDPictureValue());
                String number = idPictureTables.get(i).getmIDPictureNumber();
                map.put(number, bitmap);
        }
        return map;
    }

    @OnClick({R.id.imageview_photo_select_1, R.id.imageview_photo_select_2, R.id.imageview_photo_select_3, R.id.btn_reshoot, R.id.btn_select_confirm})
    void OnClick(View view) {
        switchSelectPhoto(view.getId());
    }

    private void switchSelectPhoto(int resouceID) {

        switch (resouceID) {
            case R.id.imageview_photo_select_1:
            case R.id.imageview_photo_select_2:
            case R.id.imageview_photo_select_3:
                selectPhoto(resouceID);
                break;
            case R.id.btn_reshoot:
                activity.finish();
                break;
            case R.id.btn_select_confirm:
                if (DbHelper.get(App.get()).updateUsePhotoInfoByUserID(userId, successNumber)) {
                    if (sendPhotoConfirm()) {
                        //更新数据库
                        App.get().finishActivity(ModePhotoActivity.class);
                        LogUtils.v(TAG, "发送确认信息成功！");
                    } else {
                        LogUtils.v(TAG, "发送确认信息失败！");
                    }
                } else {
//                    LogUtils.e(TAG, "数据库更新信息失败！");
                }

                break;
        }
    }

    private void selectPhoto(int resouceID) {
        imageview_photo_show.setImageBitmap(null);
        for (int i = 0; i < ImageViewPhoto.size(); i++) {
            ImageViewPhoto.get(i).setSelected(false);
        }
        String successNumberTemp = ImageViewNumber.get(resouceID);
        if (successNumber != null) {
            activity.findViewById(resouceID).setSelected(true);
            Bitmap showBitmap = mData.get(successNumber);
            if (showBitmap != null) {
                imageview_photo_show.setImageBitmap(showBitmap);
                successNumber = successNumberTemp;
            }
        }
    }

    private boolean sendPhotoConfirm() {
        String Number = successNumber;
        if (TextUtils.isEmpty(Number)) {
            LogUtils.v(TAG, "编号是空的");
            return false;
        }
        LogUtils.v(TAG, "编号：" + Number + "发送确认信息中。。。。");
        return sendConfirmDataToPC(MessageType.TYPE_MODE.Photo, Number);
    }

    @Override
    protected void ConfirmResult(byte mode, boolean isSuccess) {
        super.ConfirmResult(mode, isSuccess);
        LogUtils.v(TAG, "mode");
        if (mode == MessageType.TYPE_MODE.Photo) {
            if (isSuccess) {
                SocketSessionManager.getInstance().putSocketSession(UserInfoConfirmActivity.class, mSocketSession);
                requestFinishActivity();
            }
        }
    }

    @Override
    public boolean initHardWare() {
        return true;
    }

    @Override
    public byte[] getPreviewData(SocketSession socketSession) {
        return new byte[1];
    }

    @Override
    public byte[] collection(byte runningMode) {
        return new byte[1];
    }
}

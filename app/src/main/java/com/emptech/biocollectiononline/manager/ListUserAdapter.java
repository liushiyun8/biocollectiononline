package com.emptech.biocollectiononline.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * 用户信息列表适配器；
 */

public class ListUserAdapter extends BaseAdapter {
    private LayoutInflater mInflater = null;
    private Map<String, String> data;
    private ArrayList<String> titleList = new ArrayList<>();

    public ListUserAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public synchronized void setData(Map<String, String> data) {
        this.data = data;
        titleList.clear();
        for (String title : data.keySet()) {
            titleList.add(title);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (data == null) return 0;
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return titleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_userinfo, null);
            holder.tv_title = (TextView) convertView.findViewById(R.id.list_tv_user_title);
            holder.tv_content = (TextView) convertView.findViewById(R.id.list_tv_user_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String userkey = getItem(position);
        String userValue = data.get(userkey);
        LogUtils.v(AppConfig.MODULE_APP, "显示信息:[" + userkey + ":" + userValue + "]");
        holder.tv_title.setText(userkey);
        holder.tv_content.setText(userValue);
        return convertView;
    }

    static class ViewHolder {
        public TextView tv_title;
        public TextView tv_content;

    }

}

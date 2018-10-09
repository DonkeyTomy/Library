package com.tomy.lib.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomy.lib.ui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import timber.log.Timber;

/**@author Tomy
 * Created by Tomy on 2014/6/15.
 */
public class CaptureRecordPopListAdapter extends BaseAdapter {
    private static final String TAG = "RecordResolutionPopListAdapter: ";
    private CharSequence[] mEntries;
    private LayoutInflater mInflater;
    private HashMap<Integer, Boolean> mIsSelectedMap;
    private boolean mNeedIv;
    private CharSequence[] mValueList = null;
    private Context mContext;
    private List<Drawable> mIconList;
    private boolean mIsCaptureSetting;

    public CaptureRecordPopListAdapter(Context context, CharSequence[] entries, int entryValuesId, int iconListId, boolean needIv, boolean captureSetting) {
        mContext = context;
        mEntries = entries;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIsSelectedMap = new HashMap<>();
        mNeedIv = needIv;
        mIsCaptureSetting = captureSetting;
        mIconList = new ArrayList<>();
        refreshIconList(iconListId);
        if (entryValuesId != 0) {
            mValueList = mContext.getResources().getTextArray(entryValuesId);
        }
    }

    /**刷新ListView每个Item里的图标.
     * @param iconId 图标列表Id.为R.array.***格式 若为0,则表示不需要图标.
     * */
    private void refreshIconList(int iconId) {
        Timber.e("refreshIconList");
        if (iconId != 0) {
            if (!mIconList.isEmpty())
                mIconList.clear();
            TypedArray typedArray = mContext.getResources().obtainTypedArray(iconId);
            for (int i = 0; i < mEntries.length; i++) {
                mIconList.add(typedArray.getDrawable(i));
            }
            Timber.e("refreshIconList.size = %s", mIconList.size());
            typedArray.recycle();
        }
    }

    public void setData(CharSequence[] entries, int iconId, int valueId) {
        setData(entries, iconId);
        mValueList = mContext.getResources().getTextArray(valueId);
        Timber.e("setData()");
        /*for (CharSequence entry : mEntries) {
            Timber.e(TAG, "entry = " + entry);
        }*/
    }

    public void setData(CharSequence[] entries, int iconId) {
        mEntries = entries;
        refreshIconList(iconId);
        notifyDataSetChanged();
    }

    public String getValue(int position) {
        if (mValueList == null)
            return "";
        return String.valueOf(mValueList[position]);
    }

    public int getIndex(String value) {
        int index = 0;
        for (; index < mEntries.length; index++) {
        }
        return index;

    }

    public Drawable getItemIcon(int position) {
        return mIconList.get(position);
    }

    public void setItemIcon(int position, Drawable itemIcon) {
        if (mIconList == null) {
            return;
        }
        Timber.e("mIconList = %s", mIconList.size());
        mIconList.remove(position);
        mIconList.add(position, itemIcon);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        CheckBox mCheckBox  = null;
        TextView mTextView  = null;
        ImageView mIvIcon   = null;
    }

    public void setCheck(int position, boolean checked) {
        Timber.e("setCheck().position = " + position + ":" + checked);
        mIsSelectedMap.clear();
        mIsSelectedMap.put(position, checked);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEntries != null ? mEntries.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return mEntries != null ? mEntries[position] : "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            if (!mIsCaptureSetting) {
                convertView = mInflater.inflate(R.layout.layout_record_popu_item, parent, false);
            } else {
                convertView = mInflater.inflate(R.layout.layout_record_popu_item_pic_setting, parent, false);
            }
            viewHolder = new ViewHolder();
            viewHolder.mCheckBox    = convertView.findViewById(R.id.cb_record_popup_window_item);
            viewHolder.mTextView    = convertView.findViewById(R.id.tv_record_popup_window_item);
            viewHolder.mIvIcon      = convertView.findViewById(R.id.iv_list_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mTextView.setText(mEntries[position]);
        Boolean object = mIsSelectedMap.get(position);
        boolean checked = false;
        if (object != null) {
            checked = object;
        }
        viewHolder.mCheckBox.setChecked(checked);
        if (mNeedIv && mIconList != null && mIconList.size() > position) {
            viewHolder.mIvIcon.setVisibility(View.VISIBLE);
            viewHolder.mIvIcon.setBackground(mIconList.get(position));
        } else {
            viewHolder.mIvIcon.setVisibility(View.GONE);
        }
        return convertView;
    }
}

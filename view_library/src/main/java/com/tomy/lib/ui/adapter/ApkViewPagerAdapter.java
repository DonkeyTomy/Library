package com.tomy.lib.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomy.lib.ui.R;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2015-01-06.
 */
public class ApkViewPagerAdapter extends PagerAdapter {
    private List<Integer> mInfoList;
    private List<Integer> mIconList;
    private Context mContext;
    private LayoutInflater mInflater;
    private AdapterView.OnItemClickListener mListener;
    private List<List<String>> mNameList;
    private List<List<Drawable>> mDrawableList;
    private List<String> nameList;
    private List<Drawable> iconList;
    private AdapterView.OnItemLongClickListener mItemLongClickListener;

    public ApkViewPagerAdapter(Context context) {
        mContext    = context;
        mInflater   = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListener   = listener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void addIconList(int iconListId) {
        if (mIconList == null)
            mIconList = new ArrayList<>();
        mIconList.add(iconListId);
    }

    public void addInfoList(int infoListId) {
        if (mInfoList == null)
            mInfoList = new ArrayList<>();
        mInfoList.add(infoListId);
    }

    public void addName(String name) {
        if (mNameList == null) {
            mNameList   = new ArrayList<>();
        }
        if (nameList == null) {
            nameList    = new ArrayList<>();
        }
        nameList.add(name);
        if (nameList.size() == 12) {
            mNameList.add(nameList);
            nameList = null;
        }
    }

    public void addIcon(Drawable icon) {
        if (mDrawableList == null) {
            mDrawableList = new ArrayList<>();
        }
        if (iconList == null) {
            iconList    = new ArrayList<>();
        }
        iconList.add(icon);
        if (iconList.size() == 12) {
            mDrawableList.add(iconList);
            iconList = null;
        }
    }

    public void addFinish() {
        if (iconList != null && iconList.size() < 12) {
            mDrawableList.add(iconList);
            mNameList.add(nameList);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GridView gridView   = (GridView) mInflater.inflate(R.layout.grid_view, container, false);
        gridView.setSelector(new ColorDrawable(android.R.color.transparent));
        GridViewAdapter adapter;
        if (mIconList != null) {
            adapter = new GridViewAdapter(mIconList.get(position), mInfoList.get(position));
        } else {
            adapter = new GridViewAdapter(mNameList.get(position), mDrawableList.get(position));
        }
        gridView.setAdapter(adapter);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);
        if (mListener != null)
            gridView.setOnItemClickListener(mListener);
        gridView.setHorizontalSpacing(1);
        gridView.setVerticalSpacing(20);
        if (mItemLongClickListener != null)
            gridView.setOnItemLongClickListener(mItemLongClickListener);
        container.addView(gridView);
        return gridView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mIconList != null ? mIconList.size() : mDrawableList != null ? mDrawableList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void removeItem(int index, int current) {
        mDrawableList.get(index).remove(current);
        mNameList.get(index).remove(current);
    }

    public class GridViewAdapter extends BaseAdapter {
        private String[] mInfoArray;
        private TypedArray mIconArray;
        private List<String> mInfoList;
        private List<Drawable> mIconList;

        public GridViewAdapter(int infoId, int iconId) {
            Resources res   = mContext.getResources();
            mInfoArray  = res.getStringArray(infoId);
            mIconArray  = res.obtainTypedArray(iconId);
        }

        public GridViewAdapter(List<String> infoList, List<Drawable> iconList) {
            mInfoList   = infoList;
            mIconList   = iconList;
        }

        class ViewHolder {
            ImageView mIvIcon;
            TextView mTvInfo;
        }

        @Override
        public int getCount() {
            return mIconArray != null ? mInfoArray.length : mIconList.size();
        }

        @Override
        public String getItem(int position) {
            return mInfoArray[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.apk_grid_item_container, parent, false);
                holder  = new ViewHolder();
                holder.mIvIcon  = (ImageView) convertView.findViewById(R.id.icon);
                holder.mTvInfo  = (TextView) convertView.findViewById(R.id.info);
                convertView.setTag(holder);
            } else {
                holder  = (ViewHolder) convertView.getTag();
            }
            if (mIconArray != null) {
                holder.mTvInfo.setText(mInfoArray[position]);
                holder.mIvIcon.setImageDrawable(mIconArray.getDrawable(position));
            } else {
                holder.mTvInfo.setText(mInfoList.get(position));
                holder.mIvIcon.setImageDrawable(mIconList.get(position));
            }
            return convertView;
        }
    }
}

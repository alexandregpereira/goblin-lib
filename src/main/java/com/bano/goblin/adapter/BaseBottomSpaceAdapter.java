package com.bano.goblin.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bano.goblin.R;

import java.util.ArrayList;

/**
 *
 * Created by Alexandre on 30/05/2017.
 */

public abstract class BaseBottomSpaceAdapter<T, E extends ViewDataBinding> extends BaseAdapter<T, E> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_BOTTOM = 2;

    public BaseBottomSpaceAdapter(Context context, ArrayList<T> items, int layoutRes, OnClickListener<T> listener){
        super(context, items, layoutRes, listener);
    }

    @Override
    public BaseAdapter.ViewHolder<T, E> onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            E binding = DataBindingUtil.inflate(layoutInflater, getLayoutRes(), parent, false);
            return new BaseAdapter.ViewHolder<>(binding, getListener());
        }
        else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_bottom_space, parent, false);
            return new BaseAdapter.ViewHolder<>(view);
        }
    }

    @Override
    public void onBindViewHolder(BaseAdapter.ViewHolder<T, E> holder, int position) {
        if(getItemViewType(position) == TYPE_BOTTOM) return;
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionBottom(position))
            return TYPE_BOTTOM;

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    private boolean isPositionBottom(int position) {
        return getItems().size() == position;
    }
}

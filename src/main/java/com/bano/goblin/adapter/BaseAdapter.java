package com.bano.goblin.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Alexandre on 13/05/2017.
 */

public abstract class BaseAdapter<T, E extends ViewDataBinding> extends RecyclerView.Adapter<BaseAdapter.ViewHolder<T, E>>{

    private final int mLayoutRes;
    private final OnClickListener<T> mListener;
    private final Resources mResources;
    private ArrayList<T> mItems;

    protected abstract void onBindViewHolder(E e, T t);

    public interface OnClickListener<T>{
        void onClicked(T t);
    }

    public BaseAdapter(ArrayList<T> items, int layoutRes, OnClickListener<T> listener){
        mLayoutRes = layoutRes;
        mItems = items;
        mListener = listener;
        mResources = null;
    }

    public BaseAdapter(Context context, ArrayList<T> items, int layoutRes, OnClickListener<T> listener){
        mLayoutRes = layoutRes;
        mItems = items;
        mListener = listener;
        mResources = context.getResources();
    }

    @Override
    public ViewHolder<T, E> onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        E binding = DataBindingUtil.inflate(layoutInflater, mLayoutRes, parent, false);
        return new ViewHolder<>(binding, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder<T, E> holder, int position) {
        T t = mItems.get(position);
        holder.binding.getRoot().setTag(t);
        this.onBindViewHolder(holder.binding, t);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(ViewHolder<T, E> holder, int position, List<Object> payloads) {
        if(payloads != null && !payloads.isEmpty()){
            // update the specific view
            T t = (T) payloads.get(0);
            holder.binding.getRoot().setTag(t);
            this.onBindViewHolder(holder.binding, t);
        }else{
            // I have already overridden  the other onBindViewHolder(ViewHolder, int)
            // The method with 3 arguments is being called before the method with 2 args.
            // so calling super will call that method with 2 arguments.
            super.onBindViewHolder(holder,position,payloads);
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public void replace(T t) {
        int i = mItems.indexOf(t);
        if(i >= 0){
            mItems.set(i, t);
            notifyItemChanged(i, t);
        }
    }

    public void remove(T t) {
        mItems.remove(t);
        notifyDataSetChanged();
    }

    public void setItems(ArrayList<T> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public void setItem(T t) {
        int i = mItems.indexOf(t);
        if(i >= 0){
            mItems.set(i, t);
        }
        else this.mItems.add(t);
        notifyItemChanged(i, t);
    }

    public ArrayList<T> getItems(){
        return mItems;
    }

    public Resources getResources(){
        return mResources;
    }

    int getLayoutRes(){
        return mLayoutRes;
    }

    protected OnClickListener<T> getListener(){
        return mListener;
    }

    static class ViewHolder<T, E extends ViewDataBinding> extends RecyclerView.ViewHolder{
        public final E binding;

        ViewHolder(E binding, final OnClickListener<T> listener){
            super(binding.getRoot());
            this.binding = binding;
            if(listener != null) {
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onClick(View view) {
                        T t = (T) view.getTag();
                        listener.onClicked(t);
                    }
                });
            }
        }

        ViewHolder(View view){
            super(view);
            this.binding = null;
        }
    }
}

package com.zerogerc.photoframe.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.yandex.disk.client.ListItem;

import java.util.List;

/**
 * Base adapter for showing files in the RecyclerView
 */
public class BaseAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    protected List<ListItem> list;
    protected Activity activity;

    public BaseAdapter(Activity activity, List<ListItem> list) {
        this.list = list;
        this.activity = activity;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.refresh(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Append given entity to the end of the list.
     * Changes will be shown on UI.
     * @param entity given entity
     */
    public void append(ListItem entity) {
        list.add(entity);
        notifyItemInserted(list.size() - 1);
    }
}

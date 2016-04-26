package com.zerogerc.photoframe.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.zerogerc.photoframe.HierarchyEntity;

import java.util.List;

/**
 * Base adapter for showing files in the RecyclerView
 */
public class BaseAdapter extends RecyclerView.Adapter<EntityViewHolder> {
    protected List<HierarchyEntity> list;
    protected Activity activity;

    public BaseAdapter(Activity activity, List<HierarchyEntity> list) {
        this.list = list;
        this.activity = activity;
    }

    @Override
    public EntityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EntityViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(EntityViewHolder holder, int position) {
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
    public void append(HierarchyEntity entity) {
        list.add(entity);
        notifyItemInserted(list.size() - 1);
    }
}

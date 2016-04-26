package com.zerogerc.photoframe.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zerogerc.photoframe.HierarchyEntity;
import com.zerogerc.photoframe.R;

/**
 * Base holder for Adapter
 */
public class EntityViewHolder extends RecyclerView.ViewHolder {
    private TextView title;

    public EntityViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_hierarchy_entity, parent, false));

        title = ((TextView) itemView.findViewById(R.id.hierarchy_entity_title));
    }

    /**
     * Load all info from {@link HierarchyEntity} to layout
     * @param item {@link HierarchyEntity} to load on layout
     */
    public void refresh(HierarchyEntity item) {
        if (title != null) {
            title.setText(item.getTitle());
        }
    }
}

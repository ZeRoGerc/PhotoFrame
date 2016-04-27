package com.zerogerc.photoframe.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.R;

/**
 * Base holder for Adapter
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {
    private TextView title;

    public ItemViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_hierarchy_entity, parent, false));

        title = ((TextView) itemView.findViewById(R.id.hierarchy_entity_title));
    }

    /**
     * Load all info from {@link ListItem} to layout
     * @param item {@link ListItem} to load on layout
     */
    public void refresh(ListItem item) {
        if (title != null) {
            title.setText(item.getDisplayName());
        }
    }
}

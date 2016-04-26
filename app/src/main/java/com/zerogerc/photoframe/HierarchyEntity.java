package com.zerogerc.photoframe;

import com.yandex.disk.client.ListItem;

/**
 * Entity in the hierarchy of disk.
 * For example: file, image, folder...
 */
public class HierarchyEntity {
    private final String title;

    public HierarchyEntity(ListItem item) {
        title = item.getName();
    }

    public HierarchyEntity(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

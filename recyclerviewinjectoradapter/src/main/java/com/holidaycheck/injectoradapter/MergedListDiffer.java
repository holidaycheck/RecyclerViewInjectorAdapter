package com.holidaycheck.injectoradapter;

import android.support.v7.util.DiffUtil;

import java.util.Collections;
import java.util.List;

class MergedListDiffer extends DiffUtil.Callback {

    private List<MergedItem> oldList = Collections.emptyList();
    private List<MergedItem> newList = Collections.emptyList();

    void updateData(List<MergedItem> oldList, List<MergedItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        MergedItem oldRow = oldList.get(oldItemPosition);
        MergedItem newRow = newList.get(newItemPosition);

        return oldRow.type == newRow.type
            && oldRow.id == newRow.id;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //always assume that content are the same,
        // because we don't use this class to detect content updates
        return true;
    }
}

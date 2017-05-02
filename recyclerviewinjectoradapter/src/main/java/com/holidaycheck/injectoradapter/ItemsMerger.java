package com.holidaycheck.injectoradapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Merges items from two sources into the List with proper order.
 * <p>
 * There are two types of items:
 * injected items - items that are injected into the final list on concrete position
 * child items - items that position changes in order to satisfy injected items positions
 * </p>
 */
class ItemsMerger {

    private ItemsDataProvider itemsDataProvider;
    private MergeOptionsProvider mergeOptionsProvider;

    private MergeStrategy hideInjectedItemsStrategy = new HideInjectedItemsStrategy();
    private MergeStrategy showOnEmptyStrategy = new ShowInjectedItemsOnEmptyChildItemsStrategy();
    private MergeStrategy hideOnEmptyStrategy = new HideInjectedItemsOnEmptyChildItemsStrategy();
    private MergeStrategy showOnNonEmptyStrategy = new ShowInjectedItemsOnNonEmptyChildItemsStrategy();
    private MergeStrategy showOnLowerPositionOnNonEmptyStrategy = new ShowInjectedItemsWithLowerPositionOnNonEmptyChildItemsStrategy();

    ItemsMerger(ItemsDataProvider itemsDataProvider, MergeOptionsProvider mergeOptionsProvider) {
        this.itemsDataProvider = itemsDataProvider;
        this.mergeOptionsProvider = mergeOptionsProvider;
    }

    List<MergedItem> mergeItems() {
        MergeStrategy mergeStrategy = getMergeStrategy();
        return mergeStrategy.mergeItems();
    }

    private MergeStrategy getMergeStrategy() {
        if (mergeOptionsProvider.shouldShowInjectedViews()) {
            if (itemsDataProvider.getChildItemCount() == 0) {
                if (mergeOptionsProvider.shouldShowInjectedItemsOnEmptyChildItems()) {
                    return showOnEmptyStrategy;
                } else {
                    return hideOnEmptyStrategy;
                }
            } else if (mergeOptionsProvider.shouldShowInjectedItemsOnLowerPosition()) {
                return showOnLowerPositionOnNonEmptyStrategy;
            } else {
                return showOnNonEmptyStrategy;
            }
        } else {
            return hideInjectedItemsStrategy;
        }
    }

    private class HideInjectedItemsStrategy implements MergeStrategy {

        @Override
        public List<MergedItem> mergeItems() {
            List<MergedItem> result = new ArrayList<>(itemsDataProvider.getChildItemCount());
            for (int i = 0; i < itemsDataProvider.getChildItemCount(); i++) {
                result.add(MergedItem.newRow(itemsDataProvider.getChildItemTypeAtPosition(i), itemsDataProvider.getChildItemIdAtPosition(i)));
            }
            return result;
        }
    }

    private class ShowInjectedItemsOnEmptyChildItemsStrategy implements MergeStrategy {

        @Override
        public List<MergedItem> mergeItems() {
            List<MergedItem> result = new ArrayList<>(itemsDataProvider.getInjectedItemCount());
            for (int i = 0; i < itemsDataProvider.getInjectedItemCount(); i++) {
                int injectedViewType = itemsDataProvider.getInjectedItemTypeAtIndex(i);
                result.add(MergedItem.newInjectedRow(injectedViewType));
            }
            return result;
        }
    }

    private class HideInjectedItemsOnEmptyChildItemsStrategy implements MergeStrategy {

        @Override
        public List<MergedItem> mergeItems() {
            return Collections.emptyList();
        }
    }

    private class ShowInjectedItemsOnNonEmptyChildItemsStrategy implements MergeStrategy {

        @Override
        public List<MergedItem> mergeItems() {
            int childAdapterItemCount = itemsDataProvider.getChildItemCount();
            int resultSize = childAdapterItemCount + itemsDataProvider.getInjectedItemCountBeforePosition(childAdapterItemCount);
            List<MergedItem> result = new ArrayList<>(resultSize);

            int position = 0;
            int childPosition = 0;
            while (position < resultSize) {
                if (itemsDataProvider.isItemInjectedOnPosition(position)) {
                    int injectedViewType = itemsDataProvider.getInjectedItemTypeAtPosition(position);
                    result.add(MergedItem.newInjectedRow(injectedViewType));
                } else {
                    result.add(MergedItem.newRow(itemsDataProvider.getChildItemTypeAtPosition(childPosition), itemsDataProvider.getChildItemIdAtPosition(childPosition)));
                    childPosition++;
                }
                position++;
            }

            return result;
        }
    }

    private class ShowInjectedItemsWithLowerPositionOnNonEmptyChildItemsStrategy implements MergeStrategy {

        private ShowInjectedItemsOnNonEmptyChildItemsStrategy regularStrategy;

        ShowInjectedItemsWithLowerPositionOnNonEmptyChildItemsStrategy() {
            this.regularStrategy = new ShowInjectedItemsOnNonEmptyChildItemsStrategy();
        }

        @Override
        public List<MergedItem> mergeItems() {
            List<MergedItem> result = regularStrategy.mergeItems();
            int childAdapterCount = itemsDataProvider.getChildItemCount();
            for (int i = itemsDataProvider.getInjectedItemCountBeforePosition(childAdapterCount); i < itemsDataProvider.getInjectedItemCount(); i++) {
                int injectedViewType = itemsDataProvider.getInjectedItemTypeAtIndex(i);
                result.add(MergedItem.newInjectedRow(injectedViewType));
            }
            return result;
        }
    }

    interface ItemsDataProvider {

        int getChildItemCount();

        int getInjectedItemCount();

        int getChildItemTypeAtPosition(@ChildPosition int position);

        int getInjectedItemTypeAtPosition(int position);

        int getInjectedItemTypeAtIndex(int index);

        long getChildItemIdAtPosition(@ChildPosition int position);

        int getInjectedItemCountBeforePosition(int position);

        boolean isItemInjectedOnPosition(int position);
    }

    interface MergeOptionsProvider {
        boolean shouldShowInjectedItemsOnEmptyChildItems();

        boolean shouldShowInjectedItemsOnLowerPosition();

        boolean shouldShowInjectedViews();
    }

    private interface MergeStrategy {
        List<MergedItem> mergeItems();
    }

}


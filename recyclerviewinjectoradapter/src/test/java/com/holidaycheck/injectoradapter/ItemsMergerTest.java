package com.holidaycheck.injectoradapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ItemsMergerTest {

    @Test
    public void hidesInjectedItemsWhenNeeded() {
        Item Injected_0_1 = createInjectedItem(0, 0);
        Item Injected_5_1 = createInjectedItem(5, 1);

        List<Item> childItems = createChildItem(1, 1, 50);

        ItemsMerger itemsMerger = new ItemsMerger(
            createItemsDataProvider(Arrays.asList(Injected_0_1, Injected_5_1), childItems),
            createMergeOptions(true, true, false)
        );

        List<MergedItem> mergedItems = itemsMerger.mergeItems();

        assertEquals(createMergedItemsList(childItems), mergedItems);
    }

    @Test
    public void showsInjectedItemsOnEmptyChildItemsIfAllowed() {
        Item Injected_0_1 = createInjectedItem(0, 0);
        Item Injected_5_1 = createInjectedItem(5, 1);

        ItemsMerger itemsMerger = new ItemsMerger(
            createItemsDataProvider(Arrays.asList(Injected_0_1, Injected_5_1), Collections.<Item>emptyList()),
            createMergeOptions(true, false, true)
        );

        assertEquals(itemsMerger.mergeItems(), createMergedItemsList(Arrays.asList(Injected_0_1, Injected_5_1)));
    }

    @Test
    public void hidesInjectedItemsOnEmptyChildItemsIfNotAllowed() {
        Item Injected_0_1 = createInjectedItem(0, 0);
        Item Injected_5_1 = createInjectedItem(5, 1);

        ItemsMerger itemsMerger = new ItemsMerger(
            createItemsDataProvider(Arrays.asList(Injected_0_1, Injected_5_1), Collections.<Item>emptyList()),
            createMergeOptions(false, false, true)
        );

        assertThat(itemsMerger.mergeItems().isEmpty(), is(true));
    }

    @Test
    public void showsInjectedItemsOnCorrectPositionOnNonEmptyChildItems() {
        Item Injected_0_1 = createInjectedItem(0, 0);
        Item Injected_5_1 = createInjectedItem(5, 1);

        List<Item> childItems = createChildItem(1, 1, 50);

        ItemsMerger itemsMerger = new ItemsMerger(
            createItemsDataProvider(Arrays.asList(Injected_0_1, Injected_5_1), childItems),
            createMergeOptions(false, false, true)
        );

        List<MergedItem> mergedItems = itemsMerger.mergeItems();
        childItems.add(Injected_0_1.position, Injected_0_1);
        childItems.add(Injected_5_1.position, Injected_5_1);

        assertEquals(createMergedItemsList(childItems), mergedItems);
    }

    @Test
    public void hidesInjectedItemsOnHigherPositionOnNonEmptyChildItemsIfNotAllowed() {
        Item Injected_0_1 = createInjectedItem(0, 0);
        Item Injected_5_1 = createInjectedItem(5, 1);
        Item Injected_5_2 = createInjectedItem(60, 2);

        List<Item> childItems = createChildItem(1, 1, 50);

        ItemsMerger itemsMerger = new ItemsMerger(
            createItemsDataProvider(Arrays.asList(Injected_0_1, Injected_5_1, Injected_5_2), childItems),
            createMergeOptions(false, false, true)
        );

        List<MergedItem> mergedItems = itemsMerger.mergeItems();
        childItems.add(Injected_0_1.position, Injected_0_1);
        childItems.add(Injected_5_1.position, Injected_5_1);

        assertEquals(createMergedItemsList(childItems), mergedItems);
    }

    @Test
    public void showsInjectedItemsOnHigherPositionOnNonEmptyChildItemsIfAllowed() {
        Item Injected_0_1 = createInjectedItem(0, 0);
        Item Injected_5_1 = createInjectedItem(5, 1);
        Item Injected_5_2 = createInjectedItem(60, 2);

        List<Item> childItems = createChildItem(1, 1, 50);

        ItemsMerger itemsMerger = new ItemsMerger(
            createItemsDataProvider(Arrays.asList(Injected_0_1, Injected_5_1, Injected_5_2), childItems),
            createMergeOptions(false, true, true)
        );

        List<MergedItem> mergedItems = itemsMerger.mergeItems();
        childItems.add(Injected_0_1.position, Injected_0_1);
        childItems.add(Injected_5_1.position, Injected_5_1);
        childItems.add(Injected_5_2);

        assertEquals(createMergedItemsList(childItems), mergedItems);
    }

    private class Item {
        final int position;
        final int type;
        final long id;
        final boolean injected;

        private Item(int position, int type, long id, boolean injected) {
            this.position = position;
            this.type = type;
            this.id = id;
            this.injected = injected;
        }
    }

    private List<MergedItem> createMergedItemsList(List<Item> items) {
        List<MergedItem> result = new ArrayList<>();
        for (Item item : items) {
            result.add(item.injected ? MergedItem.newInjectedRow(item.type) : MergedItem.newRow(item.type, item.id));
        }
        return result;
    }

    private Item createInjectedItem(int position, int type) {
        return new Item(position, Integer.MAX_VALUE - type, Integer.MAX_VALUE - type, true);
    }

    private List<Item> createChildItem(int startType, int startId, int count) {
        List<Item> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(new Item(i, startType + i, startId + i, false));
        }
        return result;
    }

    private ItemsMerger.ItemsDataProvider createItemsDataProvider(final List<Item> injectedItems, final List<Item> childItems) {
        return new ItemsMerger.ItemsDataProvider() {
            @Override
            public int getChildItemCount() {
                return childItems.size();
            }

            @Override
            public int getInjectedItemCount() {
                return injectedItems.size();
            }

            @Override
            public int getChildItemTypeAtPosition(@ChildPosition int position) {
                return childItems.get(position).type;
            }

            @Override
            public int getInjectedItemTypeAtPosition(int position) {
                for (Item injectedItem : injectedItems) {
                    if (injectedItem.position == position) {
                        return injectedItem.type;
                    }
                }

                return -1;
            }

            @Override
            public int getInjectedItemTypeAtIndex(int index) {
                return injectedItems.get(index).type;
            }

            @Override
            public long getChildItemIdAtPosition(@ChildPosition int position) {
                return childItems.get(position).id;
            }

            @Override
            public int getInjectedItemCountBeforePosition(int position) {
                int count = 0;
                for (Item injectedItem : injectedItems) {
                    if (injectedItem.position < position) {
                        count++;
                    }
                }
                return count;
            }

            @Override
            public boolean isItemInjectedOnPosition(int position) {
                for (Item injectedItem : injectedItems) {
                    if (injectedItem.position == position) {
                        return true;
                    }
                }

                return false;
            }
        };
    }

    private ItemsMerger.MergeOptionsProvider createMergeOptions(final boolean injectOnEmpty, final boolean injectOnLowerPosition, final boolean showInjectedViews) {
        return new ItemsMerger.MergeOptionsProvider() {
            @Override
            public boolean shouldShowInjectedItemsOnEmptyChildItems() {
                return injectOnEmpty;
            }

            @Override
            public boolean shouldShowInjectedItemsOnLowerPosition() {
                return injectOnLowerPosition;
            }

            @Override
            public boolean shouldShowInjectedViews() {
                return showInjectedViews;
            }
        };
    }

}

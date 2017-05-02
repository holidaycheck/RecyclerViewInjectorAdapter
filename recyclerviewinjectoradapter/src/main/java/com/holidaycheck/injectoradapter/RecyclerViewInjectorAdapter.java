package com.holidaycheck.injectoradapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecyclerViewInjectorAdapter is a RecyclerView.Adapter that wraps regular adapter.
 * It gives possibility to easily inject custom views at desired position
 *
 * @param <VH> type of childAdapter ViewHolder
 */
public class RecyclerViewInjectorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final static String TAG = "RecyclerViewInjectorA";
    private final RecyclerView.Adapter<VH> childAdapter;

    private ItemsMerger itemsMerger;
    private MergedListDiffer mergedListDiffer;
    private VectorCalculator vectorCalculator;

    private List<MergedItem> mergedList = Collections.emptyList();
    private List<Long> nonStableFakeIds;

    /**
     * Indicates if injected views should be visible on empty child adapter
     */
    private boolean displayInjectedViewsOnEmptyChildAdapter = false;

    /**
     * Indicates if injected views can be displayed on lower position than desired (because child don't have enough items)
     */
    private boolean displayInjectedViewsOnLowerPosition = false;

    /**
     * Whether or not injected views should be visible
     */
    private boolean showInjectedViews = true;

    /**
     * Vector that is uses to translate items positions
     * parent adapter ---> child adapter
     * <p>
     * If list looks like these:
     * 0. Child item
     * 1. Child item
     * 2. -> Injected item <-
     * 3. -> Injected item <-
     * 4. Child item
     * 5. -> Injected item <-
     * 5. Child item
     * --------------------------
     * Then the vector is
     * [0][0][2][2][2][3]
     * </p>
     */
    private int[] toChildItemPositionTranslationVector;

    /**
     * Vector that is uses to translate items positions
     * parent adapter <--- child adapter
     * <p>
     * If list looks like these:
     * 0. Child item
     * 1. Child item
     * 2. -> Injected item <-
     * 3. -> Injected item <-
     * 4. Child item
     * 5. -> Injected item <-
     * 5. Child item
     * --------------------------
     * Then the vector is
     * [0][0][2][3]
     * </p>
     */
    private int[] fromChildItemPositionTranslationVector;

    /**
     * Vector that indicates number of injected views before & on given position
     * <p>
     * If list looks like these:
     * 0. Normal item
     * 1. Normal item
     * 2. -> Injected item <-
     * 3. -> Injected item <-
     * 4. Normal item
     * 5. -> Injected item <-
     * 5. Normal item
     * --------------------------
     * Then the vector is
     * [0][0][1][2][2][3]
     * </p>
     */
    private int[] numberOfInjectedItemsUpToPosition;

    /**
     * Map injected view position to view itself
     */
    private SparseArray<InjectedViewCreator> injectedItems = new SparseArray<>();

    /**
     * Map that maps view type of injected view to position
     */
    private SparseIntArray viewTypeToPositionMap = new SparseIntArray();

    /**
     * Map that maps position of injected view to view type
     */
    private SparseIntArray positionToViewTypeMap = new SparseIntArray();

    private int assignedInjectedViewTypes;

    private boolean initializedOnRecyclerViewAttach;

    /**
     * Creates new instance of this adapter that is based on data provided in childAdapter.
     * At this point childAdapter should has hasStableIds correctly set.
     *
     * @param childAdapter - adapter with real data
     */
    public RecyclerViewInjectorAdapter(RecyclerView.Adapter<VH> childAdapter) {
        this.childAdapter = childAdapter;
        setHasStableIds(true);
        childAdapter.registerAdapterDataObserver(new InjectingDataSetObserver());

        if (!childAdapter.hasStableIds()) {
            createNonStableFakeIds();
        }

        itemsMerger = new ItemsMerger(itemsMergerDataProvider, mergeOptionsProvider);
        vectorCalculator = new VectorCalculator(vectorCalculatorDataProvider);
        mergedListDiffer = new MergedListDiffer();
    }

    /**
     * Injects view into position.
     * If another view is already injected on this position then it's overridden by this one.
     *
     * @param injectedViewCreator - class that creates view
     * @param position            - target position for this view
     */
    public void inject(@NonNull InjectedViewCreator injectedViewCreator, @IntRange(from = 0) int position) {
        injectedItems.put(position, injectedViewCreator);

        int injectedViewType = Integer.MAX_VALUE - assignedInjectedViewTypes;
        assignedInjectedViewTypes += 1;

        viewTypeToPositionMap.put(injectedViewType, position);
        positionToViewTypeMap.put(position, injectedViewType);

        recalculateVectors();
        mergeItemsAndDispatchNotifications();
    }

    /**
     * Removes injected view on given position.
     * If nothing is injected a this position, nothing happens.
     *
     * @param position - position from which injected view should be removed
     */
    public void removeInjectedView(@IntRange(from = 0) int position) {
        if (injectedItems.get(position) == null) {
            Log.w(TAG, "Trying to remove view from not injected position");
        } else {
            injectedItems.remove(position);

            int viewType = positionToViewTypeMap.get(position);
            viewTypeToPositionMap.delete(viewType);
            positionToViewTypeMap.delete(position);

            recalculateVectors();
            mergeItemsAndDispatchNotifications();
        }
    }

    /**
     * @return array containing positions of injected views
     */
    public int[] getInjectedViewPositions() {
        int[] positions = new int[injectedItems.size()];
        for (int i = 0; i < injectedItems.size(); i++) {
            positions[i] = injectedItems.keyAt(i);
        }
        return positions;
    }

    /**
     * @return number of injected views
     */
    public int countInjectedViews() {
        return injectedItems.size();
    }

    /**
     * @return true - injected views are visible on empty adapter,
     * false - injected views are not visible on empty adapter
     */
    public boolean isDisplayInjectedViewsOnEmptyChildAdapter() {
        return displayInjectedViewsOnEmptyChildAdapter;
    }

    /**
     * Controls visibility of injected views. False by default.
     *
     * @param display true - injected views are visible on empty adapter,
     *                false - injected views are not visible on empty adapter
     */
    public void setDisplayInjectedViewsOnEmptyChildAdapter(boolean display) {
        this.displayInjectedViewsOnEmptyChildAdapter = display;
        mergeItemsAndDispatchNotifications();
    }

    /**
     * @return true - injected views are visible even on lower positions than desired,
     * false - injected views are not visible on lower positions
     */
    public boolean isDisplayInjectedViewsOnLowerPosition() {
        return displayInjectedViewsOnLowerPosition;
    }

    /**
     * Controls visibility of injected views. False by default.
     *
     * @param display true - injected views are visible even on lower positions than desired,
     *                false - injected views are not visible on lower positions
     */
    public void setDisplayInjectedViewsOnLowerPosition(boolean display) {
        this.displayInjectedViewsOnLowerPosition = display;
        mergeItemsAndDispatchNotifications();
    }

    /**
     * Controls visibility of injected views. True by default.
     *
     * @return true - all injected views are not hidden,
     * false - all injected views are hidden
     */
    public boolean isShowInjectedViews() {
        return showInjectedViews;
    }

    /**
     * @param showInjectedViews true - all injected views are not hidden,
     *                          false - all injected views are hidden
     */
    public void setShowInjectedViews(boolean showInjectedViews) {
        this.showInjectedViews = showInjectedViews;
        mergeItemsAndDispatchNotifications();
    }

    /**
     * Converts adapter position to child adapter position
     *
     * @param position - adapter position
     * @return - child adapter position
     */
    @ChildPosition
    public int toChildAdapterPosition(int position) {
        return showInjectedViews
            ? position - getTrimmedArrayValue(toChildItemPositionTranslationVector, position)
            : position;
    }

    /**
     * Converts child adapter position to adapter position
     *
     * @param childPosition - child adapter position
     * @return - adapter position
     */
    public int fromChildAdapterPosition(@ChildPosition int childPosition) {
        return showInjectedViews
            ? childPosition + getTrimmedArrayValue(fromChildItemPositionTranslationVector, childPosition)
            : childPosition;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (!initializedOnRecyclerViewAttach) {
            recalculateVectors();
            mergedList = itemsMerger.mergeItems();
            initializedOnRecyclerViewAttach = true;
        }

        childAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        if (!hasStableIds) {
            throw new RuntimeException("RecyclerViewInjectorAdapter has always stable ids and it can't be changed");
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        if (!mergedList.get(position).injected) {
            childAdapter.onBindViewHolder(holder, toChildAdapterPosition(position), payloads);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        childAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public boolean onFailedToRecycleView(VH holder) {
        if (!(holder instanceof InjectedViewHolder)) {
            return childAdapter.onFailedToRecycleView(holder);
        } else {
            return super.onFailedToRecycleView(holder);
        }
    }

    @Override
    public void onViewAttachedToWindow(VH holder) {
        if (!(holder instanceof InjectedViewHolder)) {
            childAdapter.onViewAttachedToWindow(holder);
        } else {
            super.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(VH holder) {
        if (!(holder instanceof InjectedViewHolder)) {
            childAdapter.onViewDetachedFromWindow(holder);
        } else {
            super.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public void onViewRecycled(VH holder) {
        if (!(holder instanceof InjectedViewHolder)) {
            childAdapter.onViewRecycled(holder);
        } else {
            super.onViewRecycled(holder);
        }
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
    }

    private void recalculateVectors() {
        toChildItemPositionTranslationVector = vectorCalculator.calculateToChildItemPositionTranslationVector();
        fromChildItemPositionTranslationVector = vectorCalculator.calculateFromChildItemPositionTranslationVector();
        numberOfInjectedItemsUpToPosition = vectorCalculator.calculateNumberOfInjectedItemsUpToPosition();
    }

    @Override
    @SuppressWarnings("unchecked")
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        int injectedPosition = viewTypeToPositionMap.get(viewType, -1);
        if (injectedPosition != -1) {
            return (VH) new InjectedViewHolder(injectedItems.get(injectedPosition).createView(parent)) {
            };
        } else {
            return childAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mergedList.get(position).injected) {
            childAdapter.onBindViewHolder(holder, toChildAdapterPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        return mergedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mergedList.get(position).type;
    }

    @Override
    public long getItemId(int position) {
        return mergedList.get(position).id;
    }

    private void createNonStableFakeIds() {
        if (nonStableFakeIds != null) {
            nonStableFakeIds.clear();
        } else {
            nonStableFakeIds = new ArrayList<>(childAdapter.getItemCount());
        }

        for (int i = 0; i < childAdapter.getItemCount(); i++) {
            nonStableFakeIds.add((long) i);
        }
    }

    private int getTrimmedArrayValue(int[] trimmedArray, int position) {
        if (trimmedArray == null) {
            return 0;
        }

        return trimmedArray.length > position
            ? trimmedArray[position]
            : trimmedArray[trimmedArray.length - 1];
    }

    private void mergeItemsAndDispatchNotifications() {
        mergeItems();
        DiffUtil.calculateDiff(mergedListDiffer).dispatchUpdatesTo(RecyclerViewInjectorAdapter.this);
    }

    private void mergeItems() {
        List<MergedItem> oldMergedList = mergedList;
        mergedList = itemsMerger.mergeItems();
        mergedListDiffer.updateData(oldMergedList, mergedList);
    }

    private class InjectingDataSetObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            if (childAdapter.hasStableIds()) {
                mergeItemsAndDispatchNotifications();
            } else {
                createNonStableFakeIds();
                mergedList = itemsMerger.mergeItems();
                notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeRemoved(@ChildPosition int positionStart, int itemCount) {
            if (!childAdapter.hasStableIds()) {
                for (int i = 0; i < itemCount; i++) {
                    nonStableFakeIds.remove(positionStart);
                }
            }
            mergeItemsAndDispatchNotifications();
        }

        @Override
        public void onItemRangeMoved(@ChildPosition int fromPosition, int toPosition, int itemCount) {
            if (!childAdapter.hasStableIds()) {
                if (itemCount > 1) {
                    throw new RuntimeException("RecyclerView supports moving only one item");
                }
                Long removedId = nonStableFakeIds.remove(fromPosition);
                nonStableFakeIds.add(toPosition, removedId);
            }
            mergeItemsAndDispatchNotifications();
        }

        @Override
        public void onItemRangeInserted(@ChildPosition int positionStart, int itemCount) {
            if (!childAdapter.hasStableIds()) {
                for (int i = 0; i < itemCount; i++) {
                    nonStableFakeIds.add(positionStart, (long) (nonStableFakeIds.size() + 1));
                }
            }
            mergeItemsAndDispatchNotifications();
        }

        @Override
        public void onItemRangeChanged(@ChildPosition int positionStart, int itemCount, Object payload) {
            mergeItems();
            for (int i = 0; i < itemCount; i++) {
                notifyItemChanged(fromChildAdapterPosition(positionStart + i), payload);
            }
        }

        @Override
        public void onItemRangeChanged(@ChildPosition int positionStart, int itemCount) {
            mergeItems();
            for (int i = 0; i < itemCount; i++) {
                notifyItemChanged(fromChildAdapterPosition(positionStart + i));
            }
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private ItemsMerger.ItemsDataProvider itemsMergerDataProvider = new ItemsMerger.ItemsDataProvider() {

        @Override
        public int getChildItemCount() {
            return childAdapter.getItemCount();
        }

        @Override
        public int getInjectedItemCount() {
            return injectedItems.size();
        }

        @Override
        public int getChildItemTypeAtPosition(@ChildPosition int position) {
            return childAdapter.getItemViewType(position);
        }

        @Override
        public int getInjectedItemTypeAtPosition(int position) {
            return positionToViewTypeMap.get(position);
        }

        @Override
        public int getInjectedItemTypeAtIndex(int index) {
            return positionToViewTypeMap.valueAt(index);
        }

        @Override
        public long getChildItemIdAtPosition(@ChildPosition int position) {
            return childAdapter.hasStableIds() ? childAdapter.getItemId(position) : nonStableFakeIds.get(position);
        }

        @Override
        public int getInjectedItemCountBeforePosition(int position) {
            return getTrimmedArrayValue(numberOfInjectedItemsUpToPosition, position);
        }

        @Override
        public boolean isItemInjectedOnPosition(int position) {
            return injectedItems.get(position) != null;
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private ItemsMerger.MergeOptionsProvider mergeOptionsProvider = new ItemsMerger.MergeOptionsProvider() {

        @Override
        public boolean shouldShowInjectedItemsOnEmptyChildItems() {
            return displayInjectedViewsOnEmptyChildAdapter;
        }

        @Override
        public boolean shouldShowInjectedItemsOnLowerPosition() {
            return displayInjectedViewsOnLowerPosition;
        }

        @Override
        public boolean shouldShowInjectedViews() {
            return showInjectedViews;
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private VectorCalculator.DataProvider vectorCalculatorDataProvider = new VectorCalculator.DataProvider() {

        @Override
        public int getInjectedItemCount() {
            return injectedItems.size();
        }

        @Override
        public int getInjectedItemPositionAtIndex(int index) {
            return injectedItems.keyAt(index);
        }

        @Override
        public boolean isItemInjectedOnPosition(int position) {
            return injectedItems.get(position) != null;
        }
    };
}

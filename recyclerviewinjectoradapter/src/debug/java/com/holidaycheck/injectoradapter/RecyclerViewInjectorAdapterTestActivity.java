package com.holidaycheck.injectoradapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewInjectorAdapterTestActivity extends Activity {

    private ChildAdapter adapter;
    private RecyclerViewInjectorAdapter<RecyclerView.ViewHolder> injectorAdapter;
    private List<Pair<String, Integer>> data = new ArrayList<>();

    private ViewGroup rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = new FrameLayout(this);
        setContentView(rootView);
    }

    public class ChildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(new TextView(RecyclerViewInjectorAdapterTestActivity.this)) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            String text = data.get(position).first;
            TextView textView = (TextView) holder.itemView;
            textView.setText(text);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).second;
        }
    }

    public ChildAdapter getAdapter() {
        return adapter;
    }

    public List<Pair<String, Integer>> getData() {
        return data;
    }

    public RecyclerViewInjectorAdapter<RecyclerView.ViewHolder> getInjectorAdapter() {
        return injectorAdapter;
    }

    public void addRecyclerView(boolean hasStableIds) {
        adapter = new ChildAdapter();
        adapter.setHasStableIds(hasStableIds);

        injectorAdapter = new RecyclerViewInjectorAdapter<>(adapter);

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setId(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(injectorAdapter);

        rootView.addView(recyclerView);

    }
}

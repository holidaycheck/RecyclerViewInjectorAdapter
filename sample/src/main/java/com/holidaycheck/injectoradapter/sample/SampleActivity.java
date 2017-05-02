package com.holidaycheck.injectoradapter.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.holidaycheck.injectoradapter.InjectedViewCreator;
import com.holidaycheck.injectoradapter.RecyclerViewInjectorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleActivity extends AppCompatActivity {

    private static int[] INJECTED_VIEW_LAYOUTS = {
        R.layout.injected_view_button,
        R.layout.injected_view_picture,
        R.layout.injected_view_picture_2,
        R.layout.injected_view_progress_bar,
        R.layout.injected_view_text,
    };

    private RecyclerViewInjectorAdapter<CustomVH> injectorAdapter;
    private List<String> adapterData;
    private Random random = new Random();

    @BindView(R.id.recycler_view)
    protected RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        ButterKnife.bind(this);

        //create regular adapter
        adapterData = createAdapterData();
        Adapter adapter = new Adapter();

        //wrap it with RecyclerViewInjectorAdapter
        injectorAdapter = new RecyclerViewInjectorAdapter<>(adapter);

        //attach to RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(injectorAdapter);
    }

    @OnClick(R.id.controls_inject)
    protected void onInjectClick() {
        final LayoutInflater layoutInflater = LayoutInflater.from(this);
        final int layoutRes = INJECTED_VIEW_LAYOUTS[injectorAdapter.countInjectedViews() % INJECTED_VIEW_LAYOUTS.length];

        injectorAdapter.inject(new InjectedViewCreator() {
            @Override
            public View createView(ViewGroup parent) {
                return layoutInflater.inflate(layoutRes, recyclerView, false);
            }
        }, random.nextInt(adapterData.size()));
    }

    @OnClick(R.id.controls_remove)
    protected void onRemoveClick() {
        int[] positions = injectorAdapter.getInjectedViewPositions();
        if (positions.length > 0) {
            injectorAdapter.removeInjectedView(positions[random.nextInt(positions.length)]);
        }
    }

    @OnClick(R.id.controls_show_hide)
    protected void onShowHideClick(Button button) {
        boolean currentlyShowing = injectorAdapter.isShowInjectedViews();
        injectorAdapter.setShowInjectedViews(!currentlyShowing);

        button.setText(currentlyShowing ? R.string.control_show : R.string.control_hide);
    }

    private List<String> createAdapterData() {
        int items = 25;
        List<String> data = new ArrayList<>(items);
        String prefix = "Regular item at position ";
        for (int i = 0; i < items; i++) {
            data.add(prefix + i);
        }
        return data;
    }

    class CustomVH extends RecyclerView.ViewHolder {

        CustomVH(View itemView) {
            super(itemView);
        }
    }

    private class Adapter extends RecyclerView.Adapter<CustomVH> {

        @Override
        public CustomVH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CustomVH(new TextView(SampleActivity.this));
        }

        @Override
        public void onBindViewHolder(CustomVH holder, int position) {
            String text = adapterData.get(position);
            TextView textView = (TextView) holder.itemView;
            textView.setText(text);
        }

        @Override
        public int getItemCount() {
            return adapterData.size();
        }
    }

}

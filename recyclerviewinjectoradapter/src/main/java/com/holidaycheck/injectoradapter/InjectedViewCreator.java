package com.holidaycheck.injectoradapter;

import android.view.View;
import android.view.ViewGroup;

/**
 * Class that is responsible for creating views that are injected into RecyclerView
 */
public interface InjectedViewCreator {

    View createView(ViewGroup parent);
}

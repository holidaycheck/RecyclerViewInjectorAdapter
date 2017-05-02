package com.holidaycheck.injectoradapter;

import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.holidaycheck.injectoradapter.RecyclerViewMatcher.withRecyclerView;

@RunWith(Parameterized.class)
public class RecyclerViewInjectorAdapterAndroidTest {

    private static final String INJECTED_TEXT_VIEW_1_TITLE = "Injected View 1";
    private static final String INJECTED_TEXT_VIEW_2_TITLE = "Injected View 2";
    private static final String INJECTED_TEXT_VIEW_3_TITLE = "Injected View 3";

    @Parameterized.Parameters
    public static Collection<Boolean[]> data() {
        return Arrays.asList(new Boolean[][]{
            { true }, { false }
        });
    }

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public ActivityTestRule<RecyclerViewInjectorAdapterTestActivity> mActivityRule = new ActivityTestRule<>(RecyclerViewInjectorAdapterTestActivity.class);

    private List<Pair<String, Integer>> childAdapterData;
    private boolean stableId;

    public RecyclerViewInjectorAdapterAndroidTest(boolean stableId) {
        this.stableId = stableId;
    }

    @Before
    public void setup() {
        childAdapterData = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            childAdapterData.add(new Pair<>("Item " + i, i));
        }
    }

    @Test
    public void testInjectedViewVisibilityOnEmptyChildAdapter() throws Throwable {
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getInjectorAdapter().setDisplayInjectedViewsOnEmptyChildAdapter(false);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 0);
            }
        });
        onView(withText(INJECTED_TEXT_VIEW_1_TITLE)).check(doesNotExist());

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
                activity.getInjectorAdapter().setDisplayInjectedViewsOnEmptyChildAdapter(true);
            }
        });

        onView(withText(INJECTED_TEXT_VIEW_1_TITLE)).check(matches(isDisplayed()));
    }

    @Test
    public void testMultipleInjectedViewsHiddenWhenRequested() throws Throwable {
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getInjectorAdapter().setShowInjectedViews(false);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 8);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_2_TITLE), 12);

                activity.getData().addAll(childAdapterData);
                activity.getAdapter().notifyDataSetChanged();
            }
        });

        List<String> listWithInjectedItems = getChildAdapterDataStringList(childAdapterData);

        for (int i = 0; i < listWithInjectedItems.size(); i++) {
            onView(withRecyclerView(R.id.recycler_view).atPosition(i)).check(matches(withText(listWithInjectedItems.get(i))));
        }
    }

    @Test
    public void testMultipleInjectedViewsOnDesiredPositions() throws Throwable {
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 8);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_2_TITLE), 12);

                activity.getData().addAll(childAdapterData);
                activity.getAdapter().notifyDataSetChanged();
            }
        });

        List<String> listWithInjectedItems = getChildAdapterDataStringList(childAdapterData);

        listWithInjectedItems.add(8, INJECTED_TEXT_VIEW_1_TITLE);
        listWithInjectedItems.add(12, INJECTED_TEXT_VIEW_2_TITLE);

        for (int i = 0; i < listWithInjectedItems.size(); i++) {
            onView(withRecyclerView(R.id.recycler_view).atPosition(i)).check(matches(withText(listWithInjectedItems.get(i))));
        }
    }

    @Test
    public void testMultipleInjectedViewsOnLowerPositionThanDesired() throws Throwable {
        final List<Pair<String, Integer>> childAdapterDataSubList = childAdapterData.subList(0, 6);
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
                activity.getInjectorAdapter().setDisplayInjectedViewsOnLowerPosition(true);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 2);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_2_TITLE), 8);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_3_TITLE), 12);

                activity.getData().addAll(childAdapterDataSubList);
                activity.getAdapter().notifyDataSetChanged();
            }
        });

        List<String> listWithInjectedItems = getChildAdapterDataStringList(childAdapterDataSubList);
        listWithInjectedItems.add(2, INJECTED_TEXT_VIEW_1_TITLE);
        listWithInjectedItems.add(INJECTED_TEXT_VIEW_2_TITLE);
        listWithInjectedItems.add(INJECTED_TEXT_VIEW_3_TITLE);

        for (int i = 0; i < listWithInjectedItems.size(); i++) {
            onView(withRecyclerView(R.id.recycler_view).atPosition(i)).check(matches(withText(listWithInjectedItems.get(i))));
        }
    }

    @Test
    public void testInjectedViewVisibleUntilEmptyChildAdapter() throws Throwable {
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getInjectorAdapter().setDisplayInjectedViewsOnEmptyChildAdapter(false);
                activity.getInjectorAdapter().setDisplayInjectedViewsOnLowerPosition(true);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 2);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_2_TITLE), 100);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_3_TITLE), 101);

                activity.getData().addAll(childAdapterData);
                activity.getAdapter().notifyDataSetChanged();
            }
        });

        ArrayList<Pair<String, Integer>> childAdapterDataCopy = new ArrayList<>(childAdapterData);

        for (final Pair<String, Integer> s : childAdapterDataCopy) {
            uiThreadTestRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final int removedItemIndex = activity.getData().indexOf(s);
                    activity.getData().remove(removedItemIndex);
                    activity.getAdapter().notifyItemRemoved(removedItemIndex);
                }
            });

            List<String> injectedChildAdapterData = getChildAdapterDataStringList(activity.getData());
            if (injectedChildAdapterData.size() > 2) {
                injectedChildAdapterData.add(2, INJECTED_TEXT_VIEW_1_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_2_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_3_TITLE);
            } else if (!injectedChildAdapterData.isEmpty()) {
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_1_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_2_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_3_TITLE);
            }

            for (int i = 0; i < injectedChildAdapterData.size(); i++) {
                onView(withRecyclerView(R.id.recycler_view).atPosition(i)).check(matches(withText(injectedChildAdapterData.get(i))));
            }

            if (injectedChildAdapterData.size() == 0) {
                onView(withRecyclerView(R.id.recycler_view).atPosition(0)).check(doesNotExist());
            }
        }
    }

    @Test
    public void testInjectedViewStillVisibleAfterAddingItemsToEmptyChildAdapter() throws Throwable {
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getInjectorAdapter().setDisplayInjectedViewsOnEmptyChildAdapter(true);
                activity.getInjectorAdapter().setDisplayInjectedViewsOnLowerPosition(true);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 5);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_2_TITLE), 100);
            }
        });

        for (final Pair<String, Integer> s : childAdapterData) {
            uiThreadTestRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getData().add(0, s);
                    activity.getAdapter().notifyItemInserted(0);
                }
            });

            List<String> injectedChildAdapterData = getChildAdapterDataStringList(activity.getData());
            if (injectedChildAdapterData.size() >= 5) {
                injectedChildAdapterData.add(5, INJECTED_TEXT_VIEW_1_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_2_TITLE);
            } else {
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_1_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_2_TITLE);
            }

            for (int i = 0; i < injectedChildAdapterData.size(); i++) {
                onView(withRecyclerView(R.id.recycler_view).atPosition(i)).check(matches(withText(injectedChildAdapterData.get(i))));
            }
        }
    }

    @Test
    public void testInjectedViewStillVisibleAfterMultipleChildAdapterDataChanges() throws Throwable {
        final RecyclerViewInjectorAdapterTestActivity activity = mActivityRule.getActivity();
        setStableIds(activity, stableId);

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getInjectorAdapter().setDisplayInjectedViewsOnEmptyChildAdapter(true);
                activity.getInjectorAdapter().setDisplayInjectedViewsOnLowerPosition(true);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_1_TITLE), 5);
                activity.getInjectorAdapter().inject(getInjectedView(INJECTED_TEXT_VIEW_2_TITLE), 100);

                activity.getData().addAll(childAdapterData);
                activity.getAdapter().notifyDataSetChanged();
            }
        });

        for (int changeId = 0; changeId < 9; changeId++) {
            if (changeId == 0) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Pair<String, Integer> objectAboutToMove = activity.getData().get(5);
                        activity.getData().remove(5);
                        activity.getData().add(10, objectAboutToMove);
                        activity.getAdapter().notifyItemMoved(5, 10);
                    }
                });
            } else if (changeId == 1) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 6; i++) {
                            activity.getData().remove(1);
                        }
                        activity.getAdapter().notifyItemRangeRemoved(1, 6);
                    }
                });
            } else if (changeId == 2) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.getData().remove(3);
                        activity.getAdapter().notifyItemRemoved(3);
                    }
                });
            } else if (changeId == 3) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Pair<String, Integer> objectAboutToUpdate = activity.getData().get(2);
                        activity.getData().remove(2);
                        activity.getData().add(2, new Pair<>("Updated view " + objectAboutToUpdate.second, objectAboutToUpdate.second));
                        activity.getAdapter().notifyItemChanged(2);
                    }
                });
            } else if (changeId == 4) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Pair<String, Integer> objectAboutToUpdate = activity.getData().get(2);
                        activity.getData().remove(2);
                        activity.getData().add(2, new Pair<>("Updated view " + objectAboutToUpdate.second, objectAboutToUpdate.second));
                        activity.getAdapter().notifyItemChanged(2, new Object());
                    }
                });
            } else if (changeId == 5) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.getData().add(0, new Pair<>("New item", 100));
                        activity.getAdapter().notifyItemInserted(0);
                    }
                });
            } else if (changeId == 6) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.getData().add(4, new Pair<>("New item", 101));
                        activity.getData().add(5, new Pair<>("New item", 102));
                        activity.getAdapter().notifyItemRangeInserted(4, 2);
                    }
                });
            } else if (changeId == 7) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Pair<String, Integer> objectAboutToUpdate = activity.getData().get(2);
                        Pair<String, Integer> objectAboutToUpdate2 = activity.getData().get(3);

                        activity.getData().remove(2);
                        activity.getData().remove(2);
                        activity.getData().add(2, new Pair<>("Updated view x " + objectAboutToUpdate.second, objectAboutToUpdate.second));
                        activity.getData().add(3, new Pair<>("Updated view x " + objectAboutToUpdate2.second, objectAboutToUpdate2.second));

                        activity.getAdapter().notifyItemRangeChanged(2, 2);
                    }
                });
            } else if (changeId == 8) {
                uiThreadTestRule.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Pair<String, Integer> objectAboutToUpdate = activity.getData().get(2);
                        Pair<String, Integer> objectAboutToUpdate2 = activity.getData().get(3);

                        activity.getData().remove(2);
                        activity.getData().remove(2);
                        activity.getData().add(2, new Pair<>("Updated view y " + objectAboutToUpdate.second, objectAboutToUpdate.second));
                        activity.getData().add(3, new Pair<>("Updated view y " + objectAboutToUpdate2.second, objectAboutToUpdate2.second));

                        activity.getAdapter().notifyItemRangeChanged(2, 2, new Object());
                    }
                });
            }

            List<String> injectedChildAdapterData = getChildAdapterDataStringList(activity.getData());
            if (injectedChildAdapterData.size() >= 5) {
                injectedChildAdapterData.add(5, INJECTED_TEXT_VIEW_1_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_2_TITLE);
            } else {
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_1_TITLE);
                injectedChildAdapterData.add(INJECTED_TEXT_VIEW_2_TITLE);
            }

            for (int i = 0; i < injectedChildAdapterData.size(); i++) {
                onView(withRecyclerView(R.id.recycler_view).atPosition(i)).check(matches(withText(injectedChildAdapterData.get(i))));
            }
        }
    }

    private void setStableIds(final RecyclerViewInjectorAdapterTestActivity activity, final boolean stableIds) throws Throwable {
        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.addRecyclerView(stableIds);
            }
        });
    }

    private List<String> getChildAdapterDataStringList(List<Pair<String, Integer>> list) {
        List<String> result = new ArrayList<>(list.size());
        for (Pair<String, Integer> item : list) {
            result.add(item.first);
        }
        return result;
    }

    @NonNull
    private InjectedViewCreator getInjectedView(final String text) {
        return new InjectedViewCreator() {
            @Override
            public View createView(ViewGroup parent) {
                TextView tv = new TextView(mActivityRule.getActivity());
                tv.setText(text);
                return tv;
            }
        };
    }
}

package com.holidaycheck.injectoradapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MergedListDifferTest {

    @Test
    public void returnsCorrectSizeOfOldList() {
        MergedListDiffer mergedListDiffer = new MergedListDiffer();
        mergedListDiffer.updateData(new ArrayList<MergedItem>() {{
            add(MergedItem.newInjectedRow(2));
            add(MergedItem.newInjectedRow(3));
        }}, Collections.<MergedItem>emptyList());

    }

    @Test
    public void returnsCorrectSizeOfNewList() {
        MergedListDiffer mergedListDiffer = new MergedListDiffer();
        mergedListDiffer.updateData(Collections.<MergedItem>emptyList(), new ArrayList<MergedItem>() {{
            add(MergedItem.newInjectedRow(2));
            add(MergedItem.newInjectedRow(3));
        }});

        assertThat(mergedListDiffer.getOldListSize(), is(0));
        assertThat(mergedListDiffer.getNewListSize(), is(2));
    }

    @Test
    public void testAreItemsTheSame() {
        MergedListDiffer mergedListDiffer = new MergedListDiffer();
        mergedListDiffer.updateData(new ArrayList<MergedItem>() {{
            add(MergedItem.newInjectedRow(2));
            add(MergedItem.newInjectedRow(3));
        }}, new ArrayList<MergedItem>() {{
            add(MergedItem.newInjectedRow(2));
            add(MergedItem.newInjectedRow(4));
        }});

        assertThat(mergedListDiffer.areItemsTheSame(0, 0), is(true));
        assertThat(mergedListDiffer.areItemsTheSame(1, 1), is(false));
    }

    @Test
    public void areContentsTheSameAlwaysReturnsTrue() {
        MergedListDiffer mergedListDiffer = new MergedListDiffer();

        assertThat(mergedListDiffer.areContentsTheSame(1, 1), is(true));
    }

}

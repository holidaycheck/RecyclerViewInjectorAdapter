package com.holidaycheck.injectoradapter;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class MergedItemTest {

    @Test
    public void testNewInjectedRowBuilder() {
        MergedItem mergedItem = MergedItem.newInjectedRow(1);

        assertThat(mergedItem.injected, is(true));
        assertThat(mergedItem.type, is(1));
        assertThat(mergedItem.id, is(1L));
    }

    @Test
    public void testNewRowBuilder() {
        MergedItem mergedItem = MergedItem.newRow(1, 2L);

        assertThat(mergedItem.injected, is(false));
        assertThat(mergedItem.type, is(1));
        assertThat(mergedItem.id, is(2L));
    }

    @Test
    public void testMergedItemEqual() {
        MergedItem mergedItem1 = MergedItem.newRow(1, 2L);
        MergedItem mergedItem2 = MergedItem.newRow(1, 2L);
        MergedItem mergedItem3 = MergedItem.newRow(1, 3L);

        assertThat(mergedItem1, is(mergedItem2));
        assertThat(mergedItem1, is(not(mergedItem3)));
    }

}

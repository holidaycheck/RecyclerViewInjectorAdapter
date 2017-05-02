package com.holidaycheck.injectoradapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class VectorCalculatorTest {

    @Test
    public void toChildVectorHasCorrectValue() {
        testToChildVectorValue(Collections.<Integer>emptyList(), new int[]{ 0 });

        testToChildVectorValue(new ArrayList<Integer>() {{
            add(0);
        }}, new int[]{ 1 });

        testToChildVectorValue(new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(5);
        }}, new int[]{ 0, 3, 3, 3, 3, 4 });

        testToChildVectorValue(new ArrayList<Integer>() {{
            add(0);
            add(2);
            add(4);
            add(5);
            add(7);
        }}, new int[]{ 1, 1, 2, 2, 4, 4, 4, 5 });
    }

    @Test
    public void calculateFromChildItemPositionTranslationVector() throws Exception {
        testFromChildVectorValue(Collections.<Integer>emptyList(), new int[]{ 0 });

        testFromChildVectorValue(new ArrayList<Integer>() {{
            add(0);
        }}, new int[]{ 1 });

        testFromChildVectorValue(new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(5);
        }}, new int[]{ 0, 3, 4 });

        testFromChildVectorValue(new ArrayList<Integer>() {{
            add(0);
            add(2);
            add(4);
            add(5);
            add(7);
        }}, new int[]{ 1, 2, 4, 5 });
    }

    @Test
    public void calculateNumberOfInjectedItemsUpToPosition() throws Exception {
        testNumberOfInjectedItemsVectorValue(Collections.<Integer>emptyList(), new int[]{ 0 });

        testNumberOfInjectedItemsVectorValue(new ArrayList<Integer>() {{
            add(0);
        }}, new int[]{ 1 });

        testNumberOfInjectedItemsVectorValue(new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(5);
        }}, new int[]{ 0, 1, 2, 3, 3, 4 });

        testNumberOfInjectedItemsVectorValue(new ArrayList<Integer>() {{
            add(0);
            add(2);
            add(4);
            add(5);
            add(7);
        }}, new int[]{ 1, 1, 2, 2, 3, 4, 4, 5 });
    }

    private void testToChildVectorValue(List<Integer> injectedPositions, int[] expectedValue) {
        VectorCalculator vectorCalculator = new VectorCalculator(createDataProvider(new ArrayList<>(injectedPositions)));

        assertTrue(Arrays.equals(
            vectorCalculator.calculateToChildItemPositionTranslationVector(),
            expectedValue
        ));
    }

    private void testFromChildVectorValue(List<Integer> injectedPositions, int[] expectedValue) {
        VectorCalculator vectorCalculator = new VectorCalculator(createDataProvider(new ArrayList<>(injectedPositions)));

        assertTrue(Arrays.equals(
            vectorCalculator.calculateFromChildItemPositionTranslationVector(),
            expectedValue
        ));
    }

    private void testNumberOfInjectedItemsVectorValue(List<Integer> injectedPositions, int[] expectedValue) {
        VectorCalculator vectorCalculator = new VectorCalculator(createDataProvider(injectedPositions));

        assertTrue(Arrays.equals(
            vectorCalculator.calculateNumberOfInjectedItemsUpToPosition(),
            expectedValue
        ));
    }

    private VectorCalculator.DataProvider createDataProvider(final List<Integer> injectedPositions) {
        return new VectorCalculator.DataProvider() {

            @Override
            public int getInjectedItemCount() {
                return injectedPositions.size();
            }

            @Override
            public int getInjectedItemPositionAtIndex(int index) {
                return injectedPositions.get(index);
            }

            @Override
            public boolean isItemInjectedOnPosition(int position) {
                for (Integer injectedPosition : injectedPositions) {
                    if (injectedPosition == position) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}

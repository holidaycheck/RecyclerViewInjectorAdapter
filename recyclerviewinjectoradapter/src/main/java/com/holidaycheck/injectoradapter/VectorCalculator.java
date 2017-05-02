package com.holidaycheck.injectoradapter;

class VectorCalculator {

    private static final int NO_POSITION = -1;
    private static final int[] EMPTY_VECTOR = new int[]{ 0 };

    private DataProvider dataProvider;

    VectorCalculator(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    int[] calculateToChildItemPositionTranslationVector() {
        int maxInjectedItemPosition = getMaxInjectedItemPosition();
        if (maxInjectedItemPosition != NO_POSITION) {
            int[] vector = new int[maxInjectedItemPosition + 1];
            int vectorIndex = 0;
            int previousValue = 0;
            while (vectorIndex < vector.length) {
                if (!dataProvider.isItemInjectedOnPosition(vectorIndex)) {
                    vector[vectorIndex] = previousValue;
                    vectorIndex++;
                } else {
                    int continuousInjectedItems = countContinuousInjectedItems(vectorIndex);
                    previousValue = previousValue + continuousInjectedItems;
                    for (int subVectorIndex = vectorIndex; subVectorIndex < vectorIndex + continuousInjectedItems; subVectorIndex++) {
                        vector[subVectorIndex] = previousValue;
                    }
                    vectorIndex += continuousInjectedItems;
                }
            }
            return vector;
        } else {
            return EMPTY_VECTOR;
        }
    }

    int[] calculateFromChildItemPositionTranslationVector() {
        int maxInjectedItemPosition = getMaxInjectedItemPosition();
        if (maxInjectedItemPosition != NO_POSITION) {
            int vectorSize = maxInjectedItemPosition - dataProvider.getInjectedItemCount() + 2;
            int[] vector = new int[vectorSize];
            int accumulator = 0;
            int vectorIndex = 0;
            for (int i = 0; vectorIndex < vectorSize; i++) {
                if (!dataProvider.isItemInjectedOnPosition(i)) {
                    vector[vectorIndex++] = accumulator;
                } else {
                    accumulator++;
                }
            }
            return vector;
        } else {
            return EMPTY_VECTOR;
        }
    }

    int[] calculateNumberOfInjectedItemsUpToPosition() {
        int maxInjectedItemPosition = getMaxInjectedItemPosition();
        if (maxInjectedItemPosition != NO_POSITION) {
            int[] vector = new int[maxInjectedItemPosition + 1];
            int previousValue = 0;
            for (int i = 0; i < vector.length; i++) {
                vector[i] = previousValue + (dataProvider.isItemInjectedOnPosition(i) ? 1 : 0);
                previousValue = vector[i];
            }
            return vector;
        } else {
            return EMPTY_VECTOR;
        }
    }

    private int countContinuousInjectedItems(int startPos) {
        int number = 0;
        while (dataProvider.isItemInjectedOnPosition(startPos)) {
            number++;
            startPos++;
        }
        return number;
    }

    private int getMaxInjectedItemPosition() {
        int maxPos = NO_POSITION;
        for (int i = 0; i < dataProvider.getInjectedItemCount(); i++) {
            maxPos = Math.max(dataProvider.getInjectedItemPositionAtIndex(i), maxPos);
        }
        return maxPos;
    }

    interface DataProvider {
        int getInjectedItemCount();

        int getInjectedItemPositionAtIndex(int index);

        boolean isItemInjectedOnPosition(int position);
    }
}

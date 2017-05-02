package com.holidaycheck.injectoradapter;

class MergedItem {

    final int type;
    final long id;
    final boolean injected;

    private MergedItem(int type, long id, boolean injected) {
        this.type = type;
        this.id = id;
        this.injected = injected;
    }

    static MergedItem newInjectedRow(int type) {
        return new MergedItem(type, type, true);
    }

    static MergedItem newRow(int type, long id) {
        return new MergedItem(type, id, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MergedItem that = (MergedItem) o;
        return type == that.type && id == that.id && injected == that.injected;
    }
}

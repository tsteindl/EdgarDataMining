package csv;

import java.util.List;

class Table {
    private String tag;
    public final List<String> tableTags;

    Table(List<String> tableTags) {
        this.tag = "";
        this.tableTags = tableTags;
    }

    private void reset() {
    }

    public int getTableTagsColSize() {
        return this.tableTags.size();
    }

    public void add(String tag) {

    }

}


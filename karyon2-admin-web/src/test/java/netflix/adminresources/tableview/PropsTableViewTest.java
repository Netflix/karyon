package netflix.adminresources.tableview;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;

public class PropsTableViewTest {

    public static final String ALL_COLS_SEARCH_STR = "java";
    public static final String PROP_NAME_SEARCH_STR = "java";
    public static final String PROP_VALUE_SEARCH_STR = "java";

    @Test
    public void verifyColumnNames() {
        final PropsTableView ptv = new PropsTableView();
        final List<String> columns = ptv.getColumns();
        assertTrue(columns != null);
        assertTrue(columns.size() == 2);
        assertTrue(columns.get(0).equals("Key"));
        assertTrue(columns.get(1).equals("Value"));
    }


    @Test
    public void getDataTest() {
        final PropsTableView ptv = new PropsTableView();
        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        assertTrue(data.size() > 0);
    }


    @Test
    public void searchTermTest() {
        final PropsTableView ptv = new PropsTableView();
        ptv.setAllColumnsSearchTerm(ALL_COLS_SEARCH_STR);
        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        int totalElms = data.size();
        assertTrue(totalElms > 0);

        for (int i = 0; i < totalElms; i++) {
            final JsonElement propElm = data.get(i);
            final JsonArray propKVArray = propElm.getAsJsonArray();
            assertTrue(propKVArray.size() == 2);
            final String propKey = propKVArray.get(0).getAsString().toLowerCase();
            final String propValue = propKVArray.get(1).getAsString().toLowerCase();
            assertTrue("Property " + propKey + " does not contain " + ALL_COLS_SEARCH_STR, (propKey.contains(ALL_COLS_SEARCH_STR) || propValue.contains(ALL_COLS_SEARCH_STR)));
        }
    }

    @Test
    public void propNameSearchTest() {
        final PropsTableView ptv = new PropsTableView();
        ptv.setColumnSearchTerm(PropsTableView.KEY, PROP_NAME_SEARCH_STR);

        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        int totalElms = data.size();
        assertTrue(totalElms > 0);

        for (int i = 0; i < totalElms; i++) {
            final JsonElement propElm = data.get(i);
            final JsonArray propKVArray = propElm.getAsJsonArray();
            assertTrue(propKVArray.size() == 2);
            final String propKey = propKVArray.get(0).getAsString().toLowerCase();
            assertTrue("Property " + propKey + " does not contain " + PROP_NAME_SEARCH_STR, (propKey.contains(PROP_NAME_SEARCH_STR)));
        }
    }

    @Test
    public void propValueSearchTest() {
        final PropsTableView ptv = new PropsTableView();
        ptv.setColumnSearchTerm(PropsTableView.VALUE, PROP_VALUE_SEARCH_STR);

        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        int totalElms = data.size();
        assertTrue(totalElms > 0);

        for (int i = 0; i < totalElms; i++) {
            final JsonElement propElm = data.get(i);
            final JsonArray propKVArray = propElm.getAsJsonArray();
            assertTrue(propKVArray.size() == 2);
            final String propValue = propKVArray.get(1).getAsString().toLowerCase();
            assertTrue("Property " + propValue + " does not contain " + PROP_VALUE_SEARCH_STR, (propValue.contains(PROP_VALUE_SEARCH_STR)));
        }
    }

    @Test
    public void paginationTest() {
        final PropsTableView ptv = new PropsTableView();
        ptv.setCurrentPageInfo(0, 10);

        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        int totalElms = data.size();
        assertTrue(totalElms == 10);

        ptv.setCurrentPageInfo(11, 10);
        final JsonArray nextPageData = ptv.getData();
        assertTrue(nextPageData != null);
        totalElms = nextPageData.size();
        assertTrue(totalElms == 10);
    }

    @Test
    public void sortKeyAscendingTest() {
        final PropsTableView ptv = new PropsTableView();
        ptv.enableColumnSort(PropsTableView.KEY, false); // ascending sort

        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        int totalElms = data.size();
        assertTrue(totalElms > 0);

        String prevKey = null;
        for (int i = 0; i < totalElms; i++) {
            final JsonElement propElm = data.get(i);
            final JsonArray propKVArray = propElm.getAsJsonArray();
            final String propKey = propKVArray.get(0).getAsString();
            assertTrue(propKVArray.size() == 2);
            if (prevKey == null) {
                prevKey = propKey;
            } else {
                // verify sorting order
                assertTrue(prevKey.compareTo(propKey) < 0);
                prevKey = propKey;
            }
        }
    }


    @Test
    public void sortValueDescending() {
        final PropsTableView ptv = new PropsTableView();
        ptv.enableColumnSort(PropsTableView.VALUE, true); // descending on value

        final JsonArray data = ptv.getData();
        assertTrue(data != null);
        int totalElms = data.size();
        assertTrue(totalElms > 0);

        String prevValue = null;
        for (int i = 0; i < totalElms; i++) {
            final JsonElement propElm = data.get(i);
            final JsonArray propKVArray = propElm.getAsJsonArray();
            final String propValue = propKVArray.get(1).getAsString();
            assertTrue(propKVArray.size() == 2);
            if (prevValue == null) {
                prevValue = propValue;
            } else {
                // verify sorting order
                assertTrue(String.format("%s - %s sort failed", prevValue, propValue), prevValue.compareTo(propValue) >= 0);
                prevValue = propValue;
            }
        }
    }
}

package netflix.adminresources.tableview;


import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import netflix.adminresources.resources.PropertiesHelper;
import netflix.adminresources.resources.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropsTableView implements TableViewResource {
    private static final Logger LOG = LoggerFactory.getLogger(PropsTableView.class);

    private final Map<String, Column> columnMap = new LinkedHashMap<String, Column>();
    public static final String KEY = "Key";
    public static final String VALUE = "Value";

    private static class Column {
        String name;
        String searchTerm;
        boolean enableSort;
        boolean isDescendingSort;
    }

    private static class PageInfo {
        int startIndex;
        int count;
    }

    public PropsTableView() {
        LOG.debug("Creating a new propsTableViewResource ");
        Column keyCol = new Column();
        keyCol.name = KEY;
        Column valCol = new Column();
        valCol.name = VALUE;
        columnMap.put(KEY, keyCol);
        columnMap.put(VALUE, valCol);
    }

    private String allColumnSearchTerm;
    private PageInfo currentPage;
    private int totalRecords;
    private int numFilteredRecords;

    @Override
    public List<String> getColumns() {
        return new ArrayList<String>(columnMap.keySet());
    }

    @Override
    public TableViewResource setColumnSearchTerm(String column, String term) {
        if (columnMap.containsKey(column)) {
            columnMap.get(column).searchTerm = term;
        }
        return this;
    }

    @Override
    public TableViewResource setAllColumnsSearchTerm(String term) {
        allColumnSearchTerm = term;
        return this;
    }

    @Override
    public TableViewResource enableColumnSort(String column, boolean isDescending) {
        if (columnMap.containsKey(column)) {
            columnMap.get(column).isDescendingSort = isDescending;
            columnMap.get(column).enableSort = true;
        }
        return this;
    }

    @Override
    public JsonArray getData() {
        JsonArray props = new JsonArray();

        List<Property> properties = PropertiesHelper.getAllProperties();
        totalRecords = properties.size();
        numFilteredRecords = totalRecords;

        List<Property> propsFiltered = applyFilter(properties);
        List<Property> propsSorted = applySorting(propsFiltered);
        List<Property> propsCurrentPage = applyPagination(propsSorted);

        for (Property property : propsCurrentPage) {
            JsonArray propArr = new JsonArray();
            JsonPrimitive propName = new JsonPrimitive(property.getName());
            JsonPrimitive propValue = new JsonPrimitive(property.getValue());
            propArr.add(propName);
            propArr.add(propValue);

            props.add(propArr);
        }
        return props;
    }


    @Override
    public int getTotalNumOfRecords() {
        return totalRecords;
    }

    @Override
    public int getFilteredNumOfRecords() {
        return numFilteredRecords;
    }

    @Override
    public TableViewResource setCurrentPageInfo(int startIndex, int count) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.count = count;
        pageInfo.startIndex = startIndex;
        currentPage = pageInfo;
        return this;
    }

    private List<Property> applyPagination(List<Property> properties) {
        if (!isPaginated()) {
            return properties;
        }

        List<Property> propertyList = Lists.newArrayList();
        int index = 0;
        int endIndex = currentPage.startIndex + currentPage.count;
        for (Property property : properties) {
            if (index >= currentPage.startIndex && index < endIndex) {
                propertyList.add(property);
            }

            if (index >= endIndex) {
                // no need to scan further elements
                break;
            }

            index++;
        }
        return propertyList;
    }

    private boolean isPaginated() {
        return currentPage != null;
    }

    private List<Property> applyFilter(List<Property> propsCurrentPage) {
        if (allColumnSearchTerm != null && !allColumnSearchTerm.isEmpty()) {
            return applyAllColumnsFilter(propsCurrentPage);
        } else if (columnSearchTermExists()) {
            return applyColumnFilters(propsCurrentPage);
        }
        return propsCurrentPage;
    }

    private List<Property> applyColumnFilters(List<Property> propsCurrentPage) {
        final String propKeySearchTerm = getPropKeySearchTerm();
        final String propValueSearchTerm = getPropValueSearchTerm();

        List<Property> filteredList = Lists.newArrayList();
        int index = 0;
        for (Property property : propsCurrentPage) {
            String propName = property.getName().toLowerCase();
            String propValue = property.getValue().toLowerCase();

            boolean matched = true;
            if (propKeySearchTerm != null) {
                matched = propName.contains(propKeySearchTerm);
            }

            if (propValueSearchTerm != null) {
                matched = matched && (propValue.contains(propValueSearchTerm));
            }

            if (matched) {
                filteredList.add(property);
                index++;
            }
        }
        numFilteredRecords = index;
        return filteredList;
    }

    private List<Property> applyAllColumnsFilter(List<Property> propsCurrentPage) {
        final String searchTermLowerCase = allColumnSearchTerm.toLowerCase();

        List<Property> filteredList = Lists.newArrayList();
        int index = 0;
        for (Property property : propsCurrentPage) {
            String propName = property.getName().toLowerCase();
            String propValue = property.getValue().toLowerCase();

            if (propName.contains(searchTermLowerCase) || propValue.contains(searchTermLowerCase)) {
                filteredList.add(property);
                index++;
            }
        }
        numFilteredRecords = index;
        return filteredList;
    }

    private boolean columnSearchTermExists() {
        for (Map.Entry<String, Column> columnEntry : columnMap.entrySet()) {
            final Column col = columnEntry.getValue();
            if (col.searchTerm != null) {
                return true;
            }
        }
        return false;
    }

    private String getPropKeySearchTerm() {
        return columnMap.get(KEY).searchTerm;
    }

    private String getPropValueSearchTerm() {
        return columnMap.get(VALUE).searchTerm;
    }

    private List<Property> applySorting(List<Property> propsFiltered) {
        Column sortOnColumn = null;
        for (Column column : columnMap.values()) {
            if (column.enableSort) {
                sortOnColumn = column;
                break;
            }
        }

        if (sortOnColumn != null) {
            final boolean sortOnKey = sortOnColumn.name.equals(KEY);
            final boolean isDescending = sortOnColumn.isDescendingSort;

            Collections.sort(propsFiltered, new Comparator<Property>() {
                @Override
                public int compare(Property property, Property property2) {
                    if (sortOnKey) {
                        if (isDescending) {
                            return property2.getName().compareTo(property.getName());
                        }
                        return property.getName().compareTo(property2.getName());
                    } else {
                        // sort on value
                        if (isDescending) {
                            return property2.getValue().compareTo(property.getValue());
                        }
                        return property.getValue().compareTo(property2.getValue());
                    }
                }
            });
        }
        return propsFiltered;
    }
}


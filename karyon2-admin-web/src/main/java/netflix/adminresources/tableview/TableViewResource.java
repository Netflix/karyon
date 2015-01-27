package netflix.adminresources.tableview;

import com.google.gson.JsonArray;

import java.util.List;

public interface TableViewResource {

    List<String> getColumns();

    TableViewResource setColumnSearchTerm(String column, String term);

    TableViewResource setAllColumnsSearchTerm(String term);

    TableViewResource enableColumnSort(String column, boolean isDescending);

    JsonArray getData();

    int getTotalNumOfRecords();

    int getFilteredNumOfRecords();

    TableViewResource setCurrentPageInfo(int startIndex, int count);

}


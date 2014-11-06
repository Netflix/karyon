package netflix.adminresources.tableview;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.core.MultivaluedMap;

public class DataTableHelper {

    public static JsonObject buildOutput(TableViewResource tableViewResource, MultivaluedMap<String, String> queryParams) {
        JsonObject output = new JsonObject();
        applyQueryParams(tableViewResource, queryParams);
        JsonArray data = tableViewResource.getData();

        final String sEcho = queryParams.getFirst("sEcho");
        output.addProperty("iTotalRecords", tableViewResource.getTotalNumOfRecords());
        output.addProperty("iTotalDisplayRecords", tableViewResource.getFilteredNumOfRecords());
        output.addProperty("sEcho", sEcho);
        output.add("aaData", data);
        return output;
    }

    /**
     * apply pagination, search, sort params
     * <p/>
     * Sample query from DataTables -
     * sEcho=1&iColumns=2&sColumns=&iDisplayStart=0&iDisplayLength=25&mDataProp_0=0&mDataProp_1=1&sSearch=&
     * bRegex=false&sSearch_0=&bRegex_0=false&bSearchable_0=true&sSearch_1=&bRegex_1=false&bSearchable_1=true&
     * iSortingCols=1&iSortCol_0=0&sSortDir_0=asc&bSortable_0=true&bSortable_1=true
     */
    private static void applyQueryParams(TableViewResource resource, MultivaluedMap<String, String> queryParams) {

        final String allColsSearch = queryParams.getFirst("sSearch");
        final String displayStart = queryParams.getFirst("iDisplayStart");
        final String displayLen = queryParams.getFirst("iDisplayLength");
        String sortColumnIndex = queryParams.getFirst("iSortCol_0");
        String sortColumnDir = queryParams.getFirst("sSortDir_0");

        if (sortColumnDir == null || sortColumnIndex == null) {
            // defaults
            sortColumnDir = "asc";
            sortColumnIndex = "0";
        }

        int colIndex = Integer.parseInt(sortColumnIndex);
        String sortColumnName = resource.getColumns().get(colIndex);

        if (displayLen != null && displayStart != null) {
            final int iDisplayLen = Integer.parseInt(displayLen);
            final int iDisplayStart = Integer.parseInt(displayStart);

            resource.setAllColumnsSearchTerm(allColsSearch)
                    .setCurrentPageInfo(iDisplayStart, iDisplayLen)
                    .enableColumnSort(sortColumnName, !(sortColumnDir.equalsIgnoreCase("asc")));
        }
    }
}

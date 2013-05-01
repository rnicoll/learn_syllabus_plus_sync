package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TimetablingCloneService extends AbstractCloneService {

    public static final String REPORTING_ACTIVITY_TABLE = "ACTIVITY";
    public static final String REPORTING_MODULE_TABLE = "MODULE";
    public static final String CACHE_ACTIVITY_TABLE = "activity";
    public static final String CACHE_MODULE_TABLE = "module";
    public static final String[][] ACTIVITY_FIELD_MAPPINGS = {
        {"MODUL", "tt_module_id"}
    };
    public static final String[][] MODULE_FIELD_MAPPINGS = {};

    public void cloneActivities(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : ACTIVITY_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_ACTIVITY_TABLE, CACHE_ACTIVITY_TABLE,
                "ID", "tt_activity_id",
                fieldMappings);
    }

    public void cloneModules(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : MODULE_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_MODULE_TABLE, CACHE_MODULE_TABLE,
                "ID", "tt_module_id",
                fieldMappings);
    }
}

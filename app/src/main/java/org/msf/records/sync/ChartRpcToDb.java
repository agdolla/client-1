package org.msf.records.sync;

import android.content.ContentProviderOperation;
import android.content.SyncResult;
import android.util.Log;

import org.joda.time.DateTime;
import org.msf.records.model.ChartGroup;
import org.msf.records.model.ChartStructure;
import org.msf.records.model.Concept;
import org.msf.records.model.ConceptList;
import org.msf.records.model.Encounter;
import org.msf.records.model.PatientChart;

import java.util.ArrayList;
import java.util.Map;

import static org.msf.records.sync.ChartProviderContract.CHART_CONTENT_URI;
import static org.msf.records.sync.ChartProviderContract.CONCEPT_NAMES_CONTENT_URI;
import static org.msf.records.sync.ChartProviderContract.ChartColumns;

/**
 * A helper class for turning the Java beans that are the result of chart RPC calls into
 * appropriate ContentProviderOperations for inserting into the DB.
 */
public class ChartRpcToDb {

    private static final String TAG = "ChartRpcToDb";

    /**
     * Convert a concept response into appropriate inserts in the concept and concept_name tables.
     */
    public static ArrayList<ContentProviderOperation> conceptRpcToDb(ConceptList response,
                                                                     SyncResult syncResult) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (Concept concept : response.results) {
            // This is safe because we have implemented insert on the content provider
            // with replace.
            operations.add(ContentProviderOperation
                    .newInsert(ChartProviderContract.CONCEPTS_CONTENT_URI)
                    .withValue(ChartColumns._ID, concept.uuid)
                    .withValue(ChartColumns.CONCEPT_TYPE, concept.type.name())
                    .build());
            syncResult.stats.numInserts++;
            for (Map.Entry<String, String> entry : concept.names.entrySet()) {
                String locale = entry.getKey();
                if (locale == null) {
                    Log.e(TAG, "null locale in concept name rpc for " + concept);
                    continue;
                }
                String name = entry.getValue();
                if (name == null) {
                    Log.e(TAG, "null name in concept name rpc for " + concept);
                    continue;
                }
                operations.add(ContentProviderOperation
                        .newInsert(CONCEPT_NAMES_CONTENT_URI)
                        .withValue(ChartColumns.CONCEPT_UUID, concept.uuid)
                        .withValue(ChartColumns.LOCALE, locale)
                        .withValue(ChartColumns.NAME, name)
                        .build());
                syncResult.stats.numInserts++;
            }
        }
        return operations;
    }

    /**
     * Convert a ChartStructure response into appropriate inserts in the chart table.
     */
    public static ArrayList<ContentProviderOperation> chartStructureRpcToDb(
            ChartStructure response, SyncResult syncResult) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        String chartUuid = response.uuid;
        if (chartUuid == null) {
            Log.e(TAG, "null chart uuid when fetching chart structure");
        }
        int chartRow = 0;
        String groupUuid;
        for (ChartGroup group : response.groups) {
            groupUuid = group.uuid;
            if (groupUuid == null) {
                Log.e(TAG, "null group uuid for chart " + chartUuid);
                continue;
            }
            for (String conceptUuid : group.concepts) {
                operations.add(ContentProviderOperation
                        .newInsert(CHART_CONTENT_URI)
                        .withValue(ChartColumns.CHART_UUID, chartUuid)
                        .withValue(ChartColumns.CHART_ROW, chartRow++)
                        .withValue(ChartColumns.GROUP_UUID, groupUuid)
                        .withValue(ChartColumns.CONCEPT_UUID, conceptUuid)
                        .build());
                syncResult.stats.numInserts++;
            }
        }
        return operations;
    }

    /**
     * Convert a ChartStructure response into appropriate inserts in the chart table.
     */
    public static ArrayList<ContentProviderOperation> observationsRpcToDb(
            PatientChart response, SyncResult syncResult) {
        final String patientUuid = response.uuid;
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (Encounter encounter : response.encounters) {
            if (encounter.uuid == null) {
                Log.e(TAG, "Encounter uuid was null for " + patientUuid);
                continue;
            }
            final String encounterUuid = encounter.uuid;
            DateTime timestamp = encounter.timestamp;
            if (timestamp == null) {
                Log.e(TAG, "Encounter timestamp was null for " + encounterUuid);
                continue;
            }
            final int encounterTime = (int) (timestamp.getMillis() / 1000); // seconds since epoch
            for (Map.Entry<Object, Object> entry : encounter.observations.entrySet()) {
                final String conceptUuid = (String) entry.getKey();
                operations.add(ContentProviderOperation
                        .newInsert(ChartProviderContract.OBSERVATIONS_CONTENT_URI)
                        .withValue(ChartColumns.PATIENT_UUID, patientUuid)
                        .withValue(ChartColumns.ENCOUNTER_UUID, encounterUuid)
                        .withValue(ChartColumns.ENCOUNTER_TIME, encounterTime)
                        .withValue(ChartColumns.CONCEPT_UUID, conceptUuid)
                        .withValue(ChartColumns.VALUE, entry.getValue().toString())
                        .build());
                syncResult.stats.numInserts++;
            }
        }
        return operations;
    }
}
// Copyright 2015 The Project Buendia Authors
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at: http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distrib-
// uted under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
// OR CONDITIONS OF ANY KIND, either express or implied.  See the License for
// specific language governing permissions and limitations under the License.

package org.projectbuendia.client.providers;

import android.content.ContentResolver;
import android.net.Uri;

import org.projectbuendia.client.BuildConfig;

/** The external contracts for {@link BuendiaProvider}. */
@SuppressWarnings("unused")
public class Contracts {
    public static final String CONTENT_AUTHORITY = BuildConfig.CONTENT_AUTHORITY;
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String TYPE_PACKAGE_PREFIX = "/vnd.projectbuendia.client.";

    /** Names of tables in the local datastore. */
    public enum Table {
        CHART_ITEMS("chart_items"),
        CONCEPT_NAMES("concept_names"),
        CONCEPTS("concepts"),
        FORMS("forms"),
        LOCATION_NAMES("location_names"),
        LOCATIONS("locations"),
        MISC("misc"),
        OBSERVATIONS("observations"),
        ORDERS("orders"),
        PATIENTS("patients"),
        USERS("users"),
        SYNC_TOKENS("sync_tokens");

        public String name;

        public String toString() {
            return name;
        }

        Table(String name) {
            this.name = name;
        }
    }

    // Each interface below corresponds to one SQLite table in the local datastore.  The column
    // names defined in the constants should exactly match the schemas defined in Database.java.

    public interface ChartItems {
        Uri CONTENT_URI = buildContentUri("chart-items");
        String GROUP_CONTENT_TYPE = buildGroupType("chart-item");
        String ITEM_CONTENT_TYPE = buildItemType("chart-item");

        // Sections and items from all charts are stored in this table.  Sections are rows with
        // a section_type and no parent_id; items are rows with a parent_id pointing to a section.
        // To get the description of a chart, filter for a specific chart_uuid and order by weight.

        String ROWID = "rowid";
        String CHART_UUID = "chart_uuid";
        String WEIGHT = "weight";  // sort order
        String SECTION_TYPE = "section_type";  // "TILE_ROW" or "GRID_SECTION" (for sections only)
        String PARENT_ROWID = "parent_rowid";  // null for sections, non-null for items only

        String LABEL = "label";  // label for sections or items
        String TYPE = "type";  // null for sections, rendering type for items only
        String REQUIRED = "required";  // 0 = show in grid if obs exist; 1 = show in grid always
        String CONCEPT_UUIDS = "concept_uuids";  // comma-separated list of concept UUIDs
        String FORMAT = "format";  // format string (see ObsFormat)
        String CAPTION_FORMAT = "caption_format";  // format string for tile caption or grid popup
        String CSS_CLASS = "css_class";  // format string for CSS class on a tile or grid row
        String CSS_STYLE = "css_style";  // format string for CSS properties on a tile or grid row
        String SCRIPT = "script";  // JavaScript for fancy rendering
    }

    public interface ConceptNames {
        Uri CONTENT_URI = buildContentUri("concept-names");
        String GROUP_CONTENT_TYPE = buildGroupType("concept-name");
        String ITEM_CONTENT_TYPE = buildItemType("concept-name");

        String CONCEPT_UUID = "concept_uuid";
        String LOCALE = "locale";  // a language encoded as a java.util.Locale.toString()
        String NAME = "name";
    }

    public interface Concepts {
        Uri CONTENT_URI = buildContentUri("concepts");
        String GROUP_CONTENT_TYPE = buildGroupType("concept");
        String ITEM_CONTENT_TYPE = buildItemType("concept");

        String UUID = "uuid";
        String XFORM_ID = "xform_id";  // ID for the concept in XForms (OpenMRS ID)
        String CONCEPT_TYPE = "concept_type";  // data type name, e.g. NUMERIC, TEXT
    }

    public interface Forms {
        Uri CONTENT_URI = buildContentUri("forms");
        String GROUP_CONTENT_TYPE = buildGroupType("form");
        String ITEM_CONTENT_TYPE = buildItemType("form");

        String UUID = "uuid";
        String NAME = "name";
        String VERSION = "version";
    }

    public interface LocationNames {
        Uri CONTENT_URI = buildContentUri("location-names");
        String GROUP_CONTENT_TYPE = buildGroupType("location-name");
        String ITEM_CONTENT_TYPE = buildItemType("location-name");

        String LOCATION_UUID = "location_uuid";
        String LOCALE = "locale";  // a language encoded as a java.util.Locale.toString()
        String NAME = "name";
    }

    public interface Locations {
        Uri CONTENT_URI = buildContentUri("locations");
        String GROUP_CONTENT_TYPE = buildGroupType("location");
        String ITEM_CONTENT_TYPE = buildItemType("location");

        String UUID = "uuid";
        String PARENT_UUID = "parent_uuid"; // parent location or null
    }

    public interface Misc {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("misc").appendPath("0").build();
        String ITEM_CONTENT_TYPE = buildItemType("misc");

        /**
         * The start time of the last full sync operation, according to the
         * local (client's) clock.  Since sync operations are transactional,
         * this should only be set if this sync was completed successfully.
         * <p/>
         * <p>Updated at the very beginning of full sync operations.
         */
        String FULL_SYNC_START_MILLIS = "full_sync_start_millis";

        /**
         * The end time of the last full sync operation, according to the
         * local (client's) clock.  In rare cases, this may correspond to a
         * sync that completed but downloaded incomplete data.
         * <p/>
         * <p>Updated at the very end of full sync operations.
         */
        String FULL_SYNC_END_MILLIS = "full_sync_end_millis";

    }

    public interface SyncTokens {
        Uri CONTENT_URI = buildContentUri("sync-tokens");
        String ITEM_CONTENT_TYPE = buildItemType("sync-token");
        String TABLE_NAME = "table_name";
        String SYNC_TOKEN = "sync_token";
    }

    public interface Observations {
        Uri CONTENT_URI = buildContentUri("observations");
        String GROUP_CONTENT_TYPE = buildGroupType("observation");
        String ITEM_CONTENT_TYPE = buildItemType("observation");

        /**
         * UUID is populated if the record was retrieved from the server. If this observation was
         * written locally as a cached value from a submitted XForm, UUID is null. As part of every
         * successful sync, all observations with null UUIDs are deleted, on the basis that an
         * authoritative version for each has been obtained from the server.
         */
        String UUID = "uuid";
        String PATIENT_UUID = "patient_uuid";
        String ENCOUNTER_UUID = "encounter_uuid";
        String ENCOUNTER_MILLIS = "encounter_millis";  // milliseconds since epoch
        String CONCEPT_UUID = "concept_uuid";
        String ENTERER_UUID = "enterer_uuid";
        String VALUE = "value";  // concept value or order UUID
    }

    public interface Orders {
        Uri CONTENT_URI = buildContentUri("orders");
        String GROUP_CONTENT_TYPE = buildGroupType("order");
        String ITEM_CONTENT_TYPE = buildItemType("order");

        String UUID = "uuid";
        String PATIENT_UUID = "patient_uuid";
        String INSTRUCTIONS = "instructions";
        String START_MILLIS = "start_millis";  // milliseconds since epoch
        String STOP_MILLIS = "stop_millis";  // milliseconds since epoch
    }

    public interface Patients {
        Uri CONTENT_URI = buildContentUri("patients");
        String GROUP_CONTENT_TYPE = buildGroupType("patient");
        String ITEM_CONTENT_TYPE = buildItemType("patient");

        String UUID = "uuid";
        String ID = "id";
        String GIVEN_NAME = "given_name";
        String FAMILY_NAME = "family_name";
        String LOCATION_UUID = "location_uuid";
        String BIRTHDATE = "birthdate";  // a local date in yyyy-mm-dd format
        String GENDER = "gender";
    }

    public interface Users {
        Uri CONTENT_URI = buildContentUri("users");
        String GROUP_CONTENT_TYPE = buildGroupType("user");
        String ITEM_CONTENT_TYPE = buildItemType("user");

        String UUID = "uuid";
        String FULL_NAME = "full_name";
    }

    // Each interface below describes a derived view implemented by a custom
    // ProviderDelegate.  The column name constants should match the columns
    // returned by the query() method of the corresponding ProviderDelegate.

    public interface LocalizedLocations {
        Uri CONTENT_URI = buildContentUri("localized-locations");
        String GROUP_CONTENT_TYPE = buildGroupType("localized-location");
        String ITEM_CONTENT_TYPE = buildItemType("localized-location");

        String UUID = "uuid";
        String PARENT_UUID = "parent_uuid";
        String NAME = "name";
        /** Patient count for a location, not including child locations. */
        String PATIENT_COUNT = "patient_count";
    }

    public interface PatientCounts {
        Uri CONTENT_URI = buildContentUri("patient-counts");
        String GROUP_CONTENT_TYPE = buildGroupType("patient-count");
        String ITEM_CONTENT_TYPE = buildItemType("patient-count");

        String LOCATION_UUID = "location_uuid";
        String PATIENT_COUNT = "patient_count";
    }

    public static Uri buildContentUri(String path) {
        return BASE_CONTENT_URI.buildUpon().appendPath(path).build();
    }

    public static String buildGroupType(String name) {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + TYPE_PACKAGE_PREFIX + name;
    }

    public static String buildItemType(String name) {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + TYPE_PACKAGE_PREFIX + name;
    }

    /** Returns the content URI for the localized locations for a given locale. */
    public static Uri getLocalizedLocationsUri(String locale) {
        return LocalizedLocations.CONTENT_URI.buildUpon()
            .appendPath(locale)
            .build();
    }

    private Contracts() { /* prevent instantiation */ }
}

package org.msf.records.sync.providers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.msf.records.sync.PatientDatabase;

import java.util.List;

/**
 * A {@link ProviderDelegate} that provides query access to all localized locations.
 */
public class LocalizedLocationsDelegate implements ProviderDelegate<PatientDatabase> {

    /**
     * Query that fetches localized location information for a given locale.
     *
     * <p>Parameters:
     * <ul>
     *     <li>string, the locale in which the location information should be returned</li>
     * </ul>
     *
     * <p>Result Columns:
     * <ul>
     *     <li>string location_uuid, the UUID of a location</li>
     *     <li>string parent_uuid, the UUID of the location's parent</li>
     *     <li>string name, the localized name of the location</li>
     * </ul>
     */
    private static final String QUERY = ""
            + "SELECT\n"
            + "  locations.location_uuid as location_uuid,\n"
            + "  locations.parent_uuid as parent_uuid,\n"
            + "  location_names.name as name\n"
            + "FROM locations\n"
            + "  INNER JOIN location_names\n"
            + "    ON locations.location_uuid = location_names.location_uuid\n"
            + "WHERE\n"
            + "  location_names.locale = ?\n";

    @Override
    public String getType() {
        return Contracts.LocalizedLocations.GROUP_CONTENT_TYPE;
    }

    @Override
    public Cursor query(
            PatientDatabase dbHelper, ContentResolver contentResolver, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        // URI expected to be of form ../localized-locations/{locale}.
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 2) {
            throw new UnsupportedOperationException("URI '" + uri + "' is malformed.");
        }

        String locale = pathSegments.get(1);
        return dbHelper.getReadableDatabase().rawQuery(QUERY, new String[] { locale });
    }

    @Override
    public Uri insert(
            PatientDatabase dbHelper, ContentResolver contentResolver, Uri uri,
            ContentValues values) {
        throw new UnsupportedOperationException("Insert is not supported for URI '" + uri + "'.");
    }

    @Override
    public int bulkInsert(
            PatientDatabase dbHelper, ContentResolver contentResolver, Uri uri,
            ContentValues[] values) {
        throw new UnsupportedOperationException(
                "Bulk insert is not supported for URI '" + uri + "'.");
    }

    @Override
    public int delete(
            PatientDatabase dbHelper, ContentResolver contentResolver, Uri uri, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete is not supported for URI '" + uri + "'.");
    }

    @Override
    public int update(
            PatientDatabase dbHelper, ContentResolver contentResolver, Uri uri,
            ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update is not supported for URI '" + uri + "'.");
    }
}
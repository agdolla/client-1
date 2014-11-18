package org.msf.records.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonObject;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A connection to the Xform handling module we are adding to OpenMRS to provide xforms.
 * This is not part of OpenMrsServer as it has entirely it's own interface, but may be merged in
 * future.
 *
 * @author nfortescue@google.com
 */
public class OpenMrsXformsConnection {
    private static final String TAG = "OpenMrsXformsConnection";

    private final VolleySingleton mVolley;
    private final String mRootUrl;
    private final String mUserName;
    private final String mPassword;

    public OpenMrsXformsConnection(Context context,
                                   @Nullable String rootUrl,
                                   @Nullable String userName,
                                   @Nullable String password) {
        this.mVolley = VolleySingleton.getInstance(context.getApplicationContext());
        mRootUrl = (rootUrl == null) ? Constants.API_URL : rootUrl;
        mUserName = (userName == null) ? Constants.LOCAL_ADMIN_USERNAME : userName;
        mPassword = (password == null) ? Constants.LOCAL_ADMIN_PASSWORD : password;
    }

    /**
     * Get a single (full) Xform from the OpenMRS server
     * @param uuid the uuid of the form to fetch
     * @param resultListener the listener to be informed of the form asynchronously
     * @param errorListener a listener to be informed of any errors
     */
    public void getXform(String uuid, final Response.Listener<String> resultListener,
                          Response.ErrorListener errorListener) {
        Request request = new OpenMrsJsonRequest(
                mUserName, mPassword,
                mRootUrl + "/xform/" + uuid + "?v=full",
                null, // null implies GET
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String xml = response.getString("xml");

                            // TODO(akalachman): This is hacky and bad but handles a server bug.
                            xml = xml.replaceFirst("xmlns:openmrs=\".*\"", "");

                            resultListener.onResponse(xml);
                        } catch (JSONException e) {
                            // The result was not in the expected format. Just log, and return
                            // results so far.
                            Log.e(TAG, "response was in bad format: " + response, e);
                        }
                    }
                }, errorListener
        );
        mVolley.addToRequestQueue(request, TAG);
    }

    /**
     * List all xforms on the server, but not their contents.
     * @param listener a listener to be told about the index entries for all forms asynchronously.
     * @param errorListener a listener to be told about any errors.
     */
    public void listXforms(final Response.Listener<List<OpenMrsXformIndexEntry>> listener,
                           final Response.ErrorListener errorListener) {
        Request request = new OpenMrsJsonRequest(
                mUserName, mPassword,
                mRootUrl + "/xform", // list all forms
                null, // null implies GET
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "got forms: " + response);
                        ArrayList<OpenMrsXformIndexEntry> result = new ArrayList<>();
                        try {
                            // This seems quite code heavy (parsing manually), but is reasonably
                            // efficient as we only look at the fields we need, so we are robust to
                            // changes in the rest of the object.
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject entry = results.getJSONObject(i);

                                // Sometimes date_changed is not set; in this case, date_changed is
                                // simply date_created.
                                long date_changed;
                                if (entry.get("date_changed") == JSONObject.NULL) {
                                    date_changed = entry.getLong("date_created");
                                } else {
                                    date_changed = entry.getLong("date_changed");
                                }

                                OpenMrsXformIndexEntry indexEntry = new OpenMrsXformIndexEntry(
                                        entry.getString("uuid"),
                                        entry.getString("name"),
                                        date_changed);
                                result.add(indexEntry);
                            }
                        } catch (JSONException e) {
                            // The result was not in the expected format. Just log, and return
                            // results so far.
                            Log.e(TAG, "response was in bad format: " + response, e);
                        }
                        Log.i(TAG, "returning response: " + response);
                        listener.onResponse(result);
                    }
                },
                errorListener
        );
        mVolley.addToRequestQueue(request, TAG);
    }

    /**
     * Get a single (full) Xform from the OpenMRS server
     *
     * @param patientId null if this is to add a new patient, non-null for observation on existing
     *                  patient
     * @param resultListener the listener to be informed of the form asynchronously
     * @param errorListener a listener to be informed of any errors
     */
    public void postXformInstance(
            @Nullable String patientId,
            String xform,
            final Response.Listener<JSONObject> resultListener,
            Response.ErrorListener errorListener) {

        // The JsonObject members in the API as written at the moment.
        // int "patient_id"
        // int "enterer_id"
        // String "date_entered"
        // String "xml" the form.
        JsonObject post = new JsonObject();
        post.addProperty("xml", xform);
        // Don't add patient property for create new patient
        if (patientId != null) {
            post.addProperty("patient_id", patientId);
        }
        // TODO(nfortescue): get the enterer from the user login
        post.addProperty("enterer_id", 1);

        post.addProperty("date_entered", ISODateTimeFormat.basicDateTime().print(new DateTime()));
        JSONObject postBody = null;
        try {
            postBody = new JSONObject(post.toString());
        } catch (JSONException e) {
            Log.e(TAG, "This should never happen converting one JSON object to another. " + post, e);
            errorListener.onErrorResponse(new VolleyError("failed to convert to JSON", e));
        }
        OpenMrsJsonRequest request = new OpenMrsJsonRequest(
                mUserName, mPassword,
                mRootUrl + "/xforminstance",
                postBody, // non-null implies POST
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        resultListener.onResponse(response);
                    }
                }, errorListener
        );
        mVolley.addToRequestQueue(request, TAG);
    }

}

package org.msf.records.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.msf.records.App;
import org.msf.records.R;
import org.msf.records.events.PatientLocationEditedEvent;
import org.msf.records.model.Location2;
import org.msf.records.view.InstantAutoCompleteTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A {@link DialogFragment} for editing a user's assigned location.
 */
public class EditAssignedLocationDialogFragment extends DialogFragment {

    private static final String LOCATION_KEY = "location";

    public static EditAssignedLocationDialogFragment newInstance(Location2 location) {
        EditAssignedLocationDialogFragment fragment = new EditAssignedLocationDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(LOCATION_KEY, location);
        fragment.setArguments(args);
        return fragment;
    }

    @InjectView(R.id.zone) InstantAutoCompleteTextView mZone;
    @InjectView(R.id.tent) InstantAutoCompleteTextView mTent;
    @InjectView(R.id.bed) InstantAutoCompleteTextView mBed;

    private LayoutInflater mInflater;
    private Location2 mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());
        mLocation = getArguments().getParcelable(LOCATION_KEY);
    }
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View fragment = inflater.inflate(R.layout.dialog_fragment_edit_assigned_location, null);
//        ButterKnife.inject(this, fragment);
//
//        mZone.setText(mLocation.getZone());
//        mTent.setText(mLocation.getTent());
//        mBed.setText(mLocation.getBed());
//
//        getDialog().setTitle("Edit Patient Location");
//
//        return fragment;
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View fragment = mInflater.inflate(R.layout.dialog_fragment_edit_assigned_location, null);
        ButterKnife.inject(this, fragment);

        mZone.setText(mLocation.getZone());
        mTent.setText(mLocation.getTent());
        mBed.setText(mLocation.getBed());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.title_edit_patient_location))
                .setPositiveButton(
                        getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                App.getMainThreadBus().post(
                                        new PatientLocationEditedEvent(Location2.create(
                                                mZone.getText().toString(),
                                                mTent.getText().toString(),
                                                mBed.getText().toString()
                                        ))
                                );
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .setView(fragment);

        return dialogBuilder.create();
    }
}
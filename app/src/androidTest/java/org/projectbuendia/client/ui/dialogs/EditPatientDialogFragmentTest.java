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

package org.projectbuendia.client.ui.dialogs;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import org.projectbuendia.client.R;
import org.projectbuendia.client.ui.FunctionalTestCase;

import java.util.Date;
import java.util.Random;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

/** Tests for adding a new patient. */
public class EditPatientDialogFragmentTest extends FunctionalTestCase {

    /**
     * Tests adding a new patient;
     * Tests adding a location to the patient;
     * Test symptom date;
     * Test last observation date;
     * Test admission date;
     */
    public void testNewPatient() {

        // Create the patient
        inUserLoginGoToPatientCreation();
        screenshot("Test Start");
        String id = Long.toString(new Date().getTime()%100000);
        populateNewPatientFields(id);
        click(viewWithText("OK"));
        waitForProgressFragment();
        screenshot("On Patient Chart");

        // Assign a location to the patient
        click(viewWithId(R.id.attribute_location));
        screenshot("After Location Dialog Shown");
        click(viewWithText(LOCATION_NAME));
        screenshot("After Location Selected");

        pressBack();

        // The new patient should be visible in the list for their location
        click(viewWithText(LOCATION_NAME));
        screenshot("In " + LOCATION_NAME);
        inPatientListClickPatientWithId(id);
        screenshot("After Patient Clicked");

        // The symptom onset date should not be assigned a default value.
        expectVisible(viewThat(
            hasAncestorThat(withId(R.id.attribute_symptoms_onset_days)),
            hasText("–")));

        // The admission date should be visible right after adding a patient.
        // Flaky because of potential periodic syncs.
        expectVisibleWithin(399999, viewThat(
            hasAncestorThat(withId(R.id.attribute_admission_days)),
            hasText("Day 1")));

        // The last observation should be today.
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM d, yyyy");
        expectVisibleWithin(399999, viewThat(
            withId(R.id.patient_chart_last_observation_date_time),
            hasTextContaining(formatter.print(DateTime.now()))));
    }

    /** Test Age Validation (cannot be more then 120 years). */
    public void testAgeValidation(){
        inUserLoginGoToPatientCreation();
        screenshot("Test Start");
        chooseSex();
        type("120", viewWithId(R.id.patient_age_years));
        click(viewWithText("OK"));
        screenshot("After OK Pressed");
        waitForProgressFragment();
        onView(withId(R.id.patient_age_years)).check(matches(editTextWithError(
            getActivity().getString(R.string.age_limit))));
    }

    /** Patients cannot be created without sex. */
    public void testSexValidation(){
        inUserLoginGoToPatientCreation();
        screenshot("Test Start");
        click(viewWithText("OK"));
        screenshot("After OK Pressed");
        expectVisible(viewThat(hasTextContaining(getActivity().getString(R.string.sex_cannot_be_null))));
    }

    /** Populates all the fields on the New Patient screen. */
    private void populateNewPatientFields(String id) {
        screenshot("Before Patient Populated");
        String given = "Given" + id;
        String family = "Family" + id;
        type(id, viewWithId(R.id.patient_id));
        type(given, viewWithId(R.id.patient_given_name));
        type(family, viewWithId(R.id.patient_family_name));
        type(id.substring(id.length() - 2), viewWithId(R.id.patient_age_years));
        type(id.substring(id.length() - 2), viewWithId(R.id.patient_age_months));
        chooseSex();
        screenshot("After Patient Populated");
    }

    /** Randomly choose a sex for the patient */
    private void chooseSex(){
        Random rand = new Random();
        int randomNum = rand.nextInt((2 - 1) + 1) + 1;

        if(randomNum == 1){
            click(viewWithId(R.id.patient_sex_male));
        }
        else if(randomNum == 2){
            click(viewWithId(R.id.patient_sex_female));
        }
    }
}

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

package org.msf.records.filter.db.encounter;

import org.msf.records.data.app.AppEncounter;
import org.msf.records.filter.db.SimpleSelectionFilter;
import org.msf.records.sync.providers.Contracts;

/** Matches only the encounter with the given UUID. */
public final class EncounterUuidFilter extends SimpleSelectionFilter<AppEncounter> {

    @Override
    public String getSelectionString() {
        return Contracts.ObservationColumns.ENCOUNTER_UUID + "=?";
    }

    @Override
    public String[] getSelectionArgs(CharSequence constraint) {
        return new String[] { constraint.toString() };
    }
}

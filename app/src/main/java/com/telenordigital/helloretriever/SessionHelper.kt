package com.telenordigital.helloretriever

// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.support.customtabs.CustomTabsSession

import java.lang.ref.WeakReference

/**
 * A class that keeps tracks of the current [CustomTabsSession] and helps other components of
 * the app to get access to the current session.
 */
object SessionHelper {
    private var sCurrentSession: WeakReference<CustomTabsSession>? = null

    /**
     * @return The current [CustomTabsSession] object.
     */
    /**
     * Sets the current session to the given one.
     * @param session The current session.
     */
    var currentSession: CustomTabsSession?
        get() = if (sCurrentSession == null) null else sCurrentSession!!.get()
        set(session) {
            sCurrentSession = WeakReference<CustomTabsSession>(session)
        }
}
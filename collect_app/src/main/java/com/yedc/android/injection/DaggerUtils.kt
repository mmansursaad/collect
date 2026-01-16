/*
 * Copyright (C) 2018 Callum Stott
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yedc.android.injection

import android.content.Context

object DaggerUtils {

    @JvmStatic
    fun getComponent(context: Context): _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent {
        val component = (context.applicationContext as _root_ide_package_.com.yedc.android.application.Collect).component
        if (component != null) {
            return component
        } else {
            throw IllegalStateException("Collect.applicationComponent is null!")
        }
    }
}

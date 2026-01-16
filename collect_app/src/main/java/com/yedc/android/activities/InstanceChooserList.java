/*
 * Copyright (C) 2009 University of Washington
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

package com.yedc.android.activities;

import static com.yedc.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static com.yedc.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static com.yedc.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static com.yedc.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.yedc.android.R;
import com.yedc.android.adapters.InstanceListCursorAdapter;
import com.yedc.android.dao.CursorLoaderFactory;
import com.yedc.android.formentry.FormOpeningMode;
import com.yedc.db.sqlite.DatabaseConnection;
import com.yedc.android.database.instances.DatabaseInstanceColumns;
import com.yedc.android.entities.EntitiesRepositoryProvider;
import com.yedc.android.external.FormUriActivity;
import com.yedc.android.external.InstancesContract;
import com.yedc.android.formlists.sorting.FormListSortingOption;
import com.yedc.android.formmanagement.drafts.BulkFinalizationViewModel;
import com.yedc.android.formmanagement.drafts.DraftsMenuProvider;
import com.yedc.android.injection.DaggerUtils;
import com.yedc.android.instancemanagement.FinalizeAllSnackbarPresenter;
import com.yedc.android.instancemanagement.InstancesDataService;
import com.yedc.android.projects.ProjectsDataService;
import com.yedc.android.utilities.FormsRepositoryProvider;
import com.yedc.android.utilities.InstancesRepositoryProvider;
import com.yedc.androidshared.ui.multiclicksafe.MultiClickGuard;
import com.yedc.async.Scheduler;
import com.yedc.lists.EmptyListView;
import com.yedc.material.MaterialProgressDialogFragment;
import com.yedc.settings.SettingsProvider;

import java.util.Arrays;

import javax.inject.Inject;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 * @deprecated Uses {@link CursorLoaderFactory} and interacts with {@link DatabaseConnection} on the
 * UI thread.
 */
@Deprecated
public class InstanceChooserList extends AppListActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String INSTANCE_LIST_ACTIVITY_SORTING_ORDER = "instanceListActivitySortingOrder";
    private static final String VIEW_SENT_FORM_SORTING_ORDER = "ViewSentFormSortingOrder";

    private boolean editMode;

    @Inject
    ProjectsDataService projectsDataService;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    Scheduler scheduler;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    EntitiesRepositoryProvider entitiesRepositoryProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    InstancesDataService instancesDataService;

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        setResult(RESULT_OK, result.getData());
        finish();
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_chooser_list);
        DaggerUtils.getComponent(this).inject(this);

        String formMode = getIntent().getStringExtra(FormOpeningMode.FORM_MODE_KEY);
        if (formMode == null || FormOpeningMode.EDIT_SAVED.equalsIgnoreCase(formMode)) {
            setTitle(getString(com.yedc.strings.R.string.review_data));
            editMode = true;
        } else {
            setTitle(getString(com.yedc.strings.R.string.view_sent_forms));
            EmptyListView emptyListView = findViewById(android.R.id.empty);
            emptyListView.setIcon(R.drawable.ic_baseline_inbox_72);
            emptyListView.setTitle(getString(com.yedc.strings.R.string.empty_list_of_sent_forms_title));
            emptyListView.setSubtitle(getString(com.yedc.strings.R.string.empty_list_of_sent_forms_subtitle));
        }

        sortingOptions = Arrays.asList(
                new FormListSortingOption(
                        R.drawable.ic_sort_by_alpha,
                        com.yedc.strings.R.string.sort_by_name_asc
                ),
                new FormListSortingOption(
                        R.drawable.ic_sort_by_alpha,
                        com.yedc.strings.R.string.sort_by_name_desc
                ),
                new FormListSortingOption(
                        R.drawable.ic_access_time,
                        com.yedc.strings.R.string.sort_by_date_desc
                ),
                new FormListSortingOption(
                        R.drawable.ic_access_time,
                        com.yedc.strings.R.string.sort_by_date_asc
                )
        );

        init();

        BulkFinalizationViewModel bulkFinalizationViewModel = new BulkFinalizationViewModel(
                projectsDataService.requireCurrentProject().getUuid(),
                scheduler,
                instancesDataService,
                settingsProvider
        );

        MaterialProgressDialogFragment.showOn(this, bulkFinalizationViewModel.isFinalizing(), getSupportFragmentManager(), () -> {
            MaterialProgressDialogFragment dialog = new MaterialProgressDialogFragment();
            dialog.setMessage("Finalizing drafts...");
            return dialog;
        });

        if (bulkFinalizationViewModel.isEnabled() && editMode) {
            DraftsMenuProvider draftsMenuProvider = new DraftsMenuProvider(this, bulkFinalizationViewModel::finalizeAllDrafts);
            addMenuProvider(draftsMenuProvider, this);
            bulkFinalizationViewModel.getDraftsCount().observe(this, draftsCount -> {
                draftsMenuProvider.setDraftsCount(draftsCount);
                invalidateMenu();
            });

            bulkFinalizationViewModel.getFinalizedForms().observe(
                    this,
                    new FinalizeAllSnackbarPresenter(this.findViewById(android.R.id.content), this)
            );
        }
    }

    private void init() {
        setupAdapter();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            if (view.isEnabled()) {
                Cursor c = (Cursor) listView.getAdapter().getItem(position);
                long instanceId = c.getLong(c.getColumnIndex(DatabaseInstanceColumns._ID));
                Uri instanceUri = InstancesContract.getUri(projectsDataService.requireCurrentProject().getUuid(), instanceId);

                String action = getIntent().getAction();
                if (Intent.ACTION_PICK.equals(action)) {
                    // caller is waiting on a picked form
                    setResult(RESULT_OK, new Intent().setData(instanceUri));
                    finish();
                } else {
                    // caller wants to view/edit a form, so launch FormFillingActivity
                    Intent parentIntent = this.getIntent();
                    Intent intent = new Intent(this, FormUriActivity.class);
                    intent.setAction(Intent.ACTION_EDIT);
                    intent.setData(instanceUri);
                    String formMode = parentIntent.getStringExtra(FormOpeningMode.FORM_MODE_KEY);
                    if (formMode == null || FormOpeningMode.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                        intent.putExtra(FormOpeningMode.FORM_MODE_KEY, FormOpeningMode.EDIT_SAVED);
                        formLauncher.launch(intent);
                    } else {
                        intent.putExtra(FormOpeningMode.FORM_MODE_KEY, FormOpeningMode.VIEW_SENT);
                        startActivity(intent);
                        finish();
                    }
                }
            } else {
                TextView disabledCause = view.findViewById(R.id.form_subtitle2);
                Toast.makeText(this, disabledCause.getText(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupAdapter() {
        String[] data = {DatabaseInstanceColumns.DISPLAY_NAME, DatabaseInstanceColumns.DELETED_DATE};
        int[] view = {R.id.form_title, R.id.form_subtitle2};

        boolean shouldCheckDisabled = !editMode;
        listAdapter = new InstanceListCursorAdapter(
                this, R.layout.form_chooser_list_item, null, data, view, shouldCheckDisabled);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected String getSortingOrderKey() {
        return editMode ? INSTANCE_LIST_ACTIVITY_SORTING_ORDER : VIEW_SENT_FORM_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();
        if (editMode) {
            return new CursorLoaderFactory(projectsDataService).createEditableInstancesCursorLoader(getFilterText(), getSortingOrder());
        } else {
            return new CursorLoaderFactory(projectsDataService).createSentInstancesCursorLoader(getFilterText(), getSortingOrder());
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarAndAllow();
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        listAdapter.swapCursor(null);
    }

    protected String getSortingOrder() {
        String sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case BY_NAME_DESC:
                sortingOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case BY_DATE_ASC:
                sortingOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortingOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
        }
        return sortingOrder;
    }
}

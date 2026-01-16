package com.yedc.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.yedc.android.database.DatabaseObjectMapper;
import com.yedc.android.formlists.savedformlist.SelectableSavedFormListItemViewHolder;
import com.yedc.android.storage.StoragePathProvider;
import com.yedc.android.storage.StorageSubdirectory;
import com.yedc.forms.instances.Instance;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class InstanceUploaderAdapter extends CursorAdapter {
    private final Consumer<Long> onItemCheckboxClickListener;
    private Set<Long> selected = new HashSet<>();

    public InstanceUploaderAdapter(Context context, Cursor cursor, Consumer<Long> onItemCheckboxClickListener) {
        super(context, cursor);
        this.onItemCheckboxClickListener = onItemCheckboxClickListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        SelectableSavedFormListItemViewHolder viewHolder = new SelectableSavedFormListItemViewHolder(parent);
        viewHolder.itemView.setTag(viewHolder);
        return viewHolder.itemView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SelectableSavedFormListItemViewHolder viewHolder = (SelectableSavedFormListItemViewHolder) view.getTag();
        Instance instance = DatabaseObjectMapper.getInstanceFromCurrentCursorPosition(cursor, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES));
        viewHolder.setItem(instance);

        long dbId = instance.getDbId();
        viewHolder.getCheckbox().setChecked(selected.contains(dbId));
        viewHolder.setOnDetailsClickListener(() -> {
            onItemCheckboxClickListener.accept(dbId);
            return null;
        });
    }

    public void setSelected(Set<Long> ids) {
        this.selected = ids;
        notifyDataSetChanged();
    }
}

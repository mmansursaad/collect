package com.yedc.android.formlists.savedformlist

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.yedc.android.R
import com.yedc.lists.RecyclerViewUtils.matchParentWidth
import com.yedc.lists.selects.MultiSelectAdapter

class SelectableSavedFormListItemViewHolder(parent: ViewGroup) :
    MultiSelectAdapter.ViewHolder<com.yedc.forms.instances.Instance>(
        SavedFormListItemView(parent.context)
    ) {
    private var selectView = itemView

    init {
        matchParentWidth()
    }

    override fun setItem(item: com.yedc.forms.instances.Instance) {
        (itemView as SavedFormListItemView).setItem(item)
    }

    override fun getCheckbox(): CheckBox {
        return (itemView as SavedFormListItemView).binding.checkbox
    }

    override fun getSelectArea(): View {
        return selectView
    }

    fun setOnDetailsClickListener(listener: () -> Unit) {
        selectView = itemView.findViewById(R.id.selectView)
        selectView.setOnClickListener { listener() }
    }
}

package com.yedc.android.formlists.savedformlist

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.yedc.android.R
import com.yedc.android.databinding.FormChooserListItemMultipleChoiceBinding
import com.yedc.android.instancemanagement.getIcon
import com.yedc.android.instancemanagement.getStatusDescription
import com.yedc.android.instancemanagement.userVisibleInstanceName
import java.util.Date

class SavedFormListItemView(context: Context) : FrameLayout(context) {

    val binding =
        FormChooserListItemMultipleChoiceBinding.inflate(LayoutInflater.from(context), this, true)

    fun setItem(value: com.yedc.forms.instances.Instance) {
        val lastStatusChangeDate = value.lastStatusChangeDate
        val status = value.status

        binding.root.findViewById<TextView>(R.id.form_title).text = value.userVisibleInstanceName(context.resources)
        binding.root.findViewById<TextView>(R.id.form_subtitle).text =
            getStatusDescription(context, status, Date(lastStatusChangeDate))

        val statusIcon = binding.root.findViewById<ImageView>(R.id.image)
        statusIcon.setImageResource(value.getIcon())
    }
}

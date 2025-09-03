package com.jed.optima.android.formlists.blankformlist

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.jed.optima.android.databinding.BlankFormListItemBinding
import com.jed.optima.strings.R.string
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class BlankFormListItemView(context: Context) : FrameLayout(context) {

    val binding = BlankFormListItemBinding.inflate(LayoutInflater.from(context), this, true)

    fun setItem(item: BlankFormListItem) {
        binding.formTitle.text = item.formName

        binding.formVersion.text =
            binding.root.context.getString(
                string.version_number,
                item.formVersion
            )
        //binding.formVersion.visibility = GONE
        binding.formVersion.visibility =
            if (item.formVersion.isNotBlank()) VISIBLE else GONE

        binding.formId.text =
            binding.root.context.getString(
                string.id_number,
                item.formId
            )
        //binding.formId.visibility = GONE

        binding.formHistory.text = try {
            if (item.dateOfLastDetectedAttachmentsUpdate != null) {
                SimpleDateFormat(
                    binding.root.context.getString(string.updated_on_date_at_time),
                    Locale.getDefault()
                ).format(item.dateOfLastDetectedAttachmentsUpdate)
            } else {
                SimpleDateFormat(
                    binding.root.context.getString(string.added_on_date_at_time),
                    Locale.getDefault()
                ).format(item.dateOfCreation)
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            ""
        }
    }

    fun setTrailingView(layoutId: Int) {
        inflate(context, layoutId, binding.trailingView)
    }
}

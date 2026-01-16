package com.yedc.android.formentry

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.yedc.android.R
import com.yedc.android.databinding.FormEntryEndBinding
import com.yedc.androidshared.system.ContextUtils
import com.yedc.strings.localization.getLocalizedString

class FormEndView(
    context: Context,
    formTitle: String,
    isFormEditableAfterFinalization: Boolean,
    formEndViewModel: FormEndViewModel,
    private val listener: Listener
) : SwipeHandler.View(context) {

    private val binding = FormEntryEndBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.description.text = context.getString(com.yedc.strings.R.string.save_enter_data_description /*formTitle*/)

        binding.saveAsDraft.isVisible = formEndViewModel.isSaveDraftEnabled()
        binding.saveAsDraft.setOnClickListener {
            listener.onSaveClicked(false)
        }

        binding.finalize.isVisible = formEndViewModel.isFinalizeEnabled()
        binding.finalize.setOnClickListener {
            listener.onSaveClicked(true)
        }

        binding.divider.isVisible = binding.saveAsDraft.isVisible && binding.finalize.isVisible

        val shouldFormBeSentAutomatically = formEndViewModel.shouldFormBeSentAutomatically()
        if (shouldFormBeSentAutomatically) {
            binding.finalize.text = context.getString(com.yedc.strings.R.string.send)
        }

        if (binding.saveAsDraft.isVisible && binding.finalize.isVisible) {
            if (shouldFormBeSentAutomatically) {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = com.yedc.strings.R.string.form_editing_enabled_after_sending,
                        hint = com.yedc.strings.R.string.form_editing_enabled_after_sending_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = com.yedc.strings.R.string.form_editing_disabled_after_sending,
                        hint = com.yedc.strings.R.string.form_editing_disabled_hint
                    )
                }
            } else {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = com.yedc.strings.R.string.form_editing_enabled_after_finalizing,
                        hint = com.yedc.strings.R.string.form_editing_enabled_after_finalizing_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = com.yedc.strings.R.string.form_editing_disabled_after_finalizing,
                        hint = com.yedc.strings.R.string.form_editing_disabled_hint
                    )
                }
            }
        } else if (binding.finalize.isVisible) {
            if (shouldFormBeSentAutomatically) {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = com.yedc.strings.R.string.form_editing_enabled_after_sending,
                        hint = com.yedc.strings.R.string.form_editing_enabled_after_sending_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = com.yedc.strings.R.string.form_editing_disabled_after_sending,
                        hint = null
                    )
                }
            } else {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = com.yedc.strings.R.string.form_editing_enabled_after_finalizing,
                        hint = com.yedc.strings.R.string.form_editing_enabled_after_finalizing_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = com.yedc.strings.R.string.form_editing_disabled_after_finalizing,
                        hint = null
                    )
                }
            }
        } else {
            binding.formEditsWarning.visibility = View.GONE
        }
    }

    private fun setWarning(icon: Int, title: Int, hint: Int?) {
        binding.formEditsIcon.setImageResource(icon)
        binding.formEditsWarningTitle.setText(title)
        binding.formEditsWarningMessage.apply {
            text = SpannableStringBuilder().apply {
                if (hint != null) {
                    append(context.getLocalizedString(hint))
                    append(" ")
                }
                append(getLearnMoreLink())
            }
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun getLearnMoreLink(): SpannableStringBuilder {
        return SpannableStringBuilder().inSpans(
            span = object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(context, com.yedc.webpage.WebViewActivity::class.java)
                    intent.putExtra("url", "https://forum.getodk.org/t/42007")
                    context.startActivity(intent)
                }
            },
            builderAction = {
                inSpans(
                    span = TextAppearanceSpan(context, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium),
                    builderAction = {
                        color(ContextUtils.getThemeAttributeValue(context, com.google.android.material.R.attr.colorAccent)) {
                            underline {
                                append(context.getLocalizedString(com.yedc.strings.R.string.form_edits_warning_learn_more))
                            }
                        }
                    }
                )
            }
        )
    }

    override fun shouldSuppressFlingGesture() = false

    override fun verticalScrollView(): NestedScrollView? {
        return findViewById(R.id.scroll_view)
    }

    interface Listener {
        fun onSaveClicked(markAsFinalized: Boolean)
    }
}

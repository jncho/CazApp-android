package com.cazapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.widget.EditText
import com.cazapp.R

class InputTextDialog : DialogFragment(){

    lateinit var listener: DialogListener
    lateinit var input: EditText

    interface DialogListener {
        fun onDialogPositiveClick(dialog: InputTextDialog)
        fun onDialogNegativeClick(dialog: InputTextDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setTitle("New List")
        alertDialog.setView(activity?.layoutInflater?.inflate(R.layout.input_text_dialog, null))
        alertDialog.setPositiveButton("OK",null)
        alertDialog.setNegativeButton("CANCEL",null)

        listener = targetFragment as DialogListener

        return alertDialog.create()
    }

    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        input = alertDialog.findViewById(R.id.edit_text_input_text_dialog)
        val okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener{listener.onDialogPositiveClick(this)}
    }
}
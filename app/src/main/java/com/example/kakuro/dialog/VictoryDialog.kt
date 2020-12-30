package com.example.kakuro.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.kakuro.R
import java.util.concurrent.TimeUnit

class VictoryDialog : AppCompatDialogFragment() {

    var time : Long = 0
    private var listener : DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(resources.getString(R.string.victory))
            .setMessage(resources.getString(R.string.victoryMessage, TimeUnit.MILLISECONDS.toSeconds(time)))
        builder.setPositiveButton("OK") {dialog, which ->
            listener?.victoryAftermath()
        }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            listener = context as DialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement DialogListener")
        }
    }

    fun putTime(time: Long) {
        this.time = time
    }

    interface DialogListener {
        fun victoryAftermath()
    }
}
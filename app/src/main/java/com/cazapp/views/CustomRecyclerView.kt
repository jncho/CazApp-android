package com.cazapp.views

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import java.security.KeyStore

class CustomRecyclerView(context: Context) : androidx.recyclerview.widget.RecyclerView(context) {

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        return layoutManager?.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }
}
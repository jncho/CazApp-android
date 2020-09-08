package com.cazapp.adapters

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper


class TouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

    interface ItemTouchHelperAdapter{
        fun onItemMove(fromPosition: Int, toPosition: Int) : Boolean
    }

    interface ItemTouchHelperViewHolder {
        fun onItemSelected()
        fun onItemClear()
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    }

    override fun onSwiped(p0: ViewHolder, p1: Int) {
        return
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE){
            if(viewHolder is ItemTouchHelperViewHolder){
                (viewHolder as ItemTouchHelperViewHolder).onItemSelected()
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if(viewHolder is ItemTouchHelperViewHolder){
            (viewHolder as ItemTouchHelperViewHolder).onItemClear()
        }
    }

}
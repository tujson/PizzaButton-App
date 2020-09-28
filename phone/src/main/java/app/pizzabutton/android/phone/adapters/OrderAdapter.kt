package app.pizzabutton.android.phone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.pizzabutton.android.common.models.Order
import app.pizzabutton.android.phone.databinding.CardOrderBinding
import java.text.SimpleDateFormat
import java.util.*

private val dateSdf = SimpleDateFormat("MMM dd", Locale.US)
private val timeSdf = SimpleDateFormat("EEE, hh:mm a", Locale.US)

class OrderAdapter : ListAdapter<Order, RecyclerView.ViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OrderViewHolder(
            CardOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(getItem(position))
    }

    class OrderViewHolder(
        private val binding: CardOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            val timePlaced = Calendar.getInstance()
            timePlaced.timeInMillis = order.timePlaced
            binding.apply {
                tvDate.text = dateSdf.format(timePlaced.time)
                tvTime.text = timeSdf.format(timePlaced.time)

                tvPizza.text = order.pizza
                tvAddress.text = order.address

                tvStoreName.text = order.store.name
            }
        }
    }
}

private class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order) =
        oldItem.timePlaced == newItem.timePlaced

    override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
}
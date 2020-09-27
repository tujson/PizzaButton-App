package app.pizzabutton.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.pizzabutton.android.databinding.CardOrderBinding
import app.pizzabutton.android.models.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: MutableList<Order>) :
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {


    class ViewHolder(private val orderBinding: CardOrderBinding) :
        RecyclerView.ViewHolder(orderBinding.root) {
        private val dateSdf = SimpleDateFormat("MMM dd")
        private val timeSdf = SimpleDateFormat("EEE, hh:mm a")

        fun bindOrder(order: Order) {
            val timePlacedCal = Calendar.getInstance()
            timePlacedCal.timeInMillis = order.timePlaced
            orderBinding.tvDate.text = dateSdf.format(timePlacedCal.time)
            orderBinding.tvTime.text = timeSdf.format(timePlacedCal.time)

            orderBinding.tvPizza.text = order.pizza
            orderBinding.tvAddress.text = order.address

            // TODO: Add in horizontal scrollview to show more Store details
            orderBinding.tvStoreName.text = order.store.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val orderBinding =
            CardOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(orderBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindOrder(orders[position])
    }

    override fun getItemCount() = orders.size
}
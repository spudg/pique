package com.spudg.pique

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.pique.databinding.IoRowBinding
import java.text.DecimalFormat

class IOAdapter(private val context: Context, private val IOs: ArrayList<IOModel>) :
    RecyclerView.Adapter<IOAdapter.IOViewHolder>() {

    inner class IOViewHolder(val binding: IoRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IOViewHolder {
        val binding = IoRowBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return IOViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IOViewHolder, position: Int) {

        with(holder) {

            val formatRounded = DecimalFormat("#,###")
            val formatUSD = DecimalFormat("$#,###")

            binding.address.text = IOs[position].address
            binding.amount.text =
                formatRounded.format(IOs[position].amount.toFloat()) + " sats (" + formatUSD.format(
                    Constants.PRICE.toFloat() * (IOs[position].amount.toFloat() / 100000000)
                ) + ")"

        }

    }

    override fun getItemCount(): Int {
        return IOs.size
    }


}
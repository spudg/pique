package com.spudg.pique

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.spudg.pique.databinding.ActivityMainBinding
import com.spudg.pique.databinding.ActivityViewTransactionBinding
import com.spudg.pique.databinding.DialogViewTxBinding
import okhttp3.*
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ViewTransaction : AppCompatActivity() {

    private lateinit var bindingViewTransaction: ActivityViewTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingViewTransaction = ActivityViewTransactionBinding.inflate(layoutInflater)
        val view = bindingViewTransaction.root
        setContentView(view)

        getTransaction(Constants.SELECTED_TX)

        bindingViewTransaction.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun getDate(ms: String, dateFormat: String): String {
        val formatter = SimpleDateFormat(dateFormat);
        val calendar = Calendar.getInstance();
        calendar.timeInMillis = (ms + "000").toLong();
        return formatter.format(calendar.time);
    }

    private fun getTransaction(txid: String) {
        val url = "https://mempool.space/api/tx/$txid"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", "Failed to get transaction details.")
            }

            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                if (response.code().toString() == "200") {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        val txInfo: MainActivity.JsonInfo.TransactionSummary =
                            gson.fromJson(
                                response.body()?.string(),
                                MainActivity.JsonInfo.TransactionSummary::class.java
                            )

                        val inputs: ArrayList<IOModel> = ArrayList()
                        val outputs: ArrayList<IOModel> = ArrayList()

                        for (input in txInfo.vin) {
                            inputs.add(IOModel(input.prevout.scriptpubkey_address, input.prevout.value))
                        }

                        for (output in txInfo.vout) {
                            outputs.add(IOModel(output.scriptpubkey_address, output.value))
                        }

                        val tx = TransactionModel(
                            txInfo.txid,
                            txInfo.size,
                            txInfo.weight,
                            txInfo.fee,
                            txInfo.status.confirmed,
                            txInfo.status.block_height,
                            txInfo.status.block_time,
                            inputs,
                            outputs
                        )

                        val formatRounded = DecimalFormat("#,###")

                        bindingViewTransaction.tvTxId.text =
                            "This transaction's ID is " + tx.txid + "."
                        bindingViewTransaction.tvSize.text =
                            formatRounded.format(tx.size.toFloat()) + " B"
                        bindingViewTransaction.tvTxFee.text =
                            formatRounded.format(tx.fee.toFloat()) + " sats"
                        bindingViewTransaction.tvWeight.text =
                            tx.weight + " WU"
                        if (tx.confirmed == "true") {
                            bindingViewTransaction.tvConfirmed.text = "Confirmed in block " + tx.blockHeight + " on " + getDate(tx.blockTime, "dd MMMM yyyy, hh:mm") + " UTC."
                        } else {
                            bindingViewTransaction.tvConfirmed.text = "Not yet confirmed."
                        }

                        if (inputs.size > 0) {
                            bindingViewTransaction.rvInputs.visibility = View.VISIBLE
                            val manager = LinearLayoutManager(this@ViewTransaction)
                            bindingViewTransaction.rvInputs.layoutManager = manager
                            val ioAdapter = IOAdapter(this@ViewTransaction, inputs)
                            bindingViewTransaction.rvInputs.adapter = ioAdapter
                        } else {
                            bindingViewTransaction.rvInputs.visibility = View.GONE
                        }

                        if (outputs.size > 0) {
                            bindingViewTransaction.rvOutputs.visibility = View.VISIBLE
                            val manager = LinearLayoutManager(this@ViewTransaction)
                            bindingViewTransaction.rvOutputs.layoutManager = manager
                            val ioAdapter = IOAdapter(this@ViewTransaction, outputs)
                            bindingViewTransaction.rvOutputs.adapter = ioAdapter
                        } else {
                            bindingViewTransaction.rvOutputs.visibility = View.GONE
                        }

                    })
                } else {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Log.e("Pique", "API returned code " + response.code().toString())

                        Toast.makeText(this@ViewTransaction, "Transaction ID not found.", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        })
    }

}
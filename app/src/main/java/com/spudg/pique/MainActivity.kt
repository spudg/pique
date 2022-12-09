package com.spudg.pique

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.gson.Gson
import com.spudg.pique.databinding.ActivityMainBinding
import com.spudg.pique.databinding.DialogViewBlockBinding
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    class JsonInfo {

        data class BlockSummary(
            val timestamp: String,
            val height: String,
            val tx_count: String,
            val size: String,
            val id: String,
            val extras: BlockExtras,
            val previousblockhash: String
        )

        data class BlockExtras(
            val avgFeeRate: String,
            val feeRange: Array<String>,
            val avgFee: String,
            val reward: String,
            val totalFees: String,
        )

    }

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingDialogViewBlock: DialogViewBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

        getBlockList()

        bindingMain.tvSearchAddress.setOnClickListener {
            if (bindingMain.llSearchAddress.visibility == View.VISIBLE) {
                bindingMain.llSearchAddress.visibility = View.GONE
                bindingMain.tvSearchAddress.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchAddress.animate().scaleY(1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleY(1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleY(1F).duration = 75
            } else {
                bindingMain.llSearchAddress.visibility = View.VISIBLE
                bindingMain.tvSearchAddress.animate().scaleX(1.1F).duration = 75
                bindingMain.tvSearchAddress.animate().scaleY(1.1F).duration = 75
                bindingMain.llSearchBlock.visibility = View.GONE
                bindingMain.tvSearchBlock.animate().scaleX(.9F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleY(.9F).duration = 75
                bindingMain.llSearchTx.visibility = View.GONE
                bindingMain.tvSearchTx.animate().scaleX(.9F).duration = 75
                bindingMain.tvSearchTx.animate().scaleY(.9F).duration = 75
            }

            if (bindingMain.llSearchAddress.visibility == View.GONE && bindingMain.llSearchBlock.visibility == View.GONE && bindingMain.llSearchTx.visibility == View.GONE) {
                val imm = ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

        bindingMain.tvSearchBlock.setOnClickListener {
            if (bindingMain.llSearchBlock.visibility == View.VISIBLE) {
                bindingMain.llSearchBlock.visibility = View.GONE
                bindingMain.tvSearchAddress.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchAddress.animate().scaleY(1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleY(1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleY(1F).duration = 75
            } else {
                bindingMain.llSearchAddress.visibility = View.GONE
                bindingMain.tvSearchAddress.animate().scaleX(.9F).duration = 75
                bindingMain.tvSearchAddress.animate().scaleY(.9F).duration = 75
                bindingMain.llSearchBlock.visibility = View.VISIBLE
                bindingMain.tvSearchBlock.animate().scaleX(1.1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleY(1.1F).duration = 75
                bindingMain.llSearchTx.visibility = View.GONE
                bindingMain.tvSearchTx.animate().scaleX(.9F).duration = 75
                bindingMain.tvSearchTx.animate().scaleY(.9F).duration = 75

            }

            if (bindingMain.llSearchAddress.visibility == View.GONE && bindingMain.llSearchBlock.visibility == View.GONE && bindingMain.llSearchTx.visibility == View.GONE) {
                val imm = ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

        bindingMain.tvSearchTx.setOnClickListener {
            if (bindingMain.llSearchTx.visibility == View.VISIBLE) {
                bindingMain.llSearchTx.visibility = View.GONE
                bindingMain.tvSearchAddress.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchAddress.animate().scaleY(1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleY(1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleX(1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleY(1F).duration = 75
            } else {
                bindingMain.llSearchAddress.visibility = View.GONE
                bindingMain.tvSearchAddress.animate().scaleX(.9F).duration = 75
                bindingMain.tvSearchAddress.animate().scaleY(.9F).duration = 75
                bindingMain.llSearchBlock.visibility = View.GONE
                bindingMain.tvSearchBlock.animate().scaleX(.9F).duration = 75
                bindingMain.tvSearchBlock.animate().scaleY(.9F).duration = 75
                bindingMain.llSearchTx.visibility = View.VISIBLE
                bindingMain.tvSearchTx.animate().scaleX(1.1F).duration = 75
                bindingMain.tvSearchTx.animate().scaleY(1.1F).duration = 75
            }

            if (bindingMain.llSearchAddress.visibility == View.GONE && bindingMain.llSearchBlock.visibility == View.GONE && bindingMain.llSearchTx.visibility == View.GONE) {
                val imm = ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

    }

    private fun getBlockList() {
        val url = "https://mempool.space/api/v1/blocks/"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", "Failed to get address details.")
            }

            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                if (response.code().toString() == "200") {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        val blockInfo: Array<JsonInfo.BlockSummary> =
                            gson.fromJson(
                                response.body()?.string(),
                                Array<JsonInfo.BlockSummary>::class.java
                            )
                        val blocks: ArrayList<BlockModel> = ArrayList()
                        for (block in blockInfo) {
                            blocks.add(
                                BlockModel(
                                    block.timestamp,
                                    block.height,
                                    block.tx_count,
                                    block.size,
                                    block.id,
                                    block.extras.avgFeeRate,
                                            block.extras.reward,
                                    (block.extras.reward.toFloat() - block.extras.totalFees.toFloat()).toString(),
                                    block.extras.totalFees,
                                    block.extras.avgFee,
                                    block.extras.feeRange[block.extras.feeRange.size - 1],
                                    block.extras.feeRange[0],
                                    block.previousblockhash
                                )
                            )
                        }
                        if (blocks.size > 0) {
                            bindingMain.rvBlocks.visibility = View.VISIBLE
                            bindingMain.tvNoBlocks.visibility = View.GONE
                            val manager = LinearLayoutManager(this@MainActivity)
                            bindingMain.rvBlocks.layoutManager = manager
                            val blockAdapter = BlockAdapter(this@MainActivity, blocks)
                            bindingMain.rvBlocks.adapter = blockAdapter
                        } else {
                            bindingMain.rvBlocks.visibility = View.GONE
                            bindingMain.tvNoBlocks.visibility = View.VISIBLE
                        }
                    })
                } else {
                    Log.e("Pique", "API returned code " + response.code().toString())


                }
            }
        })

    }

    fun showBlock(hash: String) {
        val url = "https://mempool.space/api/v1/block/$hash"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", "Failed to get address details.")
            }

            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                if (response.code().toString() == "200") {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        val blockInfo: MainActivity.JsonInfo.BlockSummary =
                            gson.fromJson(
                                response.body()?.string(),
                                MainActivity.JsonInfo.BlockSummary::class.java
                            )
                                val block = BlockModel(
                                    blockInfo.timestamp,
                                    blockInfo.height,
                                    blockInfo.tx_count,
                                    blockInfo.size,
                                    blockInfo.id,
                                    blockInfo.extras.avgFeeRate,
                                    blockInfo.extras.reward,
                                    (blockInfo.extras.reward.toFloat() - blockInfo.extras.totalFees.toFloat()).toString(),
                                    blockInfo.extras.totalFees,
                                    blockInfo.extras.avgFee,
                                    blockInfo.extras.feeRange[blockInfo.extras.feeRange.size - 1],
                                    blockInfo.extras.feeRange[0],
                                    blockInfo.previousblockhash
                                )

                        val blockDialog = Dialog(this@MainActivity, R.style.Theme_Dialog)
                        blockDialog.setCancelable(false)
                        bindingDialogViewBlock = DialogViewBlockBinding.inflate(layoutInflater)
                        val view = bindingDialogViewBlock.root
                        blockDialog.setContentView(view)
                        blockDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        bindingDialogViewBlock.tvTimestamp.text = block.timestamp
                        bindingDialogViewBlock.tvHeight.text = block.height
                        bindingDialogViewBlock.tvAveFee.text = block.aveRate
                        bindingDialogViewBlock.tvId.text = block.id
                        bindingDialogViewBlock.tvTxCount.text = block.txCount
                        bindingDialogViewBlock.tvSize.text = block.size
                        bindingDialogViewBlock.tvReward.text = block.reward
                        bindingDialogViewBlock.tvSubsidy.text = block.subsidy
                        bindingDialogViewBlock.tvFees.text = block.fees
                        bindingDialogViewBlock.tvAveFee.text = block.aveFee
                        bindingDialogViewBlock.tvHighFee.text = block.highFee
                        bindingDialogViewBlock.tvLowFee.text = block.lowFee
                        bindingDialogViewBlock.tvPrevHash.text = block.prevHash

                        blockDialog.show()

                    })
                } else {
                    Log.e("Pique", "API returned code " + response.code().toString())


                }
            }
        })

    }


}
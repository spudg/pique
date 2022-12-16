package com.spudg.pique

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.spudg.pique.databinding.ActivityMainBinding
import com.spudg.pique.databinding.DialogRefreshInfoBinding
import com.spudg.pique.databinding.DialogViewBlockBinding
import okhttp3.*
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt


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

        data class TransactionSummary(
            val txid: String,
            val size: String,
            val weight: String,
            val fee: String,
            val vin: Array<InputInfo>,
            val vout: Array<OutputInfo>,
            val status: TransactionExtras
        )

        data class TransactionExtras(
            val confirmed: String,
            val block_height: String,
            val block_time: String
        )

        data class InputInfo(
            val prevout: InputExtras
        )

        data class InputExtras(
            val scriptpubkey_address: String,
            val value: String
        )

        data class OutputInfo(
            val scriptpubkey_address: String,
            val value: String
        )

        data class AddressSummary(
            val address: String,
            val chain_stats: ChainInfo,
            val mempool_stats: MempoolInfo
        )

        data class ChainInfo(
            val funded_txo_count: String,
            val funded_txo_sum: String,
            val spent_txo_count: String,
            val spent_txo_sum: String,
            val tx_count: String
        )

        data class MempoolInfo(
            val funded_txo_count: String,
            val funded_txo_sum: String,
            val spent_txo_count: String,
            val spent_txo_sum: String,
            val tx_count: String
        )

        data class Price(
            val bitcoin: Currency
        )

        data class Currency(
            val usd: String
        )

    }

    private fun getTimeAgo(date: String): String {
        val now = Calendar.getInstance().timeInMillis
        val then = date + "000"
        val diff = now.toFloat() - then.toFloat()
        val formatted = (diff / 1000 / 60).roundToInt()
        return if (formatted == 0) {
            "just now"
        } else if (formatted == 1) {
            "1 minute ago"
        } else if (formatted in 2..59) {
            "$formatted minutes ago"
        } else if (formatted in 60..89) {
            "1 hour ago"
        } else if ((formatted.toDouble() / 60) >= 1.5) {
            (formatted.toDouble() / 60).roundToInt().toString() + " hours ago"
        } else {
            "$formatted minutes ago"
        }
    }

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingDialogViewBlock: DialogViewBlockBinding
    private lateinit var bindingDialogRefreshInfo: DialogRefreshInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

        setPrice()
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
                val imm =
                    ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
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
                val imm =
                    ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
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
                val imm =
                    ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

        bindingMain.btnSearchAddress.setOnClickListener {
            val input = bindingMain.etSearchAddress.text.toString()
            if (input.isNotEmpty()) {
                Constants.SELECTED_ADDRESS = input
                val intent = Intent(this, ViewAddress::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter an address.", Toast.LENGTH_SHORT).show()
            }
        }

        bindingMain.btnSearchBlock.setOnClickListener {
            val input = bindingMain.etSearchBlock.text.toString()
            if (input.isNotEmpty()) {
                if (input.length == 64) {
                    showBlock(input)
                } else {
                    showBlockUsingHeight(input)
                }
            } else {
                Toast.makeText(this, "Enter a block height or hash.", Toast.LENGTH_SHORT).show()
            }
        }

        bindingMain.btnSearchTx.setOnClickListener {
            val input = bindingMain.etSearchTx.text.toString()
            if (input.isNotEmpty()) {
                if (input.length == 64) {
                    Constants.SELECTED_TX = input
                    val intent = Intent(this, ViewTransaction::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid transaction ID.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter a transaction ID (64 chars).", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        bindingMain.btnInfo.setOnClickListener {
            showInfoDialog()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            getBlockList()
        }, 30000)

    }

    private fun showBlockUsingHeight(height: String) {
        val url = "https://mempool.space/api/block-height/$height"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", "Failed to get block details.")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code().toString() == "200") {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        showBlock(response.body()!!.string())
                    })
                } else {
                    Log.e("Pique", "API returned code " + response.code().toString())
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Toast.makeText(
                            this@MainActivity,
                            "Block height not found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            }
        })
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

                        var prevHash = blockInfo.previousblockhash
                        if (blockInfo.height == "0") {
                            prevHash = "N/A - Genesis"
                        }

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
                            prevHash
                        )

                        val blockDialog = Dialog(this@MainActivity, R.style.Theme_Dialog)
                        blockDialog.setCancelable(false)
                        bindingDialogViewBlock = DialogViewBlockBinding.inflate(layoutInflater)
                        val view = bindingDialogViewBlock.root
                        blockDialog.setContentView(view)
                        blockDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        val formatRounded = DecimalFormat("#,###")
                        val formatUSD = DecimalFormat("$#,###")

                        if (blockInfo.height == "0") {
                            bindingDialogViewBlock.tvBlockTitle.text =
                                "Block #" + formatRounded.format(block.height.toFloat()) + " (Genesis)"
                        } else {
                            bindingDialogViewBlock.tvBlockTitle.text =
                                "Block #" + formatRounded.format(block.height.toFloat())
                        }

                        bindingDialogViewBlock.tvGeneralInfo.text =
                            "Mined " + getTimeAgo(block.timestamp) + ". This block contains " + formatRounded.format(
                                block.txCount.toFloat()
                            ) + " transactions, an average fee rate of ~" + block.aveRate + " sat/vB and has a size of " + (BigDecimal(
                                block.size.toDouble() / 1000000
                            ).setScale(
                                2,
                                RoundingMode.HALF_UP
                            )).toString() + " MB."
                        bindingDialogViewBlock.tvReward.text =
                            formatRounded.format(block.reward.toFloat()) + " sats (" + formatUSD.format(Constants.PRICE.toFloat()*(block.reward.toFloat()/100000000)) + ")"
                        bindingDialogViewBlock.tvSubsidy.text =
                            formatRounded.format(block.subsidy.toFloat()) + " sats (" + formatUSD.format(Constants.PRICE.toFloat()*(block.subsidy.toFloat()/100000000)) + ")"
                        bindingDialogViewBlock.tvFees.text =
                            formatRounded.format(block.fees.toFloat()) + " sats (" + formatUSD.format(Constants.PRICE.toFloat()*(block.fees.toFloat()/100000000)) + ")"
                        bindingDialogViewBlock.tvAveFee.text =
                            formatRounded.format(block.aveFee.toFloat()) + " sats (" + formatUSD.format(Constants.PRICE.toFloat()*(block.aveFee.toFloat()/100000000)) + ")"
                        bindingDialogViewBlock.tvFeeRange.text =
                            formatRounded.format(block.lowFee.toFloat()) + " - " + formatRounded.format(
                                block.highFee.toFloat()
                            ) + " sat/vB"
                        bindingDialogViewBlock.tvId.text = block.id
                        bindingDialogViewBlock.tvPrevHash.text = block.prevHash

                        bindingDialogViewBlock.btnClose.setOnClickListener {
                            blockDialog.dismiss()
                        }

                        blockDialog.show()

                    })
                } else {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        Log.e("Pique", "API returned code " + response.code().toString())

                        Toast.makeText(
                            this@MainActivity,
                            "Blockhash not found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            }
        })
    }

    private fun showInfoDialog() {
        val infoDialog = Dialog(this@MainActivity, R.style.Theme_Dialog)
        infoDialog.setCancelable(false)
        bindingDialogRefreshInfo = DialogRefreshInfoBinding.inflate(layoutInflater)
        val view = bindingDialogRefreshInfo.root
        infoDialog.setContentView(view)
        infoDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDialogRefreshInfo.infoText.movementMethod = LinkMovementMethod.getInstance()

        bindingDialogRefreshInfo.btnClose.setOnClickListener {
            infoDialog.dismiss()
        }

        infoDialog.show()
    }

    private fun setPrice() {
        val urlPrice =
            "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd"
        val requestPrice = Request.Builder().url(urlPrice).build()
        val client = OkHttpClient()
        client.newCall(requestPrice).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", "Failed to get price details.")
            }

            override fun onResponse(call: Call, response: Response) {
                Handler(Looper.getMainLooper()).post(Runnable {
                    val gson = Gson()
                    val priceInfo = gson.fromJson(
                        response.body()?.string(),
                        MainActivity.JsonInfo.Price::class.java
                    )
                    Constants.PRICE = priceInfo.bitcoin.usd
                })
            }
        })
    }


}
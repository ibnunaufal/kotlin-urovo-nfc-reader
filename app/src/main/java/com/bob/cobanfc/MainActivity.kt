package com.bob.cobanfc

import android.annotation.SuppressLint
import android.device.PiccManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.experimental.and


class MainActivity : AppCompatActivity() {
    private val TAG = "PiccCheck"

    private val MSG_FOUND_UID = 12

    private var piccReader: PiccManager? = null

    private var handler: Handler? = null

    private var exec: ExecutorService? = null

    var scan_card = -1

    var SNLen = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOpen: Button = findViewById(R.id.picc_open)
        val btnCheck: Button = findViewById(R.id.picc_check)
        val tvApdu2: EditText = findViewById(R.id.rev_data)

        exec = Executors.newSingleThreadExecutor()
        piccReader = PiccManager()

        handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                // TODO Auto-generated method stub
                when (msg.what) {
                    MSG_FOUND_UID -> {
                        val uid = msg.obj as String
                        Log.d("hasil", "hex: $uid")
                        tvApdu2.append("\n$uid")

                    }
                    else -> {}
                }
                super.handleMessage(msg)
            }
        }


        btnOpen.setOnClickListener {
            opening()
        }

        btnCheck.setOnClickListener {
            checking()
        }
    }

    @Override
    fun opening(){
        exec!!.execute(Thread({ // TODO Auto-generated method stub
            piccReader!!.open()
        }, "picc open"))
    }

    @Override
    fun checking(){
        exec!!.execute(Thread({ // TODO Auto-generated method stub
            val CardType = ByteArray(2)
            val Atq = ByteArray(14)
            val SAK = 1.toChar()
            val sak = ByteArray(1)
            sak[0] = SAK.code.toByte()
            val SN = ByteArray(10)
            scan_card = piccReader!!.request(CardType, Atq)
            if (scan_card > 0) {
                SNLen = piccReader!!.antisel(SN, sak)
                Log.d(TAG, "SNLen = $SNLen")
                val msg = handler!!.obtainMessage(MSG_FOUND_UID)
//                msg.obj = bytesToHexString(SN, SNLen)
                msg.obj = bytesToHexString2(SN)
                Log.d("kebaca", bytesToHexString(SN, SNLen).toString())
                Log.d("balik", balik(SN.toString()))
                Log.d("bytesToHexString2", bytesToHexString2(SN).toString())
                Log.d("bytesToHex", bytesToHex(SN))
                (handler as Handler).sendMessage(msg)
            }
        }, "picc check"))
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }


    fun bytesToHexString(src: ByteArray, len: Int): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.isEmpty()) {
            return null
        }
        if (len <= 0) {
            return ""
        }
        for (i in 0 until len) {
            val v = (src[i] and 0xFF.toByte()).toInt()
            val hv = Integer.toHexString(v.toInt())
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    fun bytesToHexString2(src: ByteArray): String? {
        if (ByteUtils.isNullOrEmpty(src)) {
            return null
        }
        val sb = StringBuilder()
        for (b in src) {
            sb.append(String.format("%02X", b))
        }
        val temp = sb.toString().substring(0, 8)
        val returnedValue = "Hex: ${balik(temp)} | Dec: ${balik(temp).toLong(16)}"
        Log.d("toLong", sb.toString().substring(0, 8).toLong(16).toString())
        Log.d("balikk", balik(temp))
        return returnedValue

    }

    fun balik(str: String) : String{
        var buffer = ""

        var nfcid = ""

        for (index in str.length - 1 downTo 1 step 2) {
            buffer += str[index - 1]
            buffer += str[index] + ""
        }
        buffer = buffer.trim()
        buffer = buffer.takeLast(8)
        nfcid = buffer
        Log.d("after", nfcid)

        return  nfcid
    }

}
package com.azzam.smartalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azzam.smartalarm.adapter.AlarmAdapter
import com.azzam.smartalarm.databinding.ActivityMainBinding
import com.azzam.smartalarm.local.AlarmDB
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding as ActivityMainBinding

    private var alarmAdapter: AlarmAdapter? = null

    private var alarmService: AlarmReceiver? = null

    private val db by lazy { AlarmDB(this) }

    override fun onResume() {
        super.onResume()

        db.alarmDao().getAlarm().observe(this){
            alarmAdapter?.setData(it)
            Log.i("getAlarm", "setupRecyclerView: with this data $it")
        }

        /*CoroutineScope(Dispatchers.IO).launch {
            val alarm = db.alarmDao().getAlarm()
            withContext(Dispatchers.Main) {
                alarmAdapter?.setData(alarm)
            }
            Log.i("getAlarm", "setupRecyclerView: with this data $alarm")
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmService = AlarmReceiver()

        initView()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.apply {
            alarmAdapter = AlarmAdapter()
            rvReminderAlarm.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = alarmAdapter
            }
            swipeToDelete(rvReminderAlarm)
        }
    }

    private fun initView() {
        binding.apply {
            cvSetOneTimeAlarm.setOnClickListener {
                startActivity(Intent(this@MainActivity, OneTimeAlarmActivity::class.java))
            }
            cvSetRepeatingAlarm.setOnClickListener {
                startActivity(Intent(this@MainActivity, RepeatingAlarmActivity::class.java))
            }
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteItem = alarmAdapter?.listAlarm?.get(viewHolder.adapterPosition)

                CoroutineScope(Dispatchers.IO).launch {
                    deleteItem?.let { db.alarmDao().deleteAlarm(it) }
                    Log.i("DeleteAlarm","onSwipe: success deleted alarm with $deleteItem")
                    }

                deleteItem?.type?.let { alarmService?.cancelAlarm(applicationContext, it) }

                }
        }).attachToRecyclerView(recyclerView)
    }
}
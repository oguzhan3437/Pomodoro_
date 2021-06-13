package com.oguzhancetin.pomodorotimer.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.oguzhancetin.pomodorotimer.util.leftTime
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.oguzhancetin.pomodorotimer.R
import com.oguzhancetin.pomodorotimer.background.MyService
import com.oguzhancetin.pomodorotimer.database.Pomodoro
import com.oguzhancetin.pomodorotimer.databinding.FragmentMainBinding
import com.oguzhancetin.pomodorotimer.util.Times
import com.oguzhancetin.pomodorotimer.util.TimesSharedPreferences

import com.oguzhancetin.pomodorotimer.viewmodel.FragmentMainViewmodel
import java.util.*


class FragmentMain : Fragment() {
    private var serviceIntent: Intent? = null
    private lateinit var broadcastReceiver: BroadcastReceiver;

    //Display time on the main fragment 00:00
    private lateinit var timeText: TextView

    private lateinit var viewModel: FragmentMainViewmodel

    private lateinit var mCalendar: Calendar

    //check to set graph state
    var graphState = false
    private lateinit var progressCircle: ProgressBar
    private var progress = 100
    private var leftGraph = 1L


    //1sec
    val oneMin = 60000L
    val oneSec = 1000L


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)



        progressCircle = binding.progress
        progressCircle.progress = progress
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(FragmentMainViewmodel::class.java)
        //viewModel.deleteAlldata()
        mCalendar = Calendar.getInstance()

        timeText = binding.textViewTimeLeft

        serviceIntent = Intent(requireActivity(), MyService::class.java)


        //start long break
        binding.buttonLongbreak.setOnClickListener {
            startTimeService(Times.LONG_BREAK)

            graphState = true
        }
        //start short break
        binding.buttonShortBreak.setOnClickListener {

            startTimeService(Times.SHORT_BREAK)

            graphState = true
        }
        //start pomodoro
        binding.buttonStart.setOnClickListener {
            startTimeService(Times.START_TIME)

            graphState = true


        }

        //to write default time before start (25:00)
        val longTime = TimesSharedPreferences
            .getSharred(requireActivity())?.getLong(Times.START_TIME.name, Times.START_TIME.time)
        val longTimeString = printLeftMinutes(longTime)
        timeText.text = longTimeString

        /* viewModel.allPomodoro.observe(viewLifecycleOwner, Observer {
             it.forEach {
                 mCalendar.timeInMillis = (it.finished_date_milis)
                 Log.e("pomodoro",mCalendar.get(Calendar.DAY_OF_MONTH).toString()+"/"+mCalendar.get(Calendar.MONDAY).toString())
             }
         })*/

        return binding.root
    }

    private fun printLeftMinutes(longTime: Long?): String {
        var minute = (longTime!! / oneMin).toString()
        var second = ((longTime % oneMin) / oneSec).toString()
        if (minute.length == 1) minute = "0${minute}"
        if (second.length == 1) second = "0${second}"

        return "${minute} : ${second}";
    }


    @SuppressLint("LongLogTag")
    fun startTimeService(time: Times) {
        clearService()

        serviceIntent.also {
            it?.putExtra("timeType", time)
            requireActivity().startService(it)
        }

        if (leftTime.hasObservers()) {
            leftTime.removeObservers(viewLifecycleOwner)
        }

        leftTime.observe(viewLifecycleOwner, Observer {

            timeText.text = printLeftMinutes(it)
            progress = (((it.toDouble() / leftGraph.toDouble()) * 100)).toInt()
            progressCircle.progress = progress

            if (it == 0L) {
                timeText.text = "00 : 00"
                if (time == Times.START_TIME) {
                    val date = System.currentTimeMillis()
                    viewModel.insertPomodoro(Pomodoro(finished_date_milis = date))
                    leftTime.value = 2L
                }
                leftTime.value = 2L
            }
            if (graphState) {
                leftGraph = it
                graphState = false
            }


        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_alldata -> {
                viewModel.deleteAlldata()
                true
            }
            else -> {
                false
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        clearService()
    }

    private fun clearService() {
        serviceIntent?.let {
            requireActivity().stopService(it)
        }
    }
}
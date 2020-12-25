package com.oguzhancetin.pomodorotimer.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.sql.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class PomodoRepository(context: Context) {

    lateinit var  allPomodoro:LiveData<List<Pomodoro>>


    private val pomodoroDao = PomodoroDatabase.getDatabase(context).pomodoroDao()

    init {

        Log.e("todayMilis",System.currentTimeMillis().toString())
        val numberOfDayOfWeek =  LocalDateTime.ofInstant(java.util.Date(System.currentTimeMillis()).toInstant(),
            ZoneId.systemDefault()).dayOfWeek.value
        val todayMilis = System.currentTimeMillis()
        Log.e("today",todayMilis.toString())
        val aDayMilis = (86400000L)
        val totalMilis = (todayMilis)-((numberOfDayOfWeek-1)*aDayMilis)
        Log.e("total",totalMilis.toString())

        allPomodoro = pomodoroDao.getAllPomodoro(totalMilis)

    }

    suspend fun insertPomodoro(pomodoro: Pomodoro){
        pomodoroDao.insert(pomodoro)
    }

    suspend fun deleteAllPomodoro(){
        pomodoroDao.deleteAllPomodoro()
    }

}
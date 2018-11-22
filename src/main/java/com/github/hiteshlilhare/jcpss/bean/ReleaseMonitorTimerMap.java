/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.bean;

import com.github.hiteshlilhare.jcpss.util.ReleaseMonitorTimerTask;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

/**
 *
 * @author Hitesh
 */
public class ReleaseMonitorTimerMap {

    /**
     * map of release monitor timer task.
     */
    private final HashMap<String, Timer> timerMap;

    private static ReleaseMonitorTimerMap _instance;
    
    public static ReleaseMonitorTimerMap getInstance(){
        if(_instance ==  null){
            _instance = new ReleaseMonitorTimerMap();
        }
        return _instance;
    }
    /**
     * Constructor.
     */
    private ReleaseMonitorTimerMap() {
        timerMap = new HashMap<>();
    }

    /**
     * Adds monitor task to the map.
     *
     * @param task
     */
    public void addTimer(ReleaseMonitorTimerTask task) {
        //Get Date object.
        Calendar calender = Calendar.getInstance();
        calender.setTime(new Date());
        //Hour/Minute/Second/Millisecond can be randomized to avoid fixed time
        //of monitroing repository.
        //Generates random number between 0-23
        int random = (int )(Math.random() * 23);
        calender.set(Calendar.HOUR_OF_DAY, random);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);
        Date date = calender.getTime();
        //Schedule the task for the date and repeat after 24 hrs.
        Timer timer = new Timer();
        timer.schedule(task, date, 86400000);
        timerMap.put(task.getRepoID(), timer);
    }

    /**
     * Removes monitor task from map.
     *
     * @param repoId
     */
    public void removeTask(String repoId) {
        Timer timer = timerMap.remove(repoId);
        if (timer != null) {
            timer.cancel();
        }
    }
}

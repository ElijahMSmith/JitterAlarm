package me.eli.jitteralarm.utilities;


import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AlarmInfo implements Comparable<AlarmInfo> {

    private final String DATE_PATTERN = "E yyyy.MM.dd', 'hh:mm:ss a zzz";
    private final SimpleDateFormat formatForPattern = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    //Stores the important information about an alarm
    private String alarmName; //Name of the alarm
    private String alarmTime; //Time the alarm triggers - HH:MM AM/PM
    private String offsetTime; //In what offset the alarm is allowed to trigger - HH:MM:SS
    private String nextTriggerDate;
    private boolean[] triggerDays; //What days the alarm will trigger. Index 0 = Sunday, 6 = Saturday

    private final long HOUR_MILLIS = 1000 * 60 * 60;
    private final long MINUTE_MILLIS = 1000 * 60;
    private final long SECOND_MILLIS = 1000;

    //Set up our AlarmInfo object with initial data and trigger days in boolean array form
    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, boolean[] triggerOnDay){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
        this.triggerDays = triggerOnDay;
        setNextTriggerDate(generateTriggerDate(false)); //Generates a random, valid date for our first alarm to be set
        //Can be manually reset later on when resetting alarms
    }

    //Alternative constructor that can be used to pass in a string representation of trigger days
    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, String triggerDays){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
        setTriggerArray(triggerDays);
        setNextTriggerDate(generateTriggerDate(false)); //Generates a random, valid date for our first alarm to be set
        //Can be manually reset later on when resetting alarms
    }

    //Alternative constructor that creates alarm from database row (all fields stored as strings)
    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, String nextTriggerDate, String triggerDays){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
        this.nextTriggerDate = nextTriggerDate;
        setTriggerArray(triggerDays);
    }

    //Retrieve alarm data

    public String getAlarmName() {
        return alarmName;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public String getOffsetTime() {
        return offsetTime;
    }

    public String getNextTriggerDate(){
        return nextTriggerDate;
    }

    //Returns true if there any days in our triggerDays array set to true
    //Returns false if the alarm is set to never run
    public boolean areAnyTriggerDays(){
        boolean result = false;
        for(int i = 0; i < 7; i++){
            result = result || triggerDays[i];
        }
        return result;
    }

    //Retrieve trigger days in boolean array form
    public boolean[] getTriggerArray() {
        return triggerDays;
    }

    //Retrieve trigger days in String form
    public String getTriggerString() {
        //Builds String representation from boolean array
        StringBuilder triggers = new StringBuilder();

        //For each boolean, append T if its true, F if false
        for(Boolean b :triggerDays){
            triggers.append(b ? "T" : "F");
        }

        //Return our new string
        return triggers.toString();
    }

    //For future use if necessary
    protected void setTriggerArray(boolean[] triggerDays) {
        this.triggerDays = triggerDays;
    }

    //Set trigger array from its String form
    protected void setTriggerArray(String triggerString){
        triggerDays = new boolean[7];
        for(int i = 0; i < 7; i++){
            triggerDays[i] = triggerString.charAt(i) == 'T';
        }
    }

    //Allows comparisons to order alarms in our adapter. Comparison is alphabetically by alarm name (each alarm must be unique in name)
    @Override
    public int compareTo(AlarmInfo other){
        return alarmName.compareTo(other.getAlarmName());
    }

    //Creates a basic string representation of this alarm. Used for debugging purposes.
    @NonNull
    @Override
    public String toString(){
        return alarmName + "; " + alarmTime + "; " + offsetTime + "; " + getTriggerString() + "; " + nextTriggerDate;
    }

    //Checks if this alarm and another are exactly the same so that we can check for duplicates elsewhere.
    public boolean isIdenticalTo(Object o){
        if(o == null) return false;
        if(!(o instanceof AlarmInfo)) return false;
        AlarmInfo other = (AlarmInfo) o;
        //Only care to check for alarm details, we don't care if the next trigger date is different
        return other.getAlarmName().equals(alarmName) && other.getAlarmTime().equals(alarmTime)
                && other.getOffsetTime().equals(offsetTime) && other.getTriggerString().equals(this.getTriggerString());
    }

    //Reserved for future use if necessary
    //Would compare to see if two alarms had the same name (not exactly identical, implying one is an edited version of another)
    public boolean isSameAlarmNameAs(AlarmInfo alarm){
        if(alarm == null) return false;
        return alarm.getAlarmName().equals(this.alarmName);
    }

    //Uses the time, offset, and trigger dates to generate a new trigger time within the parameters
    //Return this trigger time in Gregorian Calendar form
    public GregorianCalendar generateTriggerDate(boolean alreadyTriggeredToday){

        //If we don't have any days set to trigger this alarm, the alarm is dormant and we make everything null to signify this
        if(!areAnyTriggerDays())
            return null;

        GregorianCalendar cal = new GregorianCalendar();
        String[] timeParts = alarmTime.split("[: ]");
        String[] offsetParts = offsetTime.split(":");

        //Check if today is a trigger day for this alarm (and it hasn't already triggered once today)
        if(!alreadyTriggeredToday && triggerDays[cal.get(GregorianCalendar.DAY_OF_WEEK) - 1]){
            //If this alarm has any time left to trigger today, we'll generate a random moment in that remaining time.
            //If the whole interval has already passed today, returns null and we move on to the rest of the method (starts looking for future days)
            GregorianCalendar nextTrigger = generateTriggerForToday();
            if(nextTrigger != null) return nextTrigger;
        }

        //Set base time for trigger and start looking to days tomorrow (since it doesn't have to trigger today)
        cal.add(GregorianCalendar.DAY_OF_YEAR, 1);
        cal.set(GregorianCalendar.HOUR, Integer.parseInt(timeParts[0]));
        cal.set(GregorianCalendar.MINUTE, Integer.parseInt(timeParts[1]));
        cal.set(GregorianCalendar.SECOND, 0);
        cal.set(GregorianCalendar.AM_PM, timeParts[2].equalsIgnoreCase("AM") ? GregorianCalendar.AM : GregorianCalendar.PM);

        //GregorianCalendar.DAY_OF_WEEK uses constants SUNDAY = 1, ..., SATURDAY = 7
        //cal.get(GregorianCalendar.DAY_OF_WEEK) - 1 is how we get proper index for triggerArray
        //While the current day of the week isn't right to trigger this alarm, scoot it to the next day
        //When we do hit a valid day of the week, triggerDays is true so we stop
        while(!triggerDays[cal.get(GregorianCalendar.DAY_OF_WEEK) - 1]){
            cal.add(GregorianCalendar.DAY_OF_YEAR, 1);
        }

        //Setup for adding an offset: get maximum amount of time we can add to the alarm and still be in our boundaries
        int maxOffsetHours = Integer.parseInt(offsetParts[0]);
        int maxOffsetMinutes = Integer.parseInt(offsetParts[1]);
        int maxOffsetSeconds = Integer.parseInt(offsetParts[2]);

        //Generates the entire length of our interval
        long intervalInMillis = maxOffsetHours * HOUR_MILLIS + maxOffsetMinutes * MINUTE_MILLIS + maxOffsetSeconds * SECOND_MILLIS;
        intervalInMillis/=2; //Take half our total interval

        //Select random amount of time in our half interval, make it randomly positive or negative
        long randomPoint = (Math.random() >= .5 ? -1 : 1) * (long)(Math.random() * intervalInMillis);

        //Add this random point in our interval to the base time stored in cal for our alarm trigger time
        cal.setTimeInMillis(cal.getTimeInMillis() + randomPoint);
        return cal; //Return our new trigger time
    }

    //Called if today is a trigger day, generate a time that is in the interval and hasn't already passed, return that for use
    private GregorianCalendar generateTriggerForToday(){
        GregorianCalendar cal = new GregorianCalendar();
        String[] timeParts = alarmTime.split("[: ]");
        String[] offsetParts = offsetTime.split(":");

        //Set base time for trigger
        cal.set(GregorianCalendar.HOUR, Integer.parseInt(timeParts[0]));
        cal.set(GregorianCalendar.MINUTE, Integer.parseInt(timeParts[1]));
        cal.set(GregorianCalendar.SECOND, 0);
        cal.set(GregorianCalendar.AM_PM, timeParts[2].equalsIgnoreCase("AM") ? GregorianCalendar.AM : GregorianCalendar.PM);

        //Setup for adding an offset: get maximum amount of time we can add to the alarm and still be in our boundaries
        int maxOffsetHours = Integer.parseInt(offsetParts[0]);
        int maxOffsetMinutes = Integer.parseInt(offsetParts[1]);
        int maxOffsetSeconds = Integer.parseInt(offsetParts[2]);

        //Add maximum offset to cal
        cal.add(Calendar.HOUR, maxOffsetHours);
        cal.add(Calendar.MINUTE, maxOffsetMinutes);
        cal.add(Calendar.SECOND, maxOffsetSeconds);

        //If we've missed the entire interval for today, return null indicating we need to look for future times starting tomorrow
        if(System.currentTimeMillis() >= cal.getTimeInMillis())
            return null;

        //Otherwise, generate a random time in what remaining time of the interval we have left

        //First store our upper bound on the interval
        long intervalTop = cal.getTimeInMillis();

        //Set cal to be the bottom of the interval (base time - max offset)
        cal.add(Calendar.HOUR, -2 * maxOffsetHours);
        cal.add(Calendar.MINUTE, -2 * maxOffsetMinutes);
        cal.add(Calendar.SECOND, -2 * maxOffsetSeconds);

        //Gets the higher of the two times, indicating the minimum time we can set our alarm for
            //If the current time is earlier than the intervalBottom, then we can use the whole interval
            //If the current time is later than the intervalBottom, then we have to shorten our interval to just between the current time and the top of the interval (cal)
        long lowerBound = Math.max(System.currentTimeMillis(), cal.getTimeInMillis());

        //Calculate the amount of milliseconds we have in our new interval, whatever that might be
        long newInterval = intervalTop - lowerBound;

        //Generate a random time in the new interval and return this time in calendar form
        cal.setTimeInMillis(lowerBound + (long)(Math.random() * (newInterval + 1)));
        return cal;
    }

    //Set next trigger date by passing in randomly generated calendar time
    public void setNextTriggerDate(GregorianCalendar nextTriggerDate){
        if(nextTriggerDate == null)
            //If calendar is null, this alarm isn't set to run, so we can set our string to null as a signifier
            this.nextTriggerDate = null;
        else
            //Using constant simpleDateFormat, generate string representation of calendar date, set as nextTriggerDate
            this.nextTriggerDate = formatForPattern.format(nextTriggerDate.getTime());
    }

    //Set next trigger date by passing in string representation (pulled from database)
    public void setNextTriggerDate(String nextTriggerDate){
        this.nextTriggerDate = nextTriggerDate;
    }

    //Returns GregorianCalendar form of next trigger date so that we can reset it after boot-ups if needed
    public GregorianCalendar getCalendarFromNextTriggerDate(){
        //If nextTriggerDate is null, alarm is not set to run, so we just quit while we're ahead
        if(nextTriggerDate == null)
            return null;

        try {
            Date alarmDate = formatForPattern.parse(nextTriggerDate);
            GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
            assert alarmDate != null;
            cal.setTime(alarmDate);
            return cal;
        } catch(ParseException pe){
            pe.printStackTrace();
        }
        //Return null if this doesn't work. As long as we keep formatting consistent, should be no problems with any exceptions/nulls
        return null;
    }

}

package me.eli.jitteralarm.utilities;

import androidx.annotation.NonNull;

public class AlarmInfo implements Comparable<AlarmInfo> {

    //Stores the important information about an alarm
    private String alarmName; //Name of the alarm
    private String alarmTime; //Time the alarm triggers - HH:MM AM/PM
    private String offsetTime; //In what offset the alarm is allowed to trigger - HH:MM:SS
    private boolean[] triggerDays; //What days the alarm will trigger. Index 0 = Sunday, 6 = Saturday

    //Set up our AlarmInfo object with initial data and trigger days in boolean array form
    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, boolean[] triggerOnDay){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
        this.triggerDays = triggerOnDay;
    }

    //Alternative constructor that can be used to pass in a string representation of trigger days
    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, String triggerDays){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
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
        return alarmName + "; " + alarmTime + "; " + offsetTime + "; " + getTriggerString();
    }

    //Checks if this alarm and another are exactly the same so that we can check for duplicates elsewhere.
    public boolean isIdenticalTo(Object o){
        if(!(o instanceof AlarmInfo)) return false;
        AlarmInfo other = (AlarmInfo) o;
        return other.getAlarmName().equals(alarmName) && other.getAlarmTime().equals(alarmTime)
                && other.getOffsetTime().equals(offsetTime) && other.getTriggerString().equals(this.getTriggerString());
    }

    //Reserved for future use if necessary
    //Would compare to see if two alarms had the same name (not exactly identical, implying one is an edited version of another)
    public boolean isSameAlarmNameAs(AlarmInfo alarm){
        return alarm.getAlarmName().equals(this.alarmName);
    }
}

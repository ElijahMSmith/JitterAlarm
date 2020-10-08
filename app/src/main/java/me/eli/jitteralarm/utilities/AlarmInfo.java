package me.eli.jitteralarm.utilities;

import androidx.annotation.NonNull;

public class AlarmInfo implements Comparable<AlarmInfo> {

    private String alarmName;
    private String alarmTime;
    private String offsetTime;
    private boolean[] triggerDays; //0 = Sunday, 6 = Saturday

    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, boolean[] triggerOnDay){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
        this.triggerDays = triggerOnDay;
    }

    public AlarmInfo(String alarmName, String alarmTime, String offsetTime, String triggerDays){
        this.alarmName = alarmName;
        this.alarmTime = alarmTime;
        this.offsetTime = offsetTime;
        setTriggerArray(triggerDays);
    }

    public String getAlarmName() {
        return alarmName;
    }

    protected void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    protected void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getOffsetTime() {
        return offsetTime;
    }

    protected void setOffsetTime(String offsetTime) {
        this.offsetTime = offsetTime;
    }

    public boolean[] getTriggerArray() {
        return triggerDays;
    }

    public String getTriggerString() {
        StringBuilder triggers = new StringBuilder();

        for(Boolean b :triggerDays){
            triggers.append(b ? "T" : "F");
        }

        return triggers.toString();
    }

    protected void setTriggerArray(boolean[] triggerDays) {
        this.triggerDays = triggerDays;
    }

    protected void setTriggerArray(String triggerString){
        triggerDays = new boolean[7];
        for(int i = 0; i < 7; i++){
            triggerDays[i] = triggerString.charAt(i) == 'T';
        }
    }

    @Override
    public int compareTo(AlarmInfo other){
        return alarmName.compareTo(other.getAlarmName());
    }

    @NonNull
    @Override
    public String toString(){
        return alarmName + "; " + alarmTime + "; " + offsetTime + "; " + getTriggerString();
    }

    public boolean isIdenticalTo(Object o){
        if(!(o instanceof AlarmInfo)) return false;
        AlarmInfo other = (AlarmInfo) o;
        return other.getAlarmName().equals(alarmName) && other.getAlarmTime().equals(alarmTime)
                && other.getOffsetTime().equals(offsetTime) && other.getTriggerString().equals(this.getTriggerString());
    }

    public boolean isSameAlarmNameAs(AlarmInfo alarm){
        return alarm.getAlarmName().equals(this.alarmName);
    }
}

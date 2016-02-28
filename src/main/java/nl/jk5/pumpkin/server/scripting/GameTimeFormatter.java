package nl.jk5.pumpkin.server.scripting;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class GameTimeFormatter {

    // Locale? What locale? Seriously though, since this would depend on the
    // server's locale I think it makes more sense to keep it English always.
    private static final List<String> weekDays = ImmutableList.of("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    private static final List<String> shortWeekDays = ImmutableList.of("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
    private static final List<String> months = ImmutableList.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
    private static final List<String> shortMonths = ImmutableList.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    private static final List<String> amPm = ImmutableList.of("AM", "PM");
    private static final List<? extends List<Integer>> monthLengths = ImmutableList.of(
            ImmutableList.of(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31),
            ImmutableList.of(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    );

    private GameTimeFormatter() {
    }

    public static class DateTime {
        public final int year;
        public final int month;
        public final int day;
        public final int weekDay;
        public final int yearDay;
        public final int hour;
        public final int minute;
        public final int second;

        public DateTime(int year, int month, int day, int weekDay, int yearDay, int hour, int minute, int second) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.weekDay = weekDay;
            this.yearDay = yearDay;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }
    }

    // See http://www.cplusplus.com/reference/ctime/strftime/
    private static String format(char c, DateTime t){
        switch (c){
            case 'a': return shortWeekDays.get(t.weekDay - 1);
            case 'A': return weekDays.get(t.weekDay - 1);
            case 'b': return shortMonths.get(t.month - 1);
            case 'B': return months.get(t.month - 1);
            case 'c': return format("%a %b %d %H:%M:%S %Y", t);
            case 'C': return String.format("%02d", t.year / 100);
            case 'd': return String.format("%02d", t.day);
            case 'D': return format("%m/%d/%y", t);
            case 'e': return String.format("% 2d", t.day);
            case 'F': return format("%Y-%m-%d", t);
            //case 'g': return "";
            //case 'G': return "";
            case 'h': return format("%b", t);
            case 'H': return String.format("%02d", t.hour);
            case 'I': return String.format("%02d", (t.hour + 11) % 12 + 1);
            case 'j': return String.format("%03d", t.yearDay);
            case 'm': return String.format("%02d", t.month);
            case 'M': return String.format("%02d", t.minute);
            case 'n': return "\n";
            case 'p': return amPm.get(t.hour < 12 ? 0 : 1);
            case 'r': return format("%I:%M:%S %p", t);
            case 'R': return format("%H:%M", t);
            case 'S': return String.format("%02d", t.second);
            case 't': return "\t";
            case 'T': return format("%H:%M:%S", t);
            //case 'u': return "";
            //case 'U': return "";
            //case 'V': return "";
            case 'w': return String.valueOf(t.weekDay - 1);
            //case 'W': return "";
            case 'x': return format("%D", t);
            case 'X': return format("%T", t);
            case 'y': return String.format("%02d", t.year % 100);
            case 'Y': return String.format("%04d", t.year);
            //case 'z': return "";
            //case 'Z': return "";
            case '%': return "%";
            default: return "";
        }
    }

    private static List<Integer> monthLengthsForYear(int year){
        if(year % 4 == 0 && year % 100 != 0 || year % 400 == 0){
            return monthLengths.get(0);
        }else{
            return monthLengths.get(1);
        }
    }

    public static DateTime parse(double time){
        long day = (long)(time / 24000);
        int weekDay = (int)((4 + day) % 7);
        int year = (int) 1970 + (int) (day / 365.2425);
        int yearDay = (int)(day % 365.2425);
        day = yearDay;
        List<Integer> monthLengths = monthLengthsForYear(year);
        int month = 0;
        while(day > monthLengths.get(month)){
            day = day - monthLengths.get(month);
            month = month + 1;
        }

        int seconds = (int)((time % 24000) * 60 * 60 / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        int hours = (1 + minutes / 60) % 24;
        minutes = minutes % 60;

        return new DateTime(year, month + 1, (int) day + 1, weekDay + 1, yearDay + 1, hours, minutes, seconds);
    }

    public static String format(String format, DateTime time){
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < format.length(); i++){
            char c = format.charAt(i);
            if(c == '%' && i + 1 < format.length()){
                result.append(format(format.charAt(i + 1), time));
                i ++;
            }else{
                result.append(c);
            }
        }
        return result.toString();
    }

    public static int mktime(int year, int mon, int mday, int hour, int min, int sec){
        if(year < 1970 || mon < 1 || mon > 12) return -1;
        List<Integer> monthLengths = monthLengthsForYear(year);
        int days = (int) Math.ceil((year - 1970) * 365.2425);
        for(int m = 0; m < mon - 1; m ++){
            days += monthLengths.get(m);
        }
        days += mday - 1;
        int secs = sec + (min + (hour - 1 + days * 24) * 60) * 60;
        return secs < 0 ? -1 : secs;
    }
}

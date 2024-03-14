package CommonClient;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static String parseVisitDate(Timestamp visitTime) {
        // Convert the Timestamp to a Date object
        Date date = new Date(visitTime.getTime());

        // Create a SimpleDateFormat instance with "yyyy-MM-dd" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Format the Date object into a string
        return dateFormat.format(date);
    }

    public static String parseVisitTime(Timestamp visitTime) {
        // Create a SimpleDateFormat instance with "yyyy-MM-dd" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        // Format the Date object into a string
        return dateFormat.format(visitTime);
    }

    public static Boolean isIDValid(String ID) {
        // Check if the string length is exactly 9 characters
        if (ID.length() != 9) {
            return false;
        }

        // Check if each character is a digit
        for (char c : ID.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        // If all characters are digits and string length is 9, return true
        return true;
    }
}

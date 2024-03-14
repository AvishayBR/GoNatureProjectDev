package CommonUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    /**
     * Checks if the given string is a valid email address.
     *
     * @param email - the email address to check
     * @return true if the string is a valid email address false otherwise
     */
    public static boolean isEmailAddressValid(String email) {
        if (email == null)
            return false;
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    /**
     * Checks if the given order id is valid.
     *
     * @param orderID - string of the order id.
     * @return true if the order id is valid (positive integer), false otherwise
     */
    public static boolean isValidOrderID(String orderID) {
        return (isAllDigits(orderID) && Integer.parseInt(orderID) > 0);
    }

    public static int convertStringToInt(String str) {
        try {
            // Convert the string to an int
            int intValue = Integer.parseInt(str);

            return intValue;
        } catch (NumberFormatException e) {
            // Handle the case when the string is not a valid integer
            System.err.println("Error: Not a valid integer");
        }
        return 0;
    }

    /**
     * Checks if the given id is value (9 digits).
     *
     * @param id - string of the id to check.
     * @return true if the id is valid, false otherwise.
     */
    public static boolean isValidID(String id) {
        return (isAllDigits(id) && id.length() == 9);
    }

    /**
     * Checks if the given name is valid.
     *
     * @param name - string of the name to check.
     * @return true if the name is valid, false otherwise.
     */
    public static boolean isValidName(String name) {
        if (name == null)
            return false;
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetter(name.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Checks if the given phone number is valid.
     *
     * @param phone - string of the phone number to check.
     * @return true if the phone number is valid, false otherwise.
     */
    public static boolean isValidPhone(String phone) {
        return (isAllDigits(phone) && phone.startsWith("05") && phone.length() == 10);
    }


    /**
     * Checks if the given string contains only digits.
     *
     * @param str - string to check.
     * @return true if the string contains only digits, false otherwise.
     */
    private static boolean isAllDigits(String str) {
        if (str == null)
            return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Checks if any of the provided strings are empty.
     *
     * @param strings Varargs parameter to accept multiple string arguments.
     * @return true if any string is empty, false otherwise.
     */
    public static boolean anyStringEmpty(String... strings) {
        for (String str : strings) {
            if (str == null || str.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}

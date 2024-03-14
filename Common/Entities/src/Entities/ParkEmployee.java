package Entities;

import CommonUtils.CommonUtils;

import java.io.Serializable;

/**
 * Represents a park employee entity, extending the User class.
 */
public class ParkEmployee extends User implements Serializable {
    private String EmailAddress;
    private Park Park;

    /**
     * Constructs a new ParkEmployee object with the provided user, email address, and park.
     *
     * @param user        The user object containing username and password.
     * @param EmailAddress The email address of the park employee.
     * @param Park        The park associated with the employee.
     */
    public ParkEmployee(User user, String EmailAddress, Park Park) {
        super(user.getUsername(), user.getPassword());
        this.EmailAddress = EmailAddress;
        this.Park = Park;
        role = Role.ROLE_PARK_EMPLOYEE;
    }

    /**
     * Constructs a new ParkEmployee object with the provided username, password, email address, and park.
     *
     * @param username    The username of the park employee.
     * @param password    The password of the park employee.
     * @param EmailAddress The email address of the park employee.
     * @param Park        The park associated with the employee.
     */
    public ParkEmployee(String username, String password, String EmailAddress, Park Park) {
        super(username, password);
        this.EmailAddress = EmailAddress;
        this.Park = Park;
        role = Role.ROLE_PARK_EMPLOYEE;

    }

    /**
     * Sets the park associated with the employee.
     *
     * @param park The park to be set.
     */
    public void setPark(Park park) {
        Park = park;
    }

    /**
     * Sets the email address of the park employee.
     *
     * @param emailAddress The email address to be set.
     */
    public void setEmailAddress(String emailAddress) {
        if (CommonUtils.isEmailAddressValid(emailAddress)) {
            EmailAddress = emailAddress;
        }
    }

    /**
     * Retrieves the email address of the park employee.
     *
     * @return The email address of the park employee.
     */
    public String getEmailAddress() {
        return EmailAddress;
    }

    /**
     * Retrieves the park associated with the employee.
     *
     * @return The park associated with the employee.
     */
    public Park getPark() {
        return Park;
    }
}

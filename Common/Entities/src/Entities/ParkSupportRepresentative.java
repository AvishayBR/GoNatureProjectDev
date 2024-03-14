package Entities;

import java.io.Serializable;

/**
 * The ParkSupportRepresentative class represents a support representative working in a park.
 * This class extends ParkEmployee and adds functionality related to park access management.
 */
public class ParkSupportRepresentative extends ParkEmployee implements Serializable {
    private ParkBank AccessParks;

    /**
     * Constructs a ParkSupportRepresentative object with the specified parameters.
     *
     * @param username    The username of the support representative.
     * @param password    The password of the support representative.
     * @param emailAddress The email address of the support representative.
     * @param park        The park associated with the support representative.
     */
    public ParkSupportRepresentative(String username, String password, String emailAddress, Park park) {
        super(username, password, emailAddress, park);
        AccessParks = new ParkBank(park);
        role = Role.ROLE_PARK_SUPPORT_REPRESENTATIVE;

    }

    /**
     * Constructs a ParkSupportRepresentative object with the specified parameters.
     *
     * @param user        The user object representing the support representative.
     * @param emailAddress The email address of the support representative.
     * @param park        The park associated with the support representative.
     */
    public ParkSupportRepresentative(User user, String emailAddress, Park park) {
        super(user, emailAddress, park);
        AccessParks = new ParkBank(park);
        role = Role.ROLE_PARK_SUPPORT_REPRESENTATIVE;

    }

    /**
     * Constructs a ParkSupportRepresentative object with the specified parameters.
     *
     * @param username    The username of the support representative.
     * @param password    The password of the support representative.
     * @param emailAddress The email address of the support representative.
     * @param park        The park associated with the support representative.
     * @param parks       The ParkBank object containing park access information.
     */
    public ParkSupportRepresentative(String username, String password, String emailAddress, Park park, ParkBank parks) {
        super(username, password, emailAddress, park);
        AccessParks = parks;
        role = Role.ROLE_PARK_SUPPORT_REPRESENTATIVE;

    }

    /**
     * Constructs a ParkSupportRepresentative object with the specified parameters.
     *
     * @param user        The user object representing the support representative.
     * @param emailAddress The email address of the support representative.
     * @param park        The park associated with the support representative.
     * @param parks       The ParkBank object containing park access information.
     */
    public ParkSupportRepresentative(User user, String emailAddress, Park park, ParkBank parks) {
        super(user, emailAddress, park);
        AccessParks = parks;
        role = Role.ROLE_PARK_SUPPORT_REPRESENTATIVE;

    }

    /**
     * Gets the ParkBank object containing park access information.
     *
     * @return The ParkBank object.
     */
    public ParkBank getAccessParks() {
        return AccessParks;
    }

    /**
     * Sets the ParkBank object containing park access information.
     *
     * @param accessParks The ParkBank object to set.
     */
    public void setAccessParks(ParkBank accessParks) {
        AccessParks = accessParks;
    }

    /**
     * Adds access to a park.
     *
     * @param park The park to grant access to.
     */
    public void addParkAccess(Park park) {
        AccessParks.insertPark(park);
    }

    /**
     * Removes access to a park.
     *
     * @param park The park to revoke access from.
     */
    public void removeParkAccess(Park park) {
        AccessParks.deletePark(park);
    }
}

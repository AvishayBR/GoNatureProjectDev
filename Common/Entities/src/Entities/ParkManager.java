package Entities;

import java.io.Serializable;

/**
 * The ParkManager class represents a manager working in a park.
 * This class extends ParkEmployee and inherits basic employee functionality.
 */
public class ParkManager extends ParkEmployee implements Serializable {
    private String ParkID;

    /**
     * Constructs a ParkManager object with the specified parameters.
     *
     * @param username    The username of the manager.
     * @param password    The password of the manager.
     * @param emailAddress The email address of the manager.
     * @param ParkID        The parkID associated with the manager.
     */
    public ParkManager(String username, String password, String emailAddress, String ParkID) {
        super(username, password, emailAddress, null);
        this.ParkID = ParkID;
        role = Role.ROLE_PARK_MGR;
    }

    /**
     * Constructs a ParkManager object with the specified parameters.
     *
     * @param user        The user object representing the manager.
     * @param emailAddress The email address of the manager.
     * @param ParkID        The parkID associated with the manager.
     */
    public ParkManager(User user, String emailAddress, String ParkID) {
        super(user, emailAddress, null);
        this.ParkID = ParkID;
        role = Role.ROLE_PARK_MGR;
    }

    /**
     * Gets the park ID associated with the manager.
     *
     * @return The park ID associated with the manager.
     */
    public String getParkID() {
        return ParkID;
    }
}


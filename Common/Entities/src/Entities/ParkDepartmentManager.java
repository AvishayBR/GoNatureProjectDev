package Entities;

import java.io.Serializable;

/**
 * The ParkDepartmentManager class represents a department manager working in a park.
 * This class extends ParkEmployee and provides functionality related to managing a department within a park.
 */
public class ParkDepartmentManager extends ParkEmployee implements Serializable {
    private Integer DepartmentID;
    private ParkBank parks;

    /**
     * Constructs a ParkDepartmentManager object with the specified parameters.
     *
     * @param username     The username of the department manager.
     * @param password     The password of the department manager.
     * @param emailAddress  The email address of the department manager.
     * @param park         The park associated with the department manager.
     * @param parks        The ParkBank object containing park access information.
     * @param departmentID The ID of the department managed by the department manager.
     */
    public ParkDepartmentManager(String username, String password, String emailAddress, Park park, ParkBank parks, Integer departmentID) {
        super(username, password, emailAddress, park);
        this.parks = parks;
        DepartmentID = departmentID;
        role = Role.ROLE_PARK_DEPARTMENT_MGR;

    }

    /**
     * Constructs a ParkDepartmentManager object with the specified parameters.
     *
     * @param user         The user object representing the department manager.
     * @param emailAddress  The email address of the department manager.
     * @param park         The park associated with the department manager.
     * @param parks        The ParkBank object containing park access information.
     * @param departmentID The ID of the department managed by the department manager.
     */
    public ParkDepartmentManager(User user, String emailAddress, Park park, ParkBank parks, Integer departmentID) {
        super(user, emailAddress, park);
        this.parks = parks;
        DepartmentID = departmentID;
        role = Role.ROLE_PARK_DEPARTMENT_MGR;

    }

    /**
     * Gets the ID of the department managed by the department manager.
     *
     * @return The department ID.
     */
    public Integer getDepartmentID() {
        return DepartmentID;
    }

    /**
     * Sets the ID of the department managed by the department manager.
     *
     * @param departmentID The department ID to set.
     */
    public void setDepartmentID(Integer departmentID) {
        DepartmentID = departmentID;
    }

    /**
     * Gets the ParkBank object containing park access information.
     *
     * @return The ParkBank object.
     */
    public ParkBank getParks() {
        return parks;
    }

    /**
     * Sets the ParkBank object containing park access information.
     *
     * @param parks The ParkBank object to set.
     */
    public void setParks(ParkBank parks) {
        this.parks = parks;
    }
}

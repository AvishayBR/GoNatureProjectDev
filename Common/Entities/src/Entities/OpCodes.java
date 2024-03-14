package Entities;

public enum OpCodes {
    OP_SYNC_HANDSHAKE(0),
    OP_GET_VISITOR_ORDERS(1), /* Expect: String - VisitorID, Return: ArrayList<Order> */
    OP_GET_VISITOR_GROUP_GROUP_GUIDE_ORDERS(2), /* Same method as above */
    OP_HANDLE_VISITATION_EMAIL_CHANGE(3), /* Expect: String - OrderID, String - emailAddress. Returns: boolean indicator*/
    OP_HANDLE_VISITATION_CANCEL_ORDER(4), /* Expect: String - OrderID. Returns: boolean indicator */
    OP_HANDLE_GROUP_VISITATION_EMAIL_CHANGE(5), /* Same as HANDLE_VISITATION_EMAIL_CHANGE */
    OP_HANDLE_GROUP_VISITATION_CANCEL_ORDER(6), /* Same as HANDLE_VISITATION_CANCEL_ORDER */
    OP_CREATE_NEW_VISITATION(7), /* Expect: Order, Returns: boolean indicator */
    OP_CREATE_NEW_GROUP_VISITATION(8), /* Same method as above - Perform an upcast to Order!!! Rework group size + group guide ID... */
    OP_INSERT_VISITATION_TO_WAITLIST(9),
    OP_INSERT_GROUP_VISITATION_TO_WAITLIST(10),
    OP_HANDLE_VISITATION_STATUS(11), /* Expect: String - OrderID, OrderStatus status. Returns: boolean indicator*/
    OP_HANDLE_GROUP_VISITATION_STATUS(12), /* Same method as above */
    OP_GET_VISITATION_BY_ID(13), /* Expect: String - OrderID, Returns: Order with data if order is found. Order with null data if order is not found. Null if exception was thrown */
    OP_GET_GROUP_VISITATION_BY_ID(14), /* Same method as above */
    OP_GET_PARK_DETAILS_BY_PARK_ID(15), /* Expect: String - ParkID, Returns: Park instance with data if park is found. Park with null data if park is not found. Null if exception was thrown */
    OP_SIGN_IN(16), /* Expect: String - username, String - password. Returns: User with data if success. User with null data if failure. Null if exception was thrown */
    OP_SIGN_IN_ALREADY_LOGGED_IN(17), /* Expect: String - username, null. No return (One way from Server) */
    OP_LOGOUT(18), /* Expect: String - username, null. Returns: */
    OP_QUIT(19), /* Expect: */
    OP_GET_DEPARTMENT_MANAGER_PARKS(20),
    OP_ORDER_ALREADY_EXIST(21),
    OP_GET_USER_ORDERS_BY_USERID(22), /* Expect: String[] - userID 0, orderID 1. Returns: Order */
    OP_DB_ERR(999);

    private final int opCodeValue;

    OpCodes(int opCodeValue) {
        this.opCodeValue = opCodeValue;
    }

    public int getOpCodeValue() {
        return this.opCodeValue;
    }
}
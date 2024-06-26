package Entities;

/**
 * Represents the status of an order.
 */
public enum OrderStatus {
    /**
     * Represents the status of an order being on the waitlist.
     */
    STATUS_WAITLIST(1),

    /**
     * Represents the status of an order being accepted.
     */
    STATUS_ACCEPTED(2),

    /**
     * Represents the status of an order being confirmed, but payment is pending.
     */
    STATUS_CONFIRMED_PENDING_PAYMENT(3),

    /**
     * Represents the status of an order being confirmed, but the customer is absent.
     */
    STATUS_CONFIRMED_AND_ABSENT(4),

    /**
     * Represents the status of an order being confirmed and paid.
     */
    STATUS_CONFIRMED_PAID(5),

    /**
     * Represents the status of a cancelled order.
     */
    STATUS_CANCELLED(6),

    /**
     * Represents a spontaneous status of an order.
     */
    STATUS_SPONTANEOUS_ORDER(7),

    /**
     * Represents the status of an order being pending confirmation (SMS/Email has been sent regarding the order).
     */
    STATUS_PENDING_CONFIRMATION(8),

    /**
     * Represents order that has been fulfilled
     */
    STATUS_FULFILLED(9),
    STATUS_SPONTANEOUS_ORDER_PENDING_PAYMENT(10);

    private int orderStatus;

    /**
     * Constructs an OrderStatus enum with the specified order status value.
     *
     * @param orderStatus The value representing the order status.
     */
    OrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * Returns the order status value associated with the enum constant.
     *
     * @return The order status value.
     */
    public int getOrderStatus() {
        return this.orderStatus;
    }
}

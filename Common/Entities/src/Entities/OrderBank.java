package Entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a bank of orders of a specific type.
 */
public class OrderBank implements Iterable<Order>, Serializable {
    private ArrayList<Order> orders;
    private OrderType ordersType;

    /**
     * Constructs an empty OrderBank for a specific order type.
     *
     * @param orderType The type of orders this bank will hold.
     */
    public OrderBank(OrderType orderType) {
        this.ordersType = orderType;
        orders = new ArrayList<>();
    }

    /**
     * Constructs an OrderBank with the given initial order of a specific type.
     *
     * @param order The initial order to be added to the OrderBank.
     */
    public OrderBank(Order order) {
        orders = new ArrayList<>();
        ordersType = order.getOrderType();
        orders.add(order);
    }

    /**
     * Inserts an order into the OrderBank if it matches the bank's order type.
     *
     * @param order The order to be inserted.
     * @return True if the order was successfully inserted, false otherwise.
     */
    public Boolean insertOrder(Order order) {
        if (order.getOrderType() == ordersType)
            return orders.add(order);
        return false;
    }

    public Boolean insertOrderArray(ArrayList<Order> orders) {
        ArrayList<Order> newOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getOrderType() == ordersType)
                newOrders.add(order);
            else
                return false;
        }
        this.orders = newOrders;
        return false;
    }

    /**
     * Retrieves the type of orders held by this OrderBank.
     *
     * @return The type of orders held by this OrderBank.
     */
    public OrderType getOrdersType() {
        return ordersType;
    }

    /**
     * Deletes an order from the OrderBank.
     *
     * @param order The order to be deleted.
     */
    public void deleteOrder(Order order) {
        if (orders.contains(order))
            orders.remove(order);
    }

    /**
     * Checks if an order exists in the OrderBank.
     *
     * @param order The order to be checked.
     * @return True if the order exists in the OrderBank, false otherwise.
     */
    public Boolean isExists(Order order) {
        return orders.contains(order);
    }

    /**
     * Retrieves orders with a specific status from the OrderBank.
     *
     * @param status The status of the orders to retrieve.
     * @return An OrderBank containing orders with the specified status.
     */
    public OrderBank getOrdersByStatus(OrderStatus status) {
        OrderBank ordersByStatus = new OrderBank(ordersType);
        for (Order order : orders) {
            if (order.getOrderStatus() == status)
                ordersByStatus.insertOrder(order);
        }
        return ordersByStatus;
    }

    /**
     * Changes the status of an existing order in the OrderBank.
     *
     * @param order     The order whose status is to be changed.
     * @param newStatus The new status to set for the order.
     */
    public void ChangeExistingOrderStatus(Order order, OrderStatus newStatus) {
        if (orders.contains(order)) {
            order.setOrderStatus(newStatus);
        }
    }

    /**
     * Returns an iterator over the elements in this OrderBank.
     *
     * @return An iterator over the elements in this OrderBank.
     */
    @Override
    public Iterator<Order> iterator() {
        return new OrderIterator();
    }

    /**
     * Iterator implementation for OrderBank.
     */
    private class OrderIterator implements Iterator<Order> {
        private int currentIndex = 0;

        /**
         * Returns true if the iteration has more elements.
         *
         * @return True if the iteration has more elements, false otherwise.
         */
        @Override
        public boolean hasNext() {
            return currentIndex < orders.size();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return The next element in the iteration.
         * @throws NoSuchElementException If the iteration has no more elements.
         */
        @Override
        public Order next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the OrderBank");
            }
            return orders.get(currentIndex++);
        }
    }
}

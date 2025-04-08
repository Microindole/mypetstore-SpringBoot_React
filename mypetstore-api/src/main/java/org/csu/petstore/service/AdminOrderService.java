package org.csu.petstore.service;

import org.csu.petstore.entity.*;
import org.csu.petstore.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminOrderService {
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private AdminOrderMapper orderMapper;
    @Autowired
    private LineItemMapper lineItemMapper;
    @Autowired
    private SequenceMapper sequenceMapper;
    @Autowired
    private ItemQuantityMapper itemQuantityMapper;
    @Autowired
    private AdminOrderStatusMapper orderStatusMapper;

    public void insertOrder(Order order) {
        order.setOrderId(getNextId("ordernum"));

        List<LineItem> lineItemList = lineItemMapper.getLineItemsByOrderId(order.getOrderId());
        for (LineItem item : lineItemList) {
            String itemId = item.getItemId();
            Integer increment = item.getQuantity();
            ItemQuantity itemQuantity = itemQuantityMapper.selectById(itemId);
            itemQuantityMapper.deleteById(itemId);
            itemQuantity.setQuantity(itemQuantity.getQuantity() - increment);
            itemQuantityMapper.insert(itemQuantity);

        }
        orderMapper.insertOrder(order);
        orderMapper.insertOrderStatus(order);
        for (LineItem item : lineItemList) {
            item.setOrderId(order.getOrderId());
            lineItemMapper.insertLineItem(item);
        }
    }

    public Order getOrder(int orderId) {
        Order order = orderMapper.getOrder(orderId);

        List<LineItem> lineItemList = lineItemMapper.getLineItemsByOrderId(orderId);
        for (LineItem value : lineItemList) {
            Item item = itemMapper.getItem(value.getItemId());
            ItemQuantity itemQuantity = itemQuantityMapper.selectById(value.getItemId());
            //item.setQuantity(itemQuantity.getQuantity());
            value.setItem(item);
        }
        return order;
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderMapper.getOrdersByUsername(username);
    }

    public int getNextId(String name) {
        Sequence sequence = new Sequence(name, -1);
        sequence = sequenceMapper.getSequence(sequence);
        if (sequence == null) {
            throw new RuntimeException("Error: A null sequence was returned from the database (could not get next " + name
                    + " sequence).");
        }
        Sequence parameterObject = new Sequence(name, sequence.getNextId() + 1);
        sequenceMapper.updateSequence(parameterObject);
        return sequence.getNextId();
    }

    public void updateOrderStatus(Order order) {
        orderStatusMapper.updateOrderStatus(order);
    }

    public void updateOrderAddress(Order order) {
        orderMapper.updateOrderAddress(order);
    }

    public List<Order> getOrderList() {
        return orderMapper.getOrderList();
    }

    public void deleteOrder(int orderId) {
        orderMapper.deleteOrderStatus(orderId);
        orderMapper.deleteOrder(orderId);
        lineItemMapper.deleteLineItems(orderId);
    }

    public void updateOut(int orderId) {
        orderMapper.updateOut(orderId);
    }

    public List<Order> getUnSentOrderList() {
        return orderMapper.getUnSentOrderList();
    }


}
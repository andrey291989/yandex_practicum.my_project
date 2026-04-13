package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderDetailsDTO;
import com.example.ecommerce.dto.OrderSummaryDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    public List<OrderSummaryDTO> getAllOrderSummaries() {
        return orderRepository.findAll().stream()
                .map(OrderSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public OrderDetailsDTO getOrderDetails(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.getItems().size(); // Триггерим загрузку (если нужно)
        return OrderDetailsDTO.fromEntity(order);
    }


    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
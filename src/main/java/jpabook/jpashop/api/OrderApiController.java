package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * x:n의 연관관계 맵핑 최적화
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public GetOrderResponse ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(
                    o -> o.getItem().getName()
            );
        }

        return new GetOrderResponse(all);
    }

    /**
     * 조회시 Entity 직접 맵핑을 VO/DTO를 사용하여 간접적 맵핑으로 변경
     */
    @GetMapping("/api/v2/orders")
    public GetOrderResponse ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<OrdersDto> collection = orders.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList());
        return new GetOrderResponse(collection);
    }

    /**
     * 1:n 조회시 Entity 식별 ID를 기준으로 중복데이터 제거
     */
    @GetMapping("/api/v3/orders")
    public GetOrderResponse ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrdersDto> collection = orders.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList());
        return new GetOrderResponse(collection);
    }

    /**
     * 1:n 인경우 페이징 처리
     */
    @GetMapping("/api/v3.1/orders")
    public GetOrderResponse ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset
            , @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrdersDto> collection = orders.stream().map(o -> new OrdersDto(o)).collect(Collectors.toList());
        return new GetOrderResponse(collection);
    }

    /**
     * @xToMany 컬렉션 조회 DTO 직접 조회
     */
    @GetMapping("/api/v4/orders")
    public GetOrderResponse ordersV4() {
        return new GetOrderResponse(orderQueryRepository.findOrderQueryDtos());
    }

    /**
     * @xToMany 컬렉션 조회 DTO 직접 조회 최적화
     */
    @GetMapping("/api/v5/orders")
    public GetOrderResponse ordersV5() {
        return new GetOrderResponse(orderQueryRepository.findAllByDto_optimization());
    }

    @GetMapping("/api/v6/orders")
    public GetOrderResponse ordersV6() {
        /* // 한번으로 조회하는 구문 - 다만, 1:N Join으로 인해서 중복데이터 발생
        return new GetOrderResponse(orderQueryRepository.findAllByDto_flat());
        */

        // 한번으로 조회하는 구문 - 1:N Join으로 인한 중복데이터 보정 (코드로 제거)
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return new GetOrderResponse(flats.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList()));
    }


    @Data
    @AllArgsConstructor
    static class GetOrderResponse<T> {
        private T data;
    }

    @Data
    static class OrdersDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrdersDto(Order order) {
            order.getOrderItems().stream().forEach(o -> o.getItem().getName());

            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().map(o ->
                new OrderItemDto(o)
            ).collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {
        private Long id;
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(
                OrderItem orderItem
        ) {
            this.id = orderItem.getId();
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}

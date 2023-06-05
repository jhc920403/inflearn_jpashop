package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ** xToOne Mapping Type **
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * 1. 양방향 연관관계 맺어져 있는 경우 무한루프 상태에 빠지된다.
     * -> 해결 방법은 @JsonIgnore을 한쪽 Entity에 적용해줘야 한다. (Member Entity에 적용)
     * 
     * 2. Jackson에서 응답을 Json화 시킬 때 EntityProxy일 경우 오류가 발생
     * -> 해결 방법은 응답 전용 VO/DTO를 생성할 것, 필요시 Proxy를 초기화시킬 것
     *
     * 3. V2 방법으로 수정했을 때 연관관계 맵핑된 정보까지 VO/DTO에 넣은 경우 N+1 문제가 발생
     * -> 해결 방법은 모두 LAZY로 설정 후 fetch join을 실행
     *
     * 호출 Url : http://localhost:8080/api/v1/simple-orders
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public GetOrderResponse ordersV2() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> collect = all.stream().map(m ->
                SimpleOrderDto.initOrder().order(m).build()
        ).collect(Collectors.toList());

        return new GetOrderResponse(collect);
    }

    @GetMapping("/api/v3/simple-orders")
    public GetOrderResponse ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> collect = orders.stream().map(
                o -> new SimpleOrderDto(o)
        ).collect(Collectors.toList());

        return new GetOrderResponse(collect);
    }

    /**
     * Entity의 반환값을 바로 DTO로 받아오는 구문
     */
    @GetMapping("/api/v4/simple-orders")
    public GetOrderResponse ordersV4() {
        return new GetOrderResponse(orderSimpleQueryRepository.findOrderDtos());
    }

    @Data
    @AllArgsConstructor
    static class GetOrderResponse<T>  {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class SimpleOrderDto {
        private Long orderId;
        private Member member;
        private List<OrderItem> orderItems;
        private Delivery delivery;
        private LocalDateTime orderDate;
        private OrderStatus status;

        @Builder(
                builderClassName = "init"
                , builderMethodName = "initOrder"
        )
        public SimpleOrderDto(Order order) {
            order.getOrderItems().stream().forEach(o -> o.getItem().getName());

            order.getMember().getName();        // Lazy 초기화
            order.getDelivery().getAddress();   // Lazy 초기화

            setOrderId(order.getId());
            setMember(order.getMember());
            setOrderItems(order.getOrderItems());
            setDelivery(order.getDelivery());
            setOrderDate(order.getOrderDate());
            setStatus(order.getStatus());
        }
    }
}

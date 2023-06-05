package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 극한의 성능 최적화를 위해 API 스펙에 맞춰 필수 데이터만 조회하여 DTO로 직접 받는 클래스로 구성
 * -> 기본 Repository에서 별도로 구성한 이유는 논리적으로 보면 일반 Repository는 순수하게 Entity와 관련된 정보를 가공 없이 그대로 조회
 * -> 특정 목적에 맞춘 조회 Entity라는 것을 별도로 구분하여 다른 위치에서 사용하는 것을 최대한 방지할 수 있는 유지보수적 이점 제공
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * @xToMany 컬렉션 조회 DTO 직접 조회 - Where orderId = ? 로 비교하여 orderId 만큼 요청이 발생 
     * - @xToOne 연관관계에서는 1:1 관계이기 때문에 Join으로 조회 (Member, Delivery) - Row 증가 X
     * - @xToMany 연관관계에서는 1:N 관계이기 때문에 반복문으로 OrderId를 활용하여 조회 - Row 증가 O
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();

        result.stream().forEach(n -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(n.getOrderId());
            n.setOrderItems(orderItems);
        });

        return result;
    }

    public List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id = :orderId"
                , OrderItemQueryDto.class
        ). setParameter("orderId", orderId).getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o " +
                        "join o.member m " +
                        "join o.delivery d"
                , OrderQueryDto.class
        ).getResultList();
    }

    /**
     * @xToMany 컬렉션 조회 DTO 직접 조회 최적화 (일반적으로 2번 조회에 가능, IN에 들어가는 식별자 ID 값이 일정 범위(1000)을 넘어가면 3번 이상 발생 가능)
     * - Where orderId IN (?...) 형태로 조회하여 메모리상에서 연산
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id in :orderIds"
                , OrderItemQueryDto.class
                ).setParameter("orderIds" , orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap =
                orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto -> OrderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream().map(o -> {
            return o.getOrderId();
        }).collect(Collectors.toList());
    }

    /**
     * 한번에 조회 가능
     */
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count) from Order o " +
                        "join o.member m " +
                        "join o.delivery d " +
                        "join o.orderItems oi " +
                        "join oi.item i"
                , OrderFlatDto.class
        ).getResultList();
    }
}

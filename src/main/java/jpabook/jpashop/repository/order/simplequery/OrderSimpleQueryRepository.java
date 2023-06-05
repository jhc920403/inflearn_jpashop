package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 극한의 성능 최적화를 위해 API 스펙에 맞춰 필수 데이터만 조회하여 DTO로 직접 받는 클래스로 구성
 * -> 기본 Repository에서 별도로 구성한 이유는 논리적으로 보면 일반 Repository는 순수하게 Entity와 관련된 정보를 가공 없이 그대로 조회
 * -> 특정 목적에 맞춘 조회 Entity라는 것을 별도로 구분하여 다른 위치에서 사용하는 것을 최대한 방지할 수 있는 유지보수적 이점 제공
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d"
                , OrderSimpleQueryDto.class
        ).getResultList();
    }
}

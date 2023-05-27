package jpabook.jpashop.api;

import jpabook.jpashop.domain.Addresses;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * xToOne 관계(ManyToOne, OneToOne)
 * Order
 * Order > Member
 * Order > Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    /** 엔티티 직접노출(사용하지마)_안돌아감 */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();    // Lazy 강제 초기화
            order.getDelivery().getAddresses(); // Lazy 강제 초기화
        }
        // @hibernate5Module 사용안해줘서 에러뜸
        return all;
    }
    /** Dto 사용(쿼리너무많이 날리니 사용하지마) */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        // 오더 주문 2건
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        // 2건 루프돌며 xToOne 관련된 멤버, 배송 2개가 2번씩 쿼리를 날림 총 5번의 쿼리가 날라감
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
        // 위에 코드를 아래처럼 줄일 수 있음 (Ctrl+Alt+N) static impor로 Collectors까지 빼줄 수 있다
//        return orderRepository.findAllByString(new OrderSearch()).stream()
//                .map(SimpleOrderDto::new)
//                .collect(toList());
    }

    /** fetch join(재사용도가 높지만 쿼리가 지저분함) */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        // fetch join을 사용함
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(toList());
        return result;
    }

    /** jqpl 쿼리 (fetch join보다 재활용도가 낮음) */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        // repository에 jpql을 사용해서 Dto컬럼만 들고 옴
        return orderRepository.findOrderDtos();

    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Addresses address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();;
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddresses();
        }
    }
}

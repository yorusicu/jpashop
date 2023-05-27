package jpabook.jpashop.service;

import jpabook.jpashop.domain.Addresses;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        // given
        Member member = createMember();

        Book book = createBook("JPA빠게기", 10000, 10);

        int count = 1;
        int stock = book.getStockQuantity() - count;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), count);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, getOrder.getOrderItems().size(), "주문상품 종류수가 정확한가");
        assertEquals(10000 * count, getOrder.getTotalPrice(), "주문가격은 가격 * 수량");
        assertEquals(stock, book.getStockQuantity(), "주문 수량 만큼 재고 빠지나");
    }


    @Test
    public void 주문취소() throws Exception {
        // given
        Member member = createMember();
        Book item = createBook("빡시게 JPA", 10000, 10);

        int cnt = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), cnt);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태가 CANCEL");
        assertEquals(10, item.getStockQuantity(), "취소시 재고 증가");
    }

    @Test()
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Member member = createMember();
        Item book = createBook("하하호호JPA", 10000, 10);
        int orderCnt = 10;
        
        // when
        orderService.order(member.getId(), book.getId(), orderCnt);

        // then
        fail("재고 수량 부족 예외");

    }

    private Book createBook(String bookName, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(bookName);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원");
        member.setAddress(new Addresses("서울", "선릉", "12345"));
        em.persist(member);
        return member;
    }
}
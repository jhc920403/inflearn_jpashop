package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/*
  - Transactional은 기본적으로 Test에서는 Rollback을 수행하며, 원하지 않는 경우 '회원가입'과 같이 설정하면된다.
  - 다만, Rollback(false)로 설정하게 된다면 테스트가 끝난 후에도 데이터베이스에 저장된다.

  - 데이터베이스에 저장하지 않고 확인하고 싶은 경우 EntityManager 의존성을 받아 flush()를 진행하면 된다.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @Test
    //@Rollback(false)
    public void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long savedId = memberService.join(member);
        System.out.println("테스트 중입니다 :: " + savedId);

        // then
        em.flush();
        Assert.assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);
        memberService.join(member2);

        // then
        Assert.fail("예외가 발생해야 된다.");
    }
}
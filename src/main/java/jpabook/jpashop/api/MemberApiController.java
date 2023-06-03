package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 회원 등록 API
     * V1 : Controller에 들어오는 요청을 바로 Entity 객체로 받을 경우 API 스펙과 Entity간의 1:1 맵핑되어 결합도가 높아 추후 문제가 발생할 수 있다.
     *
     * 호출 Url : http://localhost:8080/api/v2/members
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

    /**
     * 회원 수정 API
     * 호출 Url : http://localhost:8080/api/v1/members/{id}
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2 (
            @PathVariable("id") Long id
            , @RequestBody @Valid UpdateMemberRequest request
    ) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberRequest {
        private Long id;
        private String name;
    }

    /**
     * 회원 조회
     * V1 : 회원 Entity에 연동되어 있어 API의 스펙 변동의 위험이 있으며, 필요 외의 데이터까지 노출하게 된다.
     *
     * 호출 Url : /api/v2/members
     */
    @GetMapping("/api/v1/members")
    public List<Member> getMemberV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public GetMemberResponse getMemberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream().map(m ->
            new MemberDto(m.getName())
        ).collect(Collectors.toList());

        return new GetMemberResponse(
                "Member Name"
                , collect.size()
                , collect
        );
    }

    @Data
    @AllArgsConstructor
    static class GetMemberResponse<T> {
        private String responseInfo;
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }
}

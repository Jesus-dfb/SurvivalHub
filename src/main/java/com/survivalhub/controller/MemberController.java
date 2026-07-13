package com.survivalhub.controller;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.Member;
import com.survivalhub.service.MemberService;
import com.survivalhub.service.WorldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/worlds/{worldId}/members")
public class MemberController {

    private final MemberService memberService;
    private final WorldService worldService;

    public MemberController(MemberService memberService, WorldService worldService) {
        this.memberService = memberService;
        this.worldService = worldService;
    }

    @GetMapping
    public ResponseEntity<List<Member>> getMembersByWorldId(
            @PathVariable Long worldId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        List<Member> members = memberService.getMembersByWorldId(worldId);

        return ResponseEntity.ok(members);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(
            @PathVariable Long worldId,
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        return memberService.getMemberById(worldId, memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Member> createMember(
            @PathVariable Long worldId,
            @RequestBody Member member,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        Member createdMember = memberService.createMember(worldId, member);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long worldId,
            @PathVariable Long memberId,
            @RequestBody Member member,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        return memberService.updateMember(worldId, memberId, member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long worldId,
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        if (!canAccessWorld(worldId, authentication)) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = memberService.deleteMember(worldId, memberId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private boolean canAccessWorld(Long worldId, Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        return worldService.getWorldByIdForOwner(worldId, user.getId()).isPresent();
    }
}

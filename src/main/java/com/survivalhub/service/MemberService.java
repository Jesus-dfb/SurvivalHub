package com.survivalhub.service;

import com.survivalhub.model.Member;
import com.survivalhub.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getMembersByWorldId(Long worldId) {
        return memberRepository.findByWorldId(worldId);
    }

    public Optional<Member> getMemberById(Long worldId, Long memberId) {
        return memberRepository.findByWorldIdAndId(worldId, memberId);
    }

    public Member createMember(Long worldId, Member member) {
        member.setId(null);
        member.setWorldId(worldId);

        return memberRepository.save(member);
    }

    public Optional<Member> updateMember(Long worldId, Long memberId, Member updatedMember) {
        Optional<Member> memberOptional = getMemberById(worldId, memberId);

        if (memberOptional.isEmpty()) {
            return Optional.empty();
        }

        Member member = memberOptional.get();
        member.setName(updatedMember.getName());
        member.setRole(updatedMember.getRole());

        return Optional.of(memberRepository.save(member));
    }

    public boolean deleteMember(Long worldId, Long memberId) {
        Optional<Member> memberOptional = getMemberById(worldId, memberId);

        if (memberOptional.isEmpty()) {
            return false;
        }

        memberRepository.delete(memberOptional.get());

        return true;
    }

    @Transactional
    public void deleteMembersByWorldId(Long worldId) {
        memberRepository.deleteByWorldId(worldId);
    }
}

package com.survivalhub.repository;

import com.survivalhub.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByWorldId(Long worldId);

    Optional<Member> findByWorldIdAndId(Long worldId, Long id);

    void deleteByWorldId(Long worldId);
}

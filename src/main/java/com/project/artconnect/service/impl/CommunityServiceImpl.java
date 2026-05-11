package com.project.artconnect.service.impl;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.service.CommunityService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommunityServiceImpl implements CommunityService {
    private final JdbcCommunityMemberDao communityMemberDao = new JdbcCommunityMemberDao();

    @Override
    public List<CommunityMember> getAllMembers() {
        return communityMemberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        for (CommunityMember member : communityMemberDao.findAll()) {
            if (member.getName().equalsIgnoreCase(name)) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        return Collections.emptyList();
    }
}

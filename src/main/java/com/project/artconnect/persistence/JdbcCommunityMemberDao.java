package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findById(Long id) {
        throw new UnsupportedOperationException("Recherche par id non implementee pour le moment.");
    }

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> members = new ArrayList<>();
        String sql = "SELECT * FROM CommunityMember";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                members.add(creerCommunityMember(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des membres", e);
        }

        return members;
    }

    private CommunityMember creerCommunityMember(ResultSet resultSet) throws SQLException {
        CommunityMember member = new CommunityMember();
        member.setEmail(resultSet.getString("email"));
        member.setName(resultSet.getString("name") + " " + resultSet.getString("surname"));

        int birthYear = resultSet.getInt("birthYear");
        if (resultSet.wasNull()) {
            member.setBirthYear(null);
        } else {
            member.setBirthYear(birthYear);
        }

        member.setPhone(resultSet.getString("phone"));
        member.setCity(resultSet.getString("city"));
        member.setMembershipType(resultSet.getString("membershipType"));
        return member;
    }
}

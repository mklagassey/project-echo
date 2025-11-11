package com.projectecho.desktop;

import com.projectecho.core.Keyword;
import com.projectecho.core.KeywordDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteKeywordDao implements KeywordDao {

    @Override
    public void save(Keyword keyword) {
        String sql = "INSERT INTO keywords(phrase) VALUES(?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, keyword.getPhrase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Keyword keyword) {
        String sql = "DELETE FROM keywords WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, keyword.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Keyword> findAll() {
        String sql = "SELECT id, phrase FROM keywords";
        List<Keyword> keywords = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Keyword keyword = new Keyword();
                keyword.setId(rs.getLong("id"));
                keyword.setPhrase(rs.getString("phrase"));
                keywords.add(keyword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keywords;
    }
}
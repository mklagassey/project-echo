package com.projectecho.desktop;

import com.projectecho.core.Mention;
import com.projectecho.core.MentionDao;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteMentionDao implements MentionDao {

    @Override
    public void save(Mention mention) {
        String sql = "INSERT INTO mentions(content, source, url, foundAt, authoredAt, sentiment) VALUES(?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mention.getContent());
            pstmt.setString(2, mention.getSource());
            pstmt.setString(3, mention.getUrl());
            pstmt.setString(4, mention.getFoundAt().toString());
            pstmt.setString(5, mention.getAuthoredAt().toString());
            pstmt.setString(6, mention.getSentiment() != null ? mention.getSentiment().name() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsByUrl(String url) {
        String sql = "SELECT 1 FROM mentions WHERE url = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, url);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Mention> findAll() {
        String sql = "SELECT id, content, source, url, foundAt, authoredAt, sentiment FROM mentions ORDER BY authoredAt DESC";
        List<Mention> mentions = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Mention mention = new Mention();
                mention.setId(rs.getLong("id"));
                mention.setContent(rs.getString("content"));
                mention.setSource(rs.getString("source"));
                mention.setUrl(rs.getString("url"));
                mention.setFoundAt(Instant.parse(rs.getString("foundAt")));
                
                String authoredAtStr = rs.getString("authoredAt");
                if (authoredAtStr != null) {
                    mention.setAuthoredAt(Instant.parse(authoredAtStr));
                }

                String sentimentStr = rs.getString("sentiment");
                if (sentimentStr != null) {
                    mention.setSentiment(Mention.Sentiment.valueOf(sentimentStr));
                }
                mentions.add(mention);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mentions;
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM mentions";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
package com.portfolio.job_tracker_service.repository.impl;

import com.portfolio.job_tracker_service.model.entity.UserCvEntity;
import com.portfolio.job_tracker_service.repository.CvRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CvRepositoryImpl implements CvRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void save(UserCvEntity entity) {
        jdbc.update("""
                INSERT INTO user_cv (user_id, filename, pdf_data, extracted_text, uploaded_at)
                VALUES (:userId, :filename, :pdfData, :extractedText, NOW())
                ON CONFLICT (user_id) DO UPDATE SET
                    filename = EXCLUDED.filename,
                    pdf_data = EXCLUDED.pdf_data,
                    extracted_text = EXCLUDED.extracted_text,
                    uploaded_at = NOW()
                """,
                new MapSqlParameterSource()
                        .addValue("userId", entity.userId())
                        .addValue("filename", entity.filename())
                        .addValue("pdfData", entity.pdfData())
                        .addValue("extractedText", entity.extractedText()));
        log.info("CV saved for userId={}", entity.userId());
    }

    @Override
    public Optional<UserCvEntity> findByUserId(UUID userId) {
        try {
            UserCvEntity entity = jdbc.queryForObject(
                    "SELECT user_id, filename, pdf_data, extracted_text, uploaded_at FROM user_cv WHERE user_id = :userId",
                    Map.of("userId", userId),
                    (rs, row) -> new UserCvEntity(
                            (UUID) rs.getObject("user_id"),
                            rs.getString("filename"),
                            rs.getBytes("pdf_data"),
                            rs.getString("extracted_text"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime()
                    ));
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jdbc.update("DELETE FROM user_cv WHERE user_id = :userId", Map.of("userId", userId));
        log.info("CV deleted for userId={}", userId);
    }
}

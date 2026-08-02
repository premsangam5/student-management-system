package com.example.studentmanagement.repository;

import com.example.studentmanagement.model.Student;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer built directly on JdbcTemplate.
 * All queries use '?' placeholders (PreparedStatement) rather than string
 * concatenation, which is the primary defense against SQL injection.
 */
@Repository
public class StudentRepositoryImpl implements StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_ALL =
            "SELECT id, first_name, last_name, email, age, department FROM students ORDER BY id";

    private static final String SELECT_BY_ID =
            "SELECT id, first_name, last_name, email, age, department FROM students WHERE id = ?";

    private static final String INSERT =
            "INSERT INTO students (first_name, last_name, email, age, department) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE students SET first_name = ?, last_name = ?, email = ?, age = ?, department = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM students WHERE id = ?";

    private static final String EXISTS_BY_EMAIL =
            "SELECT COUNT(*) FROM students WHERE email = ?";

    @Override
    public List<Student> findAll() {
        return jdbcTemplate.query(SELECT_ALL, this::mapRow);
    }

    @Override
    public Optional<Student> findById(Long id) {
        try {
            Student student = jdbcTemplate.queryForObject(SELECT_BY_ID, this::mapRow, id);
            return Optional.ofNullable(student);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Student save(Student student) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getEmail());
            ps.setInt(4, student.getAge());
            ps.setString(5, student.getDepartment());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        student.setId(generatedId);
        return student;
    }

    @Override
    public Student update(Long id, Student student) {
        jdbcTemplate.update(UPDATE,
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getAge(),
                student.getDepartment(),
                id);
        student.setId(id);
        return student;
    }

    @Override
    public boolean deleteById(Long id) {
        int rowsAffected = jdbcTemplate.update(DELETE, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_BY_EMAIL, Integer.class, email);
        return count != null && count > 0;
    }

    private Student mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Student(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getInt("age"),
                rs.getString("department")
        );
    }
}

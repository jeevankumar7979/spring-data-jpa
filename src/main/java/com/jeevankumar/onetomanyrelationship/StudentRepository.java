package com.jeevankumar.onetomanyrelationship;

import com.jeevankumar.onetomanyrelationship.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query(value = """
            SELECT DISTINCT s.*
            FROM student s
            JOIN address a ON a.student_id = s.id
            WHERE s.name = :name
              AND a.city_name = :cityName
            """, nativeQuery = true)
    List<Student> findStudentNameAddress(
            @Param("name") String name,
            @Param("cityName") String cityName);
}

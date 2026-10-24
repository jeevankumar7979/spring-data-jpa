package com.jeevankumar.onetomanyrelationship;

import com.jeevankumar.onetomanyrelationship.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Transactional
    public Student upsertStudent(Student student) {
        if (student.getId() == null) {
            return studentRepository.save(student);
        }

        return studentRepository.findById(student.getId())
                .map(existingStudent -> {
                    existingStudent.setName(student.getName());
                    return existingStudent;
                })
                .orElseGet(() -> {
                    student.setId(null);
                    return studentRepository.save(student);
                });
    }
}

package com.jeevankumar.onetomanyrelationship;

import com.jeevankumar.onetomanyrelationship.entity.Address;
import com.jeevankumar.onetomanyrelationship.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FetchTypeDemo {
    private final StudentRepository studentRepository;
    private final EntityManager entityManager;

    public FetchTypeDemo(StudentRepository studentRepository, EntityManager entityManager) {
        this.studentRepository = studentRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void showFetchBehavior() {
        Student student = new Student("Fetch type demo");
        student.setAddresses(List.of(
                new Address(student, "Bangalore"),
                new Address(student, "Chennai")
        ));
        studentRepository.saveAndFlush(student);

        Long studentId = student.getId();
        entityManager.clear();

        Student loadedStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Demo student was not saved"));
        System.out.println("Student loaded; addresses have not been accessed yet.");
        System.out.println("Address count: " + loadedStudent.getAddresses().size());

//        studentRepository.delete(loadedStudent);
    }
}

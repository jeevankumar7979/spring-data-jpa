package com.jeevankumar.onetomanyrelationship;

import com.jeevankumar.onetomanyrelationship.entity.Student;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1/students")
public class OneToManyRelationshipApplication implements CommandLineRunner {
    private final FetchTypeDemo fetchTypeDemo;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    public OneToManyRelationshipApplication(FetchTypeDemo fetchTypeDemo, StudentRepository studentRepository, StudentService studentService) {
        this.fetchTypeDemo = fetchTypeDemo;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
    }

    public static void main(String[] args) {
        SpringApplication.run(OneToManyRelationshipApplication.class, args);
    }

    @Override
    public void run(String... args) {
        fetchTypeDemo.showFetchBehavior();
    }

    @GetMapping(params = {"name", "cityName"})
    public List<Student> findStudentsByNameAndCity(
            @RequestParam String name,
            @RequestParam String cityName) {
        return studentRepository.findStudentNameAddress(name, cityName);
    }

    @PostMapping("/upsert")
    public Student upsertStudent(@RequestBody Student student) {
        return studentService.upsertStudent(student);
    }

    @GetMapping
    public Page<Student> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
            ) {
      Sort sort =  ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return studentService.getAllStudents(pageRequest);
    }


}

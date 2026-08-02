package com.example.studentmanagement.service;

import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    public Student createStudent(Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("A student with this email already exists");
        }
        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Long id, Student student) {
        // Ensure the student exists before attempting an update (404 vs silent no-op)
        getStudentById(id);
        return studentRepository.update(id, student);
    }

    @Override
    public void deleteStudent(Long id) {
        boolean deleted = studentRepository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
    }
}

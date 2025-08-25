package com.university.result_management.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.university.result_management.models.Course;
import com.university.result_management.models.CourseEnrollment;
import com.university.result_management.models.Student;
import com.university.result_management.repositories.CourseEnrollmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepo;
    private final StudentService studentService;
    private final CourseService courseService;

    // (1) Enroll a student into a course
    public CourseEnrollment enrollStudent(Long studentId, Long courseId) {
        CourseEnrollment ce = new CourseEnrollment();
        ce.setStudent(Student.builder().id(studentId).build());
        ce.setCourse(Course.builder().id(courseId).build());
        ce.setStatus("Pending");
        return enrollmentRepo.save(ce);
    }
    public boolean isEnrolledAndPassed(Long studentId, Long courseId) {
        return enrollmentRepo.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, "Pass");
    }

    public Optional<CourseEnrollment> getLatestFailedEnrollment(Long studentId, Long courseId, String status) {
        return enrollmentRepo.findTopByStudentIdAndCourseIdAndStatusOrderByAttemptNumberDesc(studentId, courseId, status="Fail");
    }

    // (2) Get all enrollments for a specific course
    public List<CourseEnrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepo.findByCourseId(courseId);
    }

    // (3) Update marks and grade for a specific enrollment
    public void updateMarks(Long enrollmentId, Double marksObtained) {
        CourseEnrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + enrollmentId));
        
        enrollment.setMarksObtained(marksObtained);
        enrollment.setGrade(calculateGrade(marksObtained));
        enrollment.setStatus("Completed");

        enrollmentRepo.save(enrollment);
    }

    // (4) Update marks for multiple students at once (batch update)
    public void updateMarksBulk(List<Long> enrollmentIds, List<Double> marksList) {
        for (int i = 0; i < enrollmentIds.size(); i++) {
            updateMarks(enrollmentIds.get(i), marksList.get(i));
        }
    }

    // Helper method to auto-calculate Grade based on marks
    private String calculateGrade(Double marks) {
        if (marks == null) return "F";
        if (marks >= 90) return "A+";
        else if (marks >= 80) return "A";
        else if (marks >= 70) return "B";
        else if (marks >= 60) return "C";
        else if (marks >= 50) return "D";
        else return "F";
    }
    public List<CourseEnrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepo.findByStudentId(studentId);
    }
    public List<CourseEnrollment> getEnrollmentsByStudentAndSemester(Long studentId, int semester) {
        // You might need to modify this method to find enrollments based on both student ID and course semester
        List<CourseEnrollment> enrollments = enrollmentRepo.findByStudentId(studentId);
        return enrollments.stream()
                .filter(enrollment -> enrollment.getStudent().getSemester() == semester)
                .collect(Collectors.toList());
    }
    public Optional<CourseEnrollment> getEnrollmentByStudentAndCourse(Long studentId, Long courseId) {
        return enrollmentRepo.findByStudentIdAndCourseId(studentId, courseId);
    }

    public void deleteEnrollment(Long enrollmentId) {
        enrollmentRepo.deleteById(enrollmentId);
    }
    public void enrollStudentWithAttempt(Long studentId, Long courseId, int attemptNumber) {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudent(studentService.findById(studentId));
        enrollment.setCourse(courseService.getOneCourse(courseId));
        enrollment.setAttemptNumber(attemptNumber);
       enrollmentRepo.save(enrollment);
    }
    public boolean hasIncompleteEnrollment(Long studentId, Long courseId) {
        List<CourseEnrollment> enrollments = enrollmentRepo.findByStudent_IdAndCourse_Id(studentId, courseId);
        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.getStatus() == null) {
                return true;
            }
        }
        return false;
    }
    public boolean isStudentAlreadyEnrolled(Long studentId, Long courseId) {
        // Use the repository method to check if the student is already enrolled
        return enrollmentRepo.existsByStudentIdAndCourseId(studentId, courseId);
    }
    public boolean hasFailedCourse(Long studentId, Long courseId) {
        // Check if the student has a failed status for the given course
        return enrollmentRepo.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, "Fail");
        
       
    }




}

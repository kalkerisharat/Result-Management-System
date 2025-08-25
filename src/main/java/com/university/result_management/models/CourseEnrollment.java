package com.university.result_management.models;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data

@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
   

    private Double marksObtained;

    private String grade;

    private String status;
    
    private int attemptNumber = 1; // Default attempt


    
}

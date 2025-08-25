package com.university.result_management.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.university.result_management.models.CourseEnrollment;
import com.university.result_management.models.Result;
import com.university.result_management.repositories.CourseEnrollmentRepository;
import com.university.result_management.repositories.ResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarksAssignmentService {

    private final CourseEnrollmentRepository courseEnrollmentRepository;
 
    private final ResultService resultService;
    private final ResultRepository resultRepository;

    // Assign marks for multiple students
    @Transactional
    public String assignMarksBulk(List<Long> enrollmentIds, List<Double> marks, Model model) {
        for (int i = 0; i < enrollmentIds.size(); i++) {
            Long enrollmentId = enrollmentIds.get(i);
            Double newMark = marks.get(i);

            CourseEnrollment enrollment = courseEnrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

            Double previousMark = enrollment.getMarksObtained();

            // If marks are already assigned and unchanged, return message
            if (previousMark != null && previousMark.equals(newMark)) {
                model.addAttribute("title", "Marks Already Assigned");
                model.addAttribute("message", "Marks for this enrollment are already assigned and unchanged.");
                model.addAttribute("backUrl", "/instructor/course/" + enrollment.getCourse().getId() + "/students");
                return "html/common/message";
            }

            // If marks are different, delete the previous result entry
            resultRepository.deleteByEnrollmentId(enrollmentId);

            // Assign new marks
            enrollment.setMarksObtained(newMark);
            enrollment.setGrade(resultService.calculateGrade(newMark));
            enrollment.setStatus(resultService.calculateStatus(newMark));
            courseEnrollmentRepository.save(enrollment);

            // Create and save new result entry
            Result result = new Result();
            result.setStudent(enrollment.getStudent());
            result.setCourse(enrollment.getCourse());
            result.setSemester(enrollment.getStudent().getSemester());
            result.setRemarks(resultService.calculateRemarks(newMark));

            resultService.saveResultWithCGPA(result, enrollment);
        }

        // If successful, show success message
        model.addAttribute("title", "Success");
        model.addAttribute("message", "Marks updated successfully.");
        model.addAttribute("backUrl", "/instructor/course/" + enrollmentIds.get(0) + "/students");
        return "html/common/message";
    }






   

   
}

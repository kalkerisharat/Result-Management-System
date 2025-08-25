package com.university.result_management.services;






import java.util.List;

import org.springframework.stereotype.Service;

import com.university.result_management.models.CourseEnrollment;
import com.university.result_management.models.Result;
import com.university.result_management.repositories.CourseEnrollmentRepository;
import com.university.result_management.repositories.ResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResultService {
	private final ResultRepository resultRepository;
	  private final CourseEnrollmentRepository courseEnrollmentRepo;
	
	
	public List<Result> getResultsByStudentIdAndSemester(Long studentId, int semester) {
	    return resultRepository.findByStudentIdAndSemester(studentId, semester);
	}
	 public List<Result> getResultsByStudentId(Long studentId) {
	        return resultRepository.findByStudentId(studentId);
	    }
	 public double calculateGPA(double marks) {
	        if (marks >= 90) return 9.0;
	        if (marks >= 80) return 8.5;
	        if (marks >= 70) return 7.0;
	        if (marks >= 60) return 2.5;
	        if (marks >= 50) return 2.0;
	        return 1.0; // Below 50 is considered fail
	    }

	 public void saveResultWithCGPA(Result result, CourseEnrollment courseEnrollment) {
		    double marks = courseEnrollment.getMarksObtained();
		    result.setMarks(marks);
		    double gpa = calculateGPA(marks);
		    result.setGPA(gpa);

		    // Get the student's semester from the student entity
		    int semester = courseEnrollment.getStudent().getSemester(); // Access the semester from the Student entity
		 // Calculate total credits from passed courses
		    List<CourseEnrollment> allEnrollments = courseEnrollmentRepo.findByStudentId(courseEnrollment.getStudent().getId());
		    int totalCredits = allEnrollments.stream()
		        .filter(e -> "Pass".equalsIgnoreCase(e.getStatus()))
		        .mapToInt(e -> e.getCourse().getCredits())
		        .sum();
		    result.setTotalCredits(totalCredits);

		    // Calculate CGPA based on all previous results
		    List<Result> allResults = resultRepository.findByStudentId(result.getStudent().getId());
		    double cgpa = calculateCGPA(allResults);
		    result.setCgpa(cgpa);

		    result.setSemester(semester); // Set the semester for the result
		    result.setCourseEnrollment(courseEnrollment); // Link to the course enrollment
		    resultRepository.save(result); // Save the result
		}

	    // Method to calculate CGPA from all results of a student
	 public double calculateCGPA(List<Result> results) {
		    if (results == null || results.isEmpty()) {
		        return 0.0;  // or any default valid value
		    }

		    double totalGPA = 0;
		    for (Result result : results) {
		        totalGPA += result.getGPA();
		    }
		    return totalGPA / results.size();
		}



	    // Remarks based on marks
	    public String calculateRemarks(Double marks) {
	        if (marks >= 90) return "Excellent";
	        if (marks >= 80) return "Very Good";
	        if (marks >= 70) return "Good";
	        if (marks >= 60) return "Satisfactory";
	        if (marks >= 50) return "Pass";
	        return "Fail"; // Below 50 marks
	    }

	    // Grade calculation based on marks
	    public String calculateGrade(Double marks) {
	        if (marks >= 90) return "A+";
	        if (marks >= 80) return "A";
	        if (marks >= 70) return "B+";
	        if (marks >= 60) return "B";
	        if (marks >= 50) return "C+";
	        return "F"; // Below 50 is Fail
	    }

	    // Status based on marks
	    public String calculateStatus(Double marks) {
	        return marks >= 50 ? "Pass" : "Fail"; // Simple pass/fail status
	    }
	    public Integer getMaxTotalCredits(Long studentId) {
	        Integer maxCredits = resultRepository.findMaxTotalCreditsByStudentId(studentId);
	        return maxCredits != null ? maxCredits : 0;
	    }




   
}

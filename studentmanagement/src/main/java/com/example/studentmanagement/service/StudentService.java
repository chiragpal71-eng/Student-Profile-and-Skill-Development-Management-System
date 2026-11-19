package com.example.studentmanagement.service;

import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // Prevent duplicate student IDs and validate scores between 0 and 100
    public Student addStudent(Student student) {
        if (studentRepository.existsById(student.getStudentId())) {
            throw new IllegalArgumentException("Duplicate Student ID: " + student.getStudentId() + " already exists.");
        }
        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Duplicate Email: " + student.getEmail() + " already exists.");
        }
        validateScores(student.getProgrammingScore(), student.getMathematicsScore());
        return studentRepository.save(student);
    }

    private void validateScores(double progScore, double mathScore) {
        if (progScore < 0 || progScore > 100 || mathScore < 0 || mathScore > 100) {
            throw new IllegalArgumentException("Scores must be strictly between 0 and 100.");
        }
    }

    // 1. Calculate each student's average score
    public double calculateAverageScore(Student student) {
        return (student.getProgrammingScore() + student.getMathematicsScore()) / 2.0;
    }

    // 2. Assign a performance category based on the average
    public String getPerformanceCategory(Student student) {
        double avg = calculateAverageScore(student);
        if (avg >= 85.0) {
            return "Excellent";
        } else if (avg >= 70.0) {
            return "Good";
        } else if (avg >= 50.0) {
            return "Satisfactory";
        } else {
            return "Needs Improvement";
        }
    }

    // 3. Identify the stronger subject for each student
    public String getStrongerSubject(Student student) {
        if (student.getProgrammingScore() > student.getMathematicsScore()) {
            return "Programming";
        } else if (student.getMathematicsScore() > student.getProgrammingScore()) {
            return "Mathematics";
        } else {
            return "Equal in Both";
        }
    }

    // 4. Search for students by ID, email, or course
    public Student searchById(String studentId) {
        Optional opt = studentRepository.findById(studentId);
        if (opt.isPresent()) {
            return (Student) opt.get();
        }
        return null;
    }

    public Student searchByEmail(String email) {
        Optional opt = studentRepository.findByEmail(email);
        if (opt.isPresent()) {
            return (Student) opt.get();
        }
        return null;
    }

    public Student[] searchByCourse(String courseName) {
        List list = studentRepository.findByCourseNameIgnoreCase(courseName);
        Student[] arr = new Student[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = (Student) list.get(i);
        }
        return arr;
    }

    // 5. Generate a merit list sorted by average score in descending order
    public MeritListEntry[] generateMeritList() {
        Student[] students = getAllStudents();

        // Traditional Bubble Sort descending by average score
        for (int i = 0; i < students.length - 1; i++) {
            for (int j = 0; j < students.length - i - 1; j++) {
                if (calculateAverageScore(students[j]) < calculateAverageScore(students[j + 1])) {
                    Student temp = students[j];
                    students[j] = students[j + 1];
                    students[j + 1] = temp;
                }
            }
        }

        MeritListEntry[] meritList = new MeritListEntry[students.length];
        for (int i = 0; i < students.length; i++) {
            Student s = students[i];
            meritList[i] = new MeritListEntry(
                    s.getStudentId(),
                    s.getStudentName(),
                    s.getCourseName(),
                    s.getProgrammingScore(),
                    s.getMathematicsScore(),
                    calculateAverageScore(s),
                    getPerformanceCategory(s),
                    getStrongerSubject(s)
            );
        }
        return meritList;
    }

    // 6. Identify students who have the same technical skills
    public SkillGroup[] groupStudentsBySkill() {
        Student[] students = getAllStudents();
        List uniqueSkills = new ArrayList();

        for (int i = 0; i < students.length; i++) {
            List skills = students[i].getTechnicalSkills();
            for (int j = 0; j < skills.size(); j++) {
                String cleanSkill = skills.get(j).toString().trim().toUpperCase();
                if (!uniqueSkills.contains(cleanSkill)) {
                    uniqueSkills.add(cleanSkill);
                }
            }
        }

        List result = new ArrayList();
        for (int i = 0; i < uniqueSkills.size(); i++) {
            String skill = uniqueSkills.get(i).toString();
            List matchedStudents = new ArrayList();

            for (int j = 0; j < students.length; j++) {
                Student s = students[j];
                List sSkills = s.getTechnicalSkills();
                for (int k = 0; k < sSkills.size(); k++) {
                    if (sSkills.get(k).toString().trim().equalsIgnoreCase(skill)) {
                        matchedStudents.add(s.getStudentName() + " (" + s.getStudentId() + ")");
                        break;
                    }
                }
            }

            if (matchedStudents.size() > 1) {
                String[] studentArr = new String[matchedStudents.size()];
                for (int m = 0; m < matchedStudents.size(); m++) {
                    studentArr[m] = matchedStudents.get(m).toString();
                }
                result.add(new SkillGroup(skill, studentArr));
            }
        }

        SkillGroup[] finalGroups = new SkillGroup[result.size()];
        for (int i = 0; i < result.size(); i++) {
            finalGroups[i] = (SkillGroup) result.get(i);
        }
        return finalGroups;
    }

    // 7. Display students who need improvement in programming (< 50) but have mathematics >= 75
    public Student[] findWeakProgrammingStrongMathStudents() {
        Student[] allStudents = getAllStudents();
        List filtered = new ArrayList();

        for (int i = 0; i < allStudents.length; i++) {
            Student s = allStudents[i];
            if (s.getProgrammingScore() < 50.0 && s.getMathematicsScore() >= 75.0) {
                filtered.add(s);
            }
        }

        Student[] result = new Student[filtered.size()];
        for (int i = 0; i < filtered.size(); i++) {
            result[i] = (Student) filtered.get(i);
        }
        return result;
    }

    // Retrieve all students
    public Student[] getAllStudents() {
        List list = studentRepository.findAll();
        Student[] arr = new Student[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = (Student) list.get(i);
        }
        return arr;
    }

    // Update student scores and skills
    public Student updateScoresAndSkills(String studentId, double progScore, double mathScore, String[] skills) {
        Student existing = searchById(studentId);
        if (existing == null) {
            throw new NoSuchElementException("Student not found with ID: " + studentId);
        }
        validateScores(progScore, mathScore);
        existing.setProgrammingScore(progScore);
        existing.setMathematicsScore(mathScore);
        if (skills != null) {
            List skillList = new ArrayList();
            for (int i = 0; i < skills.length; i++) {
                skillList.add(skills[i]);
            }
            existing.setTechnicalSkills(skillList);
        }
        return (Student) studentRepository.save(existing);
    }

    // Delete a student record
    public void deleteStudent(String studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new NoSuchElementException("Student not found with ID: " + studentId);
        }
        studentRepository.deleteById(studentId);
    }

    // DTO class for Merit List
    public static class MeritListEntry {
        private String studentId;
        private String studentName;
        private String courseName;
        private double programmingScore;
        private double mathematicsScore;
        private double averageScore;
        private String category;
        private String strongerSubject;

        public MeritListEntry(String studentId, String studentName, String courseName,
                              double programmingScore, double mathematicsScore,
                              double averageScore, String category, String strongerSubject) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseName = courseName;
            this.programmingScore = programmingScore;
            this.mathematicsScore = mathematicsScore;
            this.averageScore = averageScore;
            this.category = category;
            this.strongerSubject = strongerSubject;
        }

        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getCourseName() { return courseName; }
        public double getProgrammingScore() { return programmingScore; }
        public double getMathematicsScore() { return mathematicsScore; }
        public double getAverageScore() { return averageScore; }
        public String getCategory() { return category; }
        public String getStrongerSubject() { return strongerSubject; }
    }

    // DTO class for Shared Skills
    public static class SkillGroup {
        private String skillName;
        private String[] students;

        public SkillGroup(String skillName, String[] students) {
            this.skillName = skillName;
            this.students = students;
        }

        public String getSkillName() { return skillName; }
        public String[] getStudents() { return students; }
    }
}
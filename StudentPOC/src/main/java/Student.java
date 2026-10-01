import java.util.LinkedHashMap;
import java.util.Map;

public class Student {

    public static void main(String[] args) {
        Map<Integer, Map<String, Integer>> studentMap = new LinkedHashMap<>();

        Map<String, Integer> student101 = new LinkedHashMap<>();
        student101.put("Maths", 80);
        student101.put("Physics", 90);
        student101.put("Chemistry", 85);
        studentMap.put(101, student101);

        Map<String, Integer> student102 = new LinkedHashMap<>();
        student102.put("Maths", 75);
        student102.put("Physics", 88);
        student102.put("Chemistry", 92);
        studentMap.put(102, student102);

        for (Map.Entry<Integer, Map<String, Integer>> studentEntry : studentMap.entrySet()) {
            int studentId = studentEntry.getKey();
            Map<String, Integer> marksBySubject = studentEntry.getValue();
            int totalMarks = 0;

            System.out.println("Student Id : " + studentId);
            System.out.println();

            for (Map.Entry<String, Integer> subjectEntry : marksBySubject.entrySet()) {
                String subject = subjectEntry.getKey();
                int marks = subjectEntry.getValue();
                totalMarks += marks;
                System.out.println(subject + " : " + marks);
            }

            System.out.println("Total Marks : " + totalMarks);
            System.out.println();
        }
    }
}

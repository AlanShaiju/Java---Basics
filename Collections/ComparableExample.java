import java.util.*;

class Student implements Comparable<Student> {
    int id;
    String name;

    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Natural ordering: sort by id
    @Override
    public int compareTo(Student s) {
        return this.id - s.id;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

public class ComparableExample {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student(3, "Ravi"));
        students.add(new Student(1, "Anu"));
        students.add(new Student(2, "Kiran"));

        Collections.sort(students); // uses compareTo
        System.out.println(students);
    }
}

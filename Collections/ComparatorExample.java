import java.util.*;

class Student {
    int id;
    String name;

    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

// Comparator for sorting by name
class NameComparator implements Comparator<Student> {
    @Override
    public int compare(Student s1, Student s2) {
        return s1.name.compareTo(s2.name);
    }
}

// Comparator for sorting by id (descending)
class IdDescComparator implements Comparator<Student> {
    @Override
    public int compare(Student s1, Student s2) {
        return s2.id - s1.id;
    }
}

public class ComparatorExample {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student(3, "Ravi"));
        students.add(new Student(1, "Anu"));
        students.add(new Student(2, "Kiran"));

        Collections.sort(students, new NameComparator());
        System.out.println("Sorted by name: " + students);

        Collections.sort(students, new IdDescComparator());
        System.out.println("Sorted by id (desc): " + students);
    }
}

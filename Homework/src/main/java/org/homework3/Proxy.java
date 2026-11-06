package org.homework3;

// Общий интерфейс
interface StudentProfile {
    void showProfile();
}

// Реальный объект
class RealStudentProfile implements StudentProfile {
    private final Student student;

    public RealStudentProfile(Student student) {
        this.student = student;
    }

    @Override
    public void showProfile() {
        System.out.println("[Proxy] Доступ разрешен. Профиль студента: " + student.getName() + ", Факультет: " + student.getFaculty() + ", Курс: " + student.getYearOfStudy());
    }
}

// Прокси
class StudentProfileProxy implements StudentProfile {
    private StudentProfile realProfile;
    private final Student student;
    private final boolean hasFines;

    public StudentProfileProxy(Student student, boolean hasFines) {
        this.student = student;
        this.hasFines = hasFines;
    }

    @Override
    public void showProfile() {
        if (hasFines) {
            System.out.println("[Proxy] Доступ запрещен! У студента " + student.getName() + " есть неоплаченные штрафы.");
        } else {
            if (realProfile == null) {
                // Создаем реализацию, но храним её как интерфейс
                realProfile = new RealStudentProfile(student);
            }
            realProfile.showProfile();
        }
    }
}
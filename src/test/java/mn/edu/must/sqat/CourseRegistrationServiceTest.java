package mn.edu.must.sqat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent() {
        return new Student("S001", "s001@example.com", true, List.of("CS100"));
    }

    // A1 — Идэвхтэй оюутан амжилттай бүртгүүлэх
    @Test
    void register_whenStudentIsActive_returnsOkAndSendsEmail() {
        when(repository.findStudent("S001")).thenReturn(activeStudent());
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(List.of("CS100"));

        String result = service.register("S001", "CS101");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S001", "CS101");
        verify(emailSender).send(eq("s001@example.com"), anyString(), contains("CS101"));
    }

    // A2 — Байхгүй оюутны бүртгэл буцаагдах
    @Test
    void register_whenStudentMissing_returnsNoStudentWithoutSaving() {
        when(repository.findStudent("S001")).thenReturn(null);

        String result = service.register("S001", "CS101");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A3 — Идэвхгүй оюутны бүртгэл буцаагдах
    @Test
    void register_whenStudentInactive_returnsInactiveWithoutEmail() {
        Student inactive = new Student("S001", "s001@example.com", false, List.of());
        when(repository.findStudent("S001")).thenReturn(inactive);

        String result = service.register("S001", "CS101");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4 — Өмнөх холбоо хичээл дутуу үед хадгалахгүй
    @Test
    void register_whenPrerequisitesMissing_returnsErrorWithoutSaving() {
        Student student = new Student("S001", "s001@example.com", true, List.of());
        when(repository.findStudent("S001")).thenReturn(student);
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(List.of("CS100"));

        String result = service.register("S001", "CS101");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A5 — И-мэйл илгээхэд алдаа гарсан ч бүртгэл хадгалагдах
    @Test
    void register_whenEmailThrows_returnsOkAndSavesRegistration() {
        when(repository.findStudent("S001")).thenReturn(activeStudent());
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(List.of());
        doThrow(new RuntimeException("SMTP error"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register("S001", "CS101");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S001", "CS101");
    }

    // A6 — Хоёр аргументийн null-ийг тус тусад нь шалгах
    @Test
    void register_whenEitherInputNull_throwsWithoutInteracting() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, "CS101"));
        assertThrows(IllegalArgumentException.class,
                () -> service.register("S001", null));
        verifyNoInteractions(repository, emailSender);
    }

    // A7 — Илгээсэн и-мэйлийн body-г ArgumentCaptor-оор шалгах
    @Test
    void register_whenSuccessful_emailBodyContainsCourseId() {
        when(repository.findStudent("S001")).thenReturn(activeStudent());
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(List.of());

        service.register("S001", "CS101");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("s001@example.com"), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("CS101"));
    }

    // A8 — Гурван төрлийн алдааг нэг параметржүүлсэн тестээр шалгах
    @ParameterizedTest
    @CsvSource({
            "missing, ERROR_NO_STUDENT",
            "inactive, ERROR_INACTIVE_STUDENT",
            "no-course, ERROR_NO_COURSE"
    })
    void register_whenInvalid_returnsExpectedError(String scenario, String expected) {
        Student student = switch (scenario) {
            case "missing" -> null;
            case "inactive" -> new Student("S001", "s001@example.com", false, List.of());
            case "no-course" -> activeStudent();
            default -> throw new IllegalArgumentException(scenario);
        };
        when(repository.findStudent("S001")).thenReturn(student);
        lenient().when(repository.courseExists("CS101"))
                .thenReturn(!scenario.equals("no-course"));

        String result = service.register("S001", "CS101");

        assertEquals(expected, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}

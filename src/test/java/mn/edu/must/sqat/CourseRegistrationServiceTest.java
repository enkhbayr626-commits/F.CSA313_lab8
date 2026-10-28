package mn.edu.must.sqat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {
    private static final String STUDENT_ID = "B232270153";
    private static final String EMAIL = "enkhbayar@example.com";
    private static final String COURSE_ID = "F.CSA313";
    private static final String COMPLETED_COURSE = "F.CSA101";

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    private CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    // A1
    @Test
    void register_activeStudent_returnsOkAndSavesAndSendsEmail() {
        stubEligibleRegistration();

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), contains(COURSE_ID));
    }

    // A2
    @Test
    void register_missingStudent_returnsErrorWithoutSavingOrSendingEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3
    @Test
    void register_inactiveStudent_returnsErrorWithoutEmailInteractions() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(false));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4
    @Test
    void register_missingPrerequisites_returnsErrorWithoutSaving() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("F.CSA211"));

        assertEquals("ERROR_PREREQUISITES", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A5
    @Test
    void register_emailSenderThrows_returnsOkAndSavesRegistration() {
        stubEligibleRegistration();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(eq(EMAIL), anyString(), anyString());

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), contains(COURSE_ID));
    }

    // A6: both arguments are checked within one JUnit test.
    @Test
    void register_nullStudentOrCourse_throwsWithoutDependencyInteractions() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(null, COURSE_ID)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(STUDENT_ID, null)));

        verifyNoInteractions(repository, emailSender);
    }

    // A7
    @Test
    void register_successfulRegistration_emailBodyContainsCourseCode() {
        stubEligibleRegistration();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(emailSender).send(eq(EMAIL), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
    }

    // A8
    @ParameterizedTest(name = "[{index}] expected={3}")
    @CsvSource({
            "false, true, true, ERROR_NO_STUDENT",
            "true, false, true, ERROR_INACTIVE_STUDENT",
            "true, true, false, ERROR_NO_COURSE"
    })
    void register_invalidStudentOrCourse_returnsExpectedError(
            boolean studentExists, boolean active, boolean courseExists, String expected) {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(studentExists ? student(active) : null);
        // Earlier student checks skip this shared stub in the first two rows.
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(courseExists);

        assertEquals(expected, service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    private Student student(boolean active) {
        return new Student(STUDENT_ID, EMAIL, active, List.of(COMPLETED_COURSE));
    }

    private void stubEligibleRegistration() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of(COMPLETED_COURSE));
    }
}

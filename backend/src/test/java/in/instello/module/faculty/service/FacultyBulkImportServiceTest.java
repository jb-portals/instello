
package in.instello.module.faculty.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FacultyBulkImportServiceTest {

    private FacultyBulkImportService service;

    @BeforeEach
    void setUp() {
        service = new FacultyBulkImportService();
    }

    @Test
    void shouldAcceptValidCsvHeaders() throws Exception {
        String csv = "First Name,Last Name,Date of Birth,Email,Staff ID,"
                + "Designation,Qualification,Specialization,Joined Date,"
                + "Primary Phone Number\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "faculty.csv",
                "text/csv",
                csv.getBytes()
        );

        assertTrue(service.validateFile(file).isEmpty());
    }

    @Test
    void shouldReportMissingRequiredColumns() throws Exception {
        String csv = "First Name,Last Name,Email\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "faculty.csv",
                "text/csv",
                csv.getBytes()
        );

        var missingColumns = service.validateFile(file);

        assertFalse(missingColumns.isEmpty());
        assertTrue(missingColumns.contains("date of birth"));
        assertTrue(missingColumns.contains("staff id"));
    }

    @Test
    void shouldRejectUnsupportedFileFormat() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "faculty.pdf",
                "application/pdf",
                "sample".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateFile(file)
        );
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "faculty.csv",
                "text/csv",
                new byte[0]
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateFile(file)
        );
    }
}

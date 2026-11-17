package in.instello.module.faculty.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FacultyBulkImportService {

    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "first name",
            "last name",
            "date of birth",
            "email",
            "staff id",
            "designation",
            "qualification",
            "specialization",
            "joined date",
            "primary phone number"
    );

    public List<String> validateFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a file.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("File name is missing.");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        List<String> headers;

        switch (extension) {
            case "csv" -> headers = readCsvHeaders(file);
            case "xls", "xlsx" -> headers = readExcelHeaders(file);
            default -> throw new IllegalArgumentException(
                    "Unsupported file format. Upload CSV, XLS, or XLSX.");
        }

        Set<String> normalizedHeaders = headers.stream()
                .map(this::normalizeHeader)
                .collect(Collectors.toSet());

        return REQUIRED_COLUMNS.stream()
                .filter(column -> !normalizedHeaders.contains(column))
                .sorted()
                .toList();
    }

    private List<String> readCsvHeaders(MultipartFile file) throws IOException {

        try (var reader = new java.io.InputStreamReader(
                     file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {

            var firstRecord = parser.iterator();

            if (!firstRecord.hasNext()) {
                throw new IllegalArgumentException("CSV file is empty.");
            }

            return new ArrayList<>(firstRecord.next().toList());
        }
    }

    private List<String> readExcelHeaders(MultipartFile file) throws IOException {

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Row headerRow = workbook.getSheetAt(0).getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException("Excel header row is missing.");
            }

            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(formatter.formatCellValue(headerRow.getCell(i)));
            }

            return headers;
        }
    }

    private String normalizeHeader(String header) {
        return header == null
                ? ""
                : header.trim().toLowerCase(Locale.ROOT);
    }
}

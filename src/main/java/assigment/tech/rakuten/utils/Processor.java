package assigment.tech.rakuten.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Map.Entry;
import static java.util.stream.Collectors.*;

public class Processor {

    /**
     * Method do:
     * 1) Extraction of breeds provided,
     * 2) processing breeds in `processBreedFromCSVRecord` method
     * 3) Normalize the breed by remove duplicates and sort
     *
     * @param csvRecords A CSV record parsed from a CSV file.
     * @return List<String>
     */
    public static List<String> getAndProcessBreedsToUniqueList(List<CSVRecord> csvRecords) {
        return csvRecords.stream()
                .map(Processor::processBreedFromCSVRecord)
                .distinct()
                .sorted()
                .collect(toList());
    }

    /**
     * Method process breeds:
     * - set it in lower case
     * - delete " "
     * - set breed =  "_unknown_breed_" if breed value is "."
     * (I consider that '.' mean that breed is undefined)
     *
     * @param csvRecord
     * @return String
     */
    private static String processBreedFromCSVRecord(CSVRecord csvRecord) {
        String breed = csvRecord.get("Breed").toLowerCase().replace(" ", "");
        return ".".equals(breed) ? "_unknown_breed_" : breed;
    }

    /**
     * Method that return Map of LicenseType for Unique breed
     *
     * @param csvRecords A CSV record parsed from a CSV file.
     * @return List<String>
     */

    public static Map<String, Map<String, Long>> getLicenseTypeMapByUniqueBreed(List<CSVRecord> csvRecords) {
        return csvRecords.stream()
                .collect(groupingBy(
                        Processor::processBreedFromCSVRecord,
                        groupingBy(r -> r.get("LicenseType"), counting())
                ));
    }


    /**
     * List of csv records processed in  Map<String, Long> where Name is key and value is how many such names are exist.
     * Then Map Entrysets reversely sorted to List and returned first topN elements
     *
     * @param csvRecords
     * @param topN       - size of returned List
     * @return List<Map.Entry < String, Long>>
     */
    public static List<Map.Entry<String, Long>> popularDogNames(List<CSVRecord> csvRecords, int topN) {
        Map<String, Long> nameCounts = csvRecords.stream()
                .collect(groupingBy(r -> r.get("DogName"), counting()));

        return nameCounts.entrySet().stream()
                .sorted(Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(toList());
    }


    /**
     * This method process date in string form into LocalDateTime form according to format
     *
     * @param dateTimeStr
     * @param formatter
     * @return LocalDateTime localDateTime
     */
    private static LocalDateTime parseStringToDateTime(String dateTimeStr, DateTimeFormatter formatter) {

        String[] dateParts = dateTimeStr.split("/"); // split by "/" for add zero if necessary
        dateParts[0] = leadZeroIfOneSymbol(dateParts[0]); //Lead months by zero if month = 1-9
        dateParts[1] = leadZeroIfOneSymbol(dateParts[1]); //Lead days by zero if days = 1-9
        dateTimeStr = format("%s/%s/%s", dateParts[0], dateParts[1], dateParts[2]);//recombine dateTime string

        String[] dateTimeParts = dateTimeStr.split(" "); //split to " " to add 0 for hours
        String[] timePart = dateTimeParts[1].split(":"); //split by ":" to work with hours and minutes
        timePart[0] = leadZeroIfOneSymbol(timePart[0]); //Lead hours by zero if hours = 1-9
        timePart[1] = leadZeroIfOneSymbol(timePart[1]); //Lead minutes by zero if minutes = 1-9

        dateTimeStr = format("%s %s:%s", dateTimeParts[0], timePart[0], timePart[1]);//recombine dateTime string

        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date: " + dateTimeStr + " - " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("An error occurred while parsing date: " + dateTimeStr + " - " + e.getMessage());
            throw e;
        }//This block parse string into date so Exception should be processed
    }

    private static String leadZeroIfOneSymbol(String digitString) {// if  digitString consist of 1 symbol then it leads by zero
        return digitString.length() == 1 ? "0" + digitString : digitString;
    }


    /**
     * Method return  List<CSVRecord> for licenses that are in range of defined dateTimes
     *
     * @param csvRecordList
     * @param startDateStr
     * @param endDateStr
     * @return List<CSVRecord> list
     */
    public static List<CSVRecord> licensesInDateRange(List<CSVRecord> csvRecordList, String startDateStr, String endDateStr) {
        //This 3 line format string date into LocalDateTime form with defined format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        LocalDateTime startDate = parseStringToDateTime(startDateStr, formatter);
        LocalDateTime endDate = parseStringToDateTime(endDateStr, formatter);

        // Stream through csvRecordList and filter based on the date range, then return into main
        return csvRecordList.stream()
                .filter(record -> {
                    String validDateStr = record.get("ValidDate");
                    if (validDateStr.isEmpty()) { // Skip this record if field ValidDate is empty
                        return false;
                    }
                    LocalDateTime validUntilDate = parseStringToDateTime(validDateStr, formatter);// parse validUntil string Value in LocalDateTime form
                    return !validUntilDate.isBefore(startDate) && !validUntilDate.isAfter(endDate); // Check if ValidDate is within the specified range
                })
                .collect(Collectors.toList());
    }

    /**
     * Method read CSV file
     *
     * @param fileName
     * @return List<CSVRecord>
     */
    public static List<CSVRecord> readCsvFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Processor.class.getClassLoader().getResourceAsStream(fileName)))) {
            CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(reader);
            return csvParser.getRecords();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}



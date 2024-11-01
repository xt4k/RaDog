package assigment.tech.rakuten;

import assigment.tech.rakuten.utils.Processor;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Map;

import static assigment.tech.rakuten.utils.Processor.*;
import static java.lang.String.*;
import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {
        out.println("-----------------------------------------------");
        out.println("Programming task: Task 1");

        out.println("0.1 Read the .csv file.");
        List<CSVRecord> dogData = readCsvFile("2017.csv");
        out.println("-----------------------------------------------");


        out.println("1.1: extract the Breeds provided. Normalize the breed names by removing all whitespaces " +
                "and making them all lowercase. Lastly create a list of unique breeds without duplicates: ");

        List<String> uniqueBreedsList = getAndProcessBreedsToUniqueList(dogData);
        out.println("We have such breeds:\n " + join("\n", uniqueBreedsList));
        out.println("------------------\n\tBreeds number: " + uniqueBreedsList.size());
        out.println("-----------------------------------------------");


        out.println("1.2 Create a list of number of licenses by LicenseType of each unique breed.");

        Map<String, Map<String, Long>> mapLicenseTypesByBreed = getLicenseTypeMapByUniqueBreed(dogData);
        out.println("License type and number by breed:");
        mapLicenseTypesByBreed.forEach((breed, licenseMap) -> {
            out.println("Breed: " + breed);
            licenseMap.forEach((type, count) -> out.println("  LicenseType: " + type + ", Count: " + count));
        });
        out.println("-----------------------------------------------\n\t");

        out.println("1.3 Find out the top 5 popular name of dogs and create a list of these names along with count of dogs having these names.");
       int nTopNames = 5;
        List<Map.Entry<String, Long>> topNamesList = popularDogNames(dogData, nTopNames);
        out.println(nTopNames + " popular dog names:");
        topNamesList.forEach(entry -> out.printf("DogName: `%s` - `%s` times%n", entry.getKey(), entry.getValue()));
        out.println("-----------------------------------------------\n\t");

        out.println("Bonus task: Create a method which takes date range as input and return the details of licences issues during that date. ");

        String startDate = "01/01/2017 00:01";
        String endDate = "12/31/2017 23:59";
        List<CSVRecord> licensesInRange = Processor.licensesInDateRange(dogData, startDate, endDate);

        System.out.println("\nLicenses, that valid in range:" + startDate + " --- " + endDate + ":");
        licensesInRange.forEach(r -> System.out.println(r.toMap().values()));

        out.println(format("Total license number: `%s` are valid in range: `%s` ---  `%s`",licensesInRange.size(),startDate,endDate));
        out.println("====================== THE END ==============================");
    }
}
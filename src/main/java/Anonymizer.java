import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class Anonymizer extends Utils {

    /**
     * Entry point.
     *
     * @param args
     *            the arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File resourcesDirectory = new File("src/main/resources/");
        File dataFile = new File(resourcesDirectory.getAbsolutePath(), "adult.csv");

        Data data = Data.create(dataFile.getAbsolutePath(), Charset.defaultCharset(), ',');

        AttributeType.Hierarchy.DefaultHierarchy gender = AttributeType.Hierarchy.create();
        gender.add("Male", "*");
        gender.add("Female", "*");


        AttributeType.Hierarchy.DefaultHierarchy race = AttributeType.Hierarchy.create();
        race.add("White", "*");
        race.add("Asian-Pac-Islander", "*");
        race.add("Amer-Indian-Eskimo", "*");
        race.add("Other", "*");
        race.add("Black", "*");

        // Define data types
        data.getDefinition().setDataType("age", DataType.INTEGER);
        data.getDefinition().setDataType("gender", DataType.STRING);
        data.getDefinition().setDataType("race", DataType.STRING);


        // Define input files
        data.getDefinition().setAttributeType("marital-status", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setResponseVariable("marital-status", true);

        data.getDefinition().setAttributeType("age", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("gender", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
        data.getDefinition().setAttributeType("race", AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);


        // Define hierarchy.
        File ageFile = new File(resourcesDirectory.getAbsolutePath(), "age.csv");

        data.getDefinition().setHierarchy("age", AttributeType.Hierarchy.create(ageFile.getAbsolutePath(), StandardCharsets.UTF_8, ';'));
        data.getDefinition().setHierarchy("gender", gender);
        data.getDefinition().setHierarchy("race", race);


        //age quasi-identifier is generalized to the levels 2-4 of its generalization hierarchy
        data.getDefinition().setMinimumGeneralization("age", 2);
        data.getDefinition().setMaximumGeneralization("age", 4);


        // Perform anonymization
        ARXAnonymizer anonymizer = new ARXAnonymizer();

        ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(1d);
        config.setQualityModel(Metric.createLossMetric());

        ARXResult result = anonymizer.anonymize(data, config);

        // Print info
        printResult(result, data);

        // Write results
        System.out.print(" - Writing data...");
        result.getOutput(false).save("data/test_anonymized.csv", ',');
        System.out.println("Done!");

    }

}

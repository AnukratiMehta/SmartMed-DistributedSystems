/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 * Implementation of gRPC service for patient-related operations.
 * Handles requests to fetch patient records and generates synthetic patient data.
 * 
 * @author anukratimehta
 */

import com.healthcare.grpc.patient.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.*;
import java.util.logging.Logger;
import distsys.smartmed.common.LoggingUtils;

// gRPC service implementation for PatientService
public class PatientServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {
    // Logger instance to log activities in this service
    private static final Logger logger = Logger.getLogger(PatientServiceImpl.class.getName());
    
    /**
     * Handles the getPatientRecord gRPC call.
     * Validates the request, generates a synthetic patient record,
     * and sends the response back to the client.
     */
    @Override
    public void getPatientRecord(PatientRequest request, StreamObserver<PatientResponse> responseObserver) {
        String patientId = request.getPatientId();
        
        try {
            // Validate patient ID format
            ValidationUtils.validatePatientId(patientId);
            // Log service start
            LoggingUtils.logServiceStart(logger, "PatientService", patientId);

            // Generate a synthetic patient record based on the patient ID
            PatientResponse response = generatePatientRecord(patientId);

            // Send the response to the client
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            // Log service end
            LoggingUtils.logServiceEnd(logger, "PatientService", patientId, "Record fetched");

        } catch (IllegalArgumentException e) {
            // Log validation error and return INVALID_ARGUMENT status
            LoggingUtils.logError(logger, "PatientService", patientId, e, true);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            // Log unexpected server error and return INTERNAL status
            LoggingUtils.logError(logger, "PatientService", patientId, e, false);
            responseObserver.onError(Status.INTERNAL.withDescription("Server error").asRuntimeException());
        }
    }

    /**
     * Generates a synthetic patient record using the patient ID.
     * Ensures consistent data for the same ID by seeding Random with hash.
     *
     * @param patientId the ID of the patient
     * @return a generated PatientResponse object
     */
    private PatientResponse generatePatientRecord(String patientId) {
        logger.fine("Generating patient record for: " + patientId);

        // Sample first and last names
        String[] firstNames = {
            "John", "Jane", "Robert", "Emily", "Michael", "Sarah", "David", "Lisa",
            "James", "Mary", "William", "Jennifer", "Richard", "Jessica", "Thomas", "Elizabeth"
        };
        
        String[] lastNames = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson",
            "Anderson", "Taylor", "Thomas", "White", "Harris", "Martin", "Thompson", "Garcia"
        };
        
        // Sample medical conditions with ICD-10 codes
        String[][] conditions = {
            {"Hypertension", "I10"}, 
            {"Type 2 Diabetes", "E11.9"},
            {"Asthma", "J45.909"},
            {"Major Depressive Disorder", "F32.9"},
            {"Osteoarthritis", "M17.9"},
            {"Hyperlipidemia", "E78.5"},
            {"GERD", "K21.9"},
            {"Chronic Kidney Disease", "N18.9"},
            {"COPD", "J44.9"},
            {"Hypothyroidism", "E03.9"}
        };
        
        // Sample medications with their categories
        String[][] medications = {
            {"Metformin 500mg", "Antidiabetic"},
            {"Lisinopril 10mg", "ACE Inhibitor"},
            {"Ibuprofen 400mg", "NSAID"},
            {"Atorvastatin 20mg", "Statin"},
            {"Sertraline 50mg", "SSRI"},
            {"Albuterol Inhaler", "Bronchodilator"},
            {"Omeprazole 20mg", "PPI"},
            {"Levothyroxine 50mcg", "Thyroid Hormone"},
            {"Losartan 50mg", "ARB"},
            {"Amlodipine 5mg", "Calcium Channel Blocker"}
        };
        
        // Generate consistent pseudo-random data based on the patientId
        int hash = patientId.hashCode();
        Random rand = new Random(hash); // Use hash as seed for consistency

        // Choose a name based on hash
        String name = firstNames[Math.abs(hash) % firstNames.length] + " " + 
                      lastNames[Math.abs(hash / 31) % lastNames.length];
        
        // Generate a list of 1 to 3 medical conditions
        List<String> medicalHistory = new ArrayList<>();
        int conditionCount = 1 + Math.abs(hash) % 3;
        for (int i = 0; i < conditionCount; i++) {
            String[] condition = conditions[(Math.abs(hash) + i) % conditions.length];
            int diagnosisYear = 2015 + Math.abs(hash + i) % 9; // Years from 2015 to 2023
            medicalHistory.add(condition[0] + " (" + condition[1] + ") dx. " + diagnosisYear);
        }
        
        // Decide if the patient is currently on medication (80% chance)
        String currentMedication = "None";
        if (rand.nextDouble() > 0.2) {
            String[] medication = medications[Math.abs(hash) % medications.length];
            currentMedication = medication[0] + " (" + medication[1] + ")";
        }
        
        // Generate age between 20 and 80
        int age = 20 + Math.abs(hash) % 61;
        
        // Build and return the patient response
        return PatientResponse.newBuilder()
            .setPatientId(patientId)
            .setName(name)
            .setAge(age)
            .setCurrentMedication(currentMedication)
            .addAllMedicalHistory(medicalHistory)
            .build();
    }
}

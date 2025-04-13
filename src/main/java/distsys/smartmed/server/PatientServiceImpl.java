/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
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

public class PatientServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {
    private static final Logger logger = Logger.getLogger(PatientServiceImpl.class.getName());
    
@Override
public void getPatientRecord(PatientRequest request, StreamObserver<PatientResponse> responseObserver) {
    String patientId = request.getPatientId();
    
    try {
        ValidationUtils.validatePatientId(patientId);
        LoggingUtils.logServiceStart(logger, "PatientService", patientId);

        PatientResponse response = generatePatientRecord(patientId);
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        LoggingUtils.logServiceEnd(logger, "PatientService", patientId, "Record fetched");

    } catch (IllegalArgumentException e) {
        LoggingUtils.logError(logger, "PatientService", patientId, e, true);
        responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
        LoggingUtils.logError(logger, "PatientService", patientId, e, false);
        responseObserver.onError(Status.INTERNAL.withDescription("Server error").asRuntimeException());
    }
}   
    private PatientResponse generatePatientRecord(String patientId) {
        logger.fine("Generating patient record for: " + patientId);
    // Expanded name databases
    String[] firstNames = {
        "John", "Jane", "Robert", "Emily", "Michael", "Sarah", "David", "Lisa",
        "James", "Mary", "William", "Jennifer", "Richard", "Jessica", "Thomas", "Elizabeth"
    };
    
    String[] lastNames = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson",
        "Anderson", "Taylor", "Thomas", "White", "Harris", "Martin", "Thompson", "Garcia"
    };
    
    // Expanded medical conditions with realistic ICD-10 codes
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
    
    // Expanded medications with categories
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
    
    // Generate consistent data based on patientId hash
    int hash = patientId.hashCode();
    Random rand = new Random(hash); // Seed with hash for consistency
    
    // Select name
    String name = firstNames[Math.abs(hash) % firstNames.length] + " " + 
                 lastNames[Math.abs(hash/31) % lastNames.length];
    
    // Generate 1-3 medical conditions
    List<String> medicalHistory = new ArrayList<>();
    int conditionCount = 1 + Math.abs(hash) % 3;
    for (int i = 0; i < conditionCount; i++) {
        String[] condition = conditions[(Math.abs(hash) + i) % conditions.length];
        int diagnosisYear = 2015 + Math.abs(hash + i) % 9; // 2015-2023
        medicalHistory.add(condition[0] + " (" + condition[1] + ") dx. " + diagnosisYear);
    }
    
    // Generate current medication (possibly none)
    String currentMedication = "None";
    if (rand.nextDouble() > 0.2) { // 80% chance of having medication
        String[] medication = medications[Math.abs(hash) % medications.length];
        currentMedication = medication[0] + " (" + medication[1] + ")";
    }
    
    // Generate age (20-80 years old)
    int age = 20 + Math.abs(hash) % 61;
    
    return PatientResponse.newBuilder()
        .setPatientId(patientId)
        .setName(name)
        .setAge(age)
        .setCurrentMedication(currentMedication)
        .addAllMedicalHistory(medicalHistory)
        .build();
}
}
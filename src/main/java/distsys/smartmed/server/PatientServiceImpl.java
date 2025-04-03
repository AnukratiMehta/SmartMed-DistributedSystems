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
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class PatientServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {
    
    @Override
    public void getPatientRecord(PatientRequest request, 
                               StreamObserver<PatientResponse> responseObserver) {
        String patientId = request.getPatientId();
        
        try {
            // Validate ID is numeric and between 1-100
            int id = Integer.parseInt(patientId);
            if (id < 1 || id > 100) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("ID must be 1-100")
                    .asRuntimeException());
                return;
            }
            
            // Generate or fetch patient record
            PatientResponse response = generatePatientRecord(patientId);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (NumberFormatException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid patient ID format")
                .asRuntimeException());
        }
    }
    
    private PatientResponse generatePatientRecord(String patientId) {
        String[] firstNames = {"John", "Jane", "Robert", "Emily"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown"};
        String[] conditions = {"Hypertension", "Diabetes", "Asthma"};
        String[] medications = {"Metformin", "Lisinopril", "Ibuprofen"};

        int hash = patientId.hashCode();
        String name = firstNames[Math.abs(hash) % firstNames.length] + " " + 
                     lastNames[Math.abs(hash/31) % lastNames.length];
        
        return PatientResponse.newBuilder()
            .setPatientId(patientId)
            .setName(name)
            .setCurrentMedication(medications[Math.abs(hash) % medications.length])
            .addMedicalHistory(conditions[Math.abs(hash) % conditions.length] + " (2023)")
            .build();
    }
}
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
import io.grpc.stub.StreamObserver;

public class PatientServiceImpl extends PatientServiceGrpc.PatientServiceImplBase {
    @Override
    public void getPatientRecord(PatientRequest request, StreamObserver<PatientResponse> responseObserver) {
        String patientId = request.getPatientId();
        System.out.println("[SmartMed] Fetching record for patient: " + patientId);

        PatientResponse response = PatientResponse.newBuilder()
            .setPatientId(patientId)
            .setName("John Doe")
            .addMedicalHistory("Hypertension (2022)")
            .addMedicalHistory("Diabetes (2021)")
            .setCurrentMedication("Metformin, Lisinopril")
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

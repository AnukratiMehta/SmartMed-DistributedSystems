/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 *
 * @author anukratimehta
 */


import com.healthcare.grpc.medication.*;
import io.grpc.stub.StreamObserver;
import java.util.*;

public class MedicationServiceImpl extends MedicationServiceGrpc.MedicationServiceImplBase {

    @Override
    public StreamObserver<MedicationRecord> analyzeMedicationSchedule(
        StreamObserver<MedicationAnalysis> responseObserver) {
        
        return new StreamObserver<MedicationRecord>() {
            private final List<MedicationRecord> records = new ArrayList<MedicationRecord>();
            private String currentPatientId = null;

            @Override
            public void onNext(MedicationRecord record) {
                records.add(record);
                if (currentPatientId == null) {
                    currentPatientId = record.getPatientId();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in medication analysis: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                int total = records.size();
                int taken = 0;
                List<MedicationRecord> missed = new ArrayList<MedicationRecord>();
                
                for (MedicationRecord record : records) {
                    if (record.getWasTaken()) {
                        taken++;
                    } else {
                        missed.add(record);
                    }
                }
                
                float percentage = total > 0 ? (taken * 100.0f) / total : 0;
                String summary = generateSummary(percentage, total);
                
                MedicationAnalysis analysis = MedicationAnalysis.newBuilder()
                    .setAdherencePercentage(percentage)
                    .setTotalDoses(total)
                    .setTakenDoses(taken)
                    .addAllMissedDoses(missed)
                    .setSummary(summary)
                    .build();
                
                responseObserver.onNext(analysis);
                responseObserver.onCompleted();
            }
            
            private String generateSummary(float percentage, int total) {
                if (total == 0) return "No medication scheduled";
                if (percentage >= 90) return "Excellent adherence!";
                if (percentage >= 70) return "Good adherence";
                if (percentage >= 50) return "Fair adherence";
                return "Poor adherence - please consult your doctor";
            }
        };
    }
}
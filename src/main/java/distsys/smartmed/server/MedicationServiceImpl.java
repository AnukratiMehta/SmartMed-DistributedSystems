package distsys.smartmed.server;

import com.healthcare.grpc.medication.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.stub.StreamObserver;
import java.util.*;
import java.util.logging.Logger;
import io.grpc.Status;

public class MedicationServiceImpl extends MedicationServiceGrpc.MedicationServiceImplBase {
    private static final Logger logger = Logger.getLogger(MedicationServiceImpl.class.getName());

    @Override
    public StreamObserver<MedicationRecord> analyzeMedicationSchedule(
        StreamObserver<MedicationAnalysis> responseObserver) {
        
        return new StreamObserver<MedicationRecord>() {
            private final List<MedicationRecord> records = new ArrayList<>();
            private String currentPatientId = null;

            @Override
            public void onNext(MedicationRecord record) {
                try {
                    // Added validation
                    ValidationUtils.validatePatientId(record.getPatientId());
                    
                    if (currentPatientId == null) {
                        currentPatientId = record.getPatientId();
                        logger.info("Starting medication analysis for patient: " + currentPatientId);
                    }
                    records.add(record);
                    logger.fine("Received medication record: " + record.getMedicationName() + 
                              " at " + record.getScheduledTime());
                              
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid medication record: " + e.getMessage());
                    responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription(e.getMessage())
                        .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("Medication analysis error for " + currentPatientId + 
                            ": " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                int total = records.size();
                int taken = 0;
                List<MedicationRecord> missed = new ArrayList<>();
                
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
                logger.info("Completed medication analysis for " + currentPatientId + 
                           ": " + percentage + "% adherence");
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
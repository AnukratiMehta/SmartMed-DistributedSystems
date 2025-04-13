package distsys.smartmed.server;

import com.healthcare.grpc.medication.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.stub.StreamObserver;
import java.util.*;
import java.util.logging.Logger;
import io.grpc.Status;
import distsys.smartmed.common.LoggingUtils;
import java.util.stream.Collectors;


public class MedicationServiceImpl extends MedicationServiceGrpc.MedicationServiceImplBase {
    private static final Logger logger = Logger.getLogger(MedicationServiceImpl.class.getName());
    
    private MedicationAnalysis generateAnalysis(List<MedicationRecord> records) {
    int total = records.size();
    int taken = (int) records.stream().filter(MedicationRecord::getWasTaken).count();
    float percentage = total > 0 ? (taken * 100.0f) / total : 0;
    
    return MedicationAnalysis.newBuilder()
        .setAdherencePercentage(percentage)
        .setTotalDoses(total)
        .setTakenDoses(taken)
        .addAllMissedDoses(records.stream().filter(r -> !r.getWasTaken()).collect(Collectors.toList()))
        .setSummary(generateSummary(percentage, total))
        .build();
}

private String generateSummary(float percentage, int total) {
    if (total == 0) return "No medication scheduled";
    if (percentage >= 90) return "Excellent adherence!";
    if (percentage >= 70) return "Good adherence";
    if (percentage >= 50) return "Fair adherence";
    return "Poor adherence - please consult your doctor";
}

@Override
public StreamObserver<MedicationRecord> analyzeMedicationSchedule(StreamObserver<MedicationAnalysis> responseObserver) {
    return new StreamObserver<MedicationRecord>() {
        private String patientId = null;
        private final List<MedicationRecord> records = new ArrayList<>();

        @Override
        public void onNext(MedicationRecord record) {
            try {
                ValidationUtils.validatePatientId(record.getPatientId());
                
                if (patientId == null) {
                    patientId = record.getPatientId();
                    LoggingUtils.logServiceStart(logger, "MedicationService", patientId);
                }

                records.add(record);
                LoggingUtils.logFine(logger, "Received medication: %s at %s (%s)",
                    record.getMedicationName(), record.getScheduledTime(),
                    record.getWasTaken() ? "taken" : "missed");

            } catch (IllegalArgumentException e) {
                LoggingUtils.logError(logger, "MedicationService", record.getPatientId(), e, true);
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            }
        }

@Override
public void onCompleted() {
    if (!records.isEmpty()) {
        int total = records.size();
        int taken = (int) records.stream().filter(MedicationRecord::getWasTaken).count();
        float percentage = (taken * 100.0f) / total;
        
        // Generate appropriate message
        String summaryMessage;
        if (percentage >= 90) {
            summaryMessage = "Excellent adherence! Keep it up!";
        } else if (percentage >= 70) {
            summaryMessage = "Good adherence, but could improve";
        } else if (percentage >= 50) {
            summaryMessage = "Fair adherence - consult your doctor";
        } else {
            summaryMessage = "Poor adherence - requires intervention";
        }

        MedicationAnalysis analysis = MedicationAnalysis.newBuilder()
            .setAdherencePercentage(percentage)
            .setTotalDoses(total)
            .setTakenDoses(taken)
            .addAllMissedDoses(getMissedRecords(records))
            .setSummary(summaryMessage)  // <-- Add this line
            .build();
        
        responseObserver.onNext(analysis);
    }
    responseObserver.onCompleted();
}

private List<MedicationRecord> getMissedRecords(List<MedicationRecord> records) {
    return records.stream()
        .filter(record -> !record.getWasTaken())
        .collect(Collectors.toList());
}

        @Override
        public void onError(Throwable t) {
            LoggingUtils.logError(logger, "MedicationService", patientId != null ? patientId : "unknown", t, false);
        }
    };
}
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 * gRPC service implementation for analyzing a patient's medication schedule in the SmartMed system.
 * Calculates adherence percentage and provides feedback based on incoming medication records.
 * 
 * @author anukratimehta
 */

import com.healthcare.grpc.medication.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.*;
import java.util.logging.Logger;
import distsys.smartmed.common.LoggingUtils;
import java.util.stream.Collectors;

public class MedicationServiceImpl extends MedicationServiceGrpc.MedicationServiceImplBase {
    private static final Logger logger = Logger.getLogger(MedicationServiceImpl.class.getName());

    /**
     * Generates a medication adherence analysis from a list of medication records.
     *
     * @param records List of MedicationRecord objects to analyze
     * @return MedicationAnalysis object with adherence stats and summary
     */
    private MedicationAnalysis generateAnalysis(List<MedicationRecord> records) {
        int total = records.size();
        int taken = (int) records.stream().filter(MedicationRecord::getWasTaken).count();
        float percentage = total > 0 ? (taken * 100.0f) / total : 0;

        return MedicationAnalysis.newBuilder()
            .setAdherencePercentage(percentage)
            .setTotalDoses(total)
            .setTakenDoses(taken)
            .addAllMissedDoses(records.stream().filter(r -> !r.getWasTaken()).collect(Collectors.toList()))
            .setSummary(generateAdherenceSummary(percentage))
            .build();
    }

    /**
     * Provides a text-based summary based on adherence percentage.
     *
     * @param percentage Calculated medication adherence percentage
     * @return Summary string describing the adherence quality
     */
    private String generateAdherenceSummary(float percentage) {
        if (percentage >= 90) return "Excellent adherence! Keep it up!";
        if (percentage >= 70) return "Good adherence, but could improve";
        if (percentage >= 50) return "Fair adherence - consult your doctor";
        return "Poor adherence - requires intervention";
    }

    /**
     * Receives a stream of MedicationRecord messages and responds with an adherence analysis.
     * Validates patient ID, logs each incoming record, and builds a final report upon completion.
     *
     * @param responseObserver StreamObserver to send the final MedicationAnalysis response
     * @return StreamObserver to receive MedicationRecord messages from the client
     */
    @Override
    public StreamObserver<MedicationRecord> analyzeMedicationSchedule(StreamObserver<MedicationAnalysis> responseObserver) {
        return new StreamObserver<MedicationRecord>() {
            private String patientId = null;
            private final List<MedicationRecord> records = new ArrayList<>();

            @Override
            public void onNext(MedicationRecord record) {
                try {
                    // Validate the incoming patient ID
                    ValidationUtils.validatePatientId(record.getPatientId());

                    // Log service start when the first record is received
                    if (patientId == null) {
                        patientId = record.getPatientId();
                        LoggingUtils.logServiceStart(logger, "MedicationService", patientId);
                    }

                    records.add(record);

                    // Log each medication intake event
                    LoggingUtils.logFine(logger, "Received medication: %s at %s (%s)",
                        record.getMedicationName(), record.getScheduledTime(),
                        record.getWasTaken() ? "taken" : "missed");

                } catch (IllegalArgumentException e) {
                    // Handle validation failure
                    LoggingUtils.logError(logger, "MedicationService", record.getPatientId(), e, true);
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
                }
            }

            @Override
            public void onCompleted() {
                // Send back the analysis report and complete the stream
                if (!records.isEmpty()) {
                    responseObserver.onNext(generateAnalysis(records));
                }
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                // Log any unexpected errors
                LoggingUtils.logError(logger, "MedicationService", patientId != null ? patientId : "unknown", t, false);
            }
        };
    }
}

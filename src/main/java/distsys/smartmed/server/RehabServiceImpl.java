package distsys.smartmed.server;

import com.healthcare.grpc.rehab.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import distsys.smartmed.common.LoggingUtils;

public class RehabServiceImpl extends RehabServiceGrpc.RehabServiceImplBase {
    private static final Logger logger = Logger.getLogger(RehabServiceImpl.class.getName());

    @Override
    public StreamObserver<ExerciseInput> liveExerciseFeedback(
        StreamObserver<ExerciseFeedback> responseObserver) {
        
        return new StreamObserver<ExerciseInput>() {
            private int totalReps = 0;
            private int goodPostureCount = 0;
            private String currentExercise = "";
            private String patientId = "";

            @Override
            public void onNext(ExerciseInput input) {
                try {
                    // Validate first
                    ValidationUtils.validatePatientId(input.getPatientId());
                    
                    if (totalReps == 0) {
                        // Initialize session on first rep
                        patientId = input.getPatientId();
                        currentExercise = input.getExerciseName();
                        LoggingUtils.logServiceStart(logger, "RehabService", patientId);
                    }

                    totalReps++;
                    double angle = input.getPostureAngle();
                    
                    // Generate immediate feedback
                    String feedbackMsg;
                    String severity;
                    if (angle < 30) {
                        feedbackMsg = "Bend less at the knees";
                        severity = "WARNING";
                    } else if (angle > 45) {
                        feedbackMsg = "Too rigid - relax your muscles";
                        severity = "WARNING";
                    } else {
                        feedbackMsg = "Perfect form!";
                        severity = "INFO";
                        goodPostureCount++;
                    }

                    // Send real-time feedback
                    responseObserver.onNext(ExerciseFeedback.newBuilder()
                        .setRepetitionNumber(input.getRepetitionNumber())
                        .setMessage(feedbackMsg)
                        .setSeverity(severity)
                        .build());

                    logger.fine(String.format("Processed rep %d: %.1f° - %s", 
                        input.getRepetitionNumber(), angle, severity));

                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid input: " + e.getMessage());
                    responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription(e.getMessage())
                        .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.warning("Client error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                try {
                    // Send final summary
                    double successRate = (goodPostureCount * 100.0) / totalReps;
                    String summary = String.format(
                        "Completed %d reps of %s.\nGood posture: %.1f%% (%d/%d)",
                        totalReps, currentExercise, successRate, goodPostureCount, totalReps);
                    
                    responseObserver.onNext(ExerciseFeedback.newBuilder()
                        .setRepetitionNumber(0) // Special marker for summary
                        .setMessage(summary)
                        .setSeverity("SUMMARY")
                        .build());
                    
                    responseObserver.onCompleted();
                    logger.info("Session completed for " + patientId + ": " + summary);
                    
                } catch (Exception e) {
                    logger.severe("Error completing session: " + e.getMessage());
                }
            }
        };
    }
}
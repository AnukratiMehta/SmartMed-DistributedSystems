/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 * gRPC service implementation for live exercise feedback during rehabilitation sessions.
 * Provides real-time posture analysis and a summary of performance at the end.
 * 
 * @author anukratimehta
 */

import com.healthcare.grpc.rehab.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import distsys.smartmed.common.LoggingUtils;

public class RehabServiceImpl extends RehabServiceGrpc.RehabServiceImplBase {
    private static final Logger logger = Logger.getLogger(RehabServiceImpl.class.getName());

    /**
     * Processes a live stream of ExerciseInput messages and sends immediate feedback
     * based on posture angle. Sends a summary upon stream completion.
     *
     * @param responseObserver StreamObserver to send ExerciseFeedback messages to the client
     * @return StreamObserver to receive ExerciseInput messages from the client
     */
    @Override
    public StreamObserver<ExerciseInput> liveExerciseFeedback(
        StreamObserver<ExerciseFeedback> responseObserver) {
        
        return new StreamObserver<ExerciseInput>() {
            private int totalReps = 0;
            private int goodPostureCount = 0;
            private String currentExercise = "";
            private String patientId = "";

            /**
             * Called when a new repetition is received.
             * Validates patient ID, evaluates posture, and sends real-time feedback.
             *
             * @param input the ExerciseInput from the client
             */
            @Override
            public void onNext(ExerciseInput input) {
                try {
                    // Validate the patient ID
                    ValidationUtils.validatePatientId(input.getPatientId());
                    
                    // Initialize session with first input
                    if (totalReps == 0) {
                        patientId = input.getPatientId();
                        currentExercise = input.getExerciseName();
                        LoggingUtils.logServiceStart(logger, "RehabService", patientId);
                    }

                    totalReps++;
                    double angle = input.getPostureAngle();
                    
                    // Determine feedback based on posture angle
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

                    // Send feedback back to client
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

            /**
             * Called when the stream ends successfully.
             * Sends a summary message with the overall posture success rate.
             */
            @Override
            public void onCompleted() {
                try {
                    double successRate = (goodPostureCount * 100.0) / totalReps;
                    String summary = String.format(
                        "Completed %d reps of %s.\nGood posture: %.1f%% (%d/%d)",
                        totalReps, currentExercise, successRate, goodPostureCount, totalReps);
                    
                    // Send final summary
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

            /**
             * Called when the client sends an error.
             *
             * @param t the Throwable received from the client side
             */
            @Override
            public void onError(Throwable t) {
                logger.warning("Client error: " + t.getMessage());
            }
        };
    }
}

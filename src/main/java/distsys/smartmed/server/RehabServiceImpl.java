/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 *
 * @author anukratimehta
 */

import io.grpc.stub.StreamObserver;
import com.healthcare.grpc.rehab.ExerciseInput;
import com.healthcare.grpc.rehab.ExerciseFeedback;
import com.healthcare.grpc.rehab.RehabServiceGrpc;
import java.util.logging.Logger;
import java.util.logging.Level;


public class RehabServiceImpl extends RehabServiceGrpc.RehabServiceImplBase {
    private static final Logger logger = Logger.getLogger(RehabServiceImpl.class.getName());

    @Override
    public StreamObserver<ExerciseInput> liveExerciseFeedback(
        StreamObserver<ExerciseFeedback> responseObserver) {
        
        return new StreamObserver<ExerciseInput>() {
            private int totalReps = 0;
            private int goodPostureCount = 0;
            private int warningCount = 0;
            private int criticalCount = 0;
            private String currentExercise = "";
            private String patientId = "";

            @Override
            public void onNext(ExerciseInput exerciseInput) {
                // Store exercise info on first rep
                if (totalReps == 0) {
                    currentExercise = exerciseInput.getExerciseName();
                    patientId = exerciseInput.getPatientId();
                }
                
                totalReps++;
                String feedbackMessage;
                String severity;
                double angle = exerciseInput.getPostureAngle();

                // Provide feedback based on posture angle
                if (angle < 30) {
                    feedbackMessage = "Your posture is too bent. Try to straighten your back.";
                    severity = "critical";
                    criticalCount++;
                } else if (angle > 45) {
                    feedbackMessage = "Your posture is too rigid. Try to relax your muscles.";
                    severity = "warning";
                    warningCount++;
                } else {
                    feedbackMessage = "Your posture is good!";
                    severity = "info";
                    goodPostureCount++;
                }

                // Send immediate feedback
                responseObserver.onNext(ExerciseFeedback.newBuilder()
                    .setMessage(feedbackMessage)
                    .setRepetitionNumber(exerciseInput.getRepetitionNumber())
                    .setSeverity(severity)
                    .build());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Exercise stream error", t);
            }

            @Override
            public void onCompleted() {
                // Calculate performance metrics
                double goodPosturePercentage = (double) goodPostureCount / totalReps * 100;
                String summaryMessage;
                
                if (goodPosturePercentage >= 80) {
                    summaryMessage = String.format(
                        "Excellent work %s! You maintained good posture in %.1f%% of your %s reps (%d/%d).",
                        patientId, goodPosturePercentage, currentExercise, goodPostureCount, totalReps
                    );
                } else if (goodPosturePercentage >= 60) {
                    summaryMessage = String.format(
                        "Good job %s. You had good posture in %.1f%% of %s reps (%d/%d). Try to maintain form consistently.",
                        patientId, goodPosturePercentage, currentExercise, goodPostureCount, totalReps
                    );
                } else {
                    summaryMessage = String.format(
                        "%s, you had good posture in only %.1f%% of %s reps (%d/%d). Focus on your form next time.",
                        patientId, goodPosturePercentage, currentExercise, goodPostureCount, totalReps
                    );
                }

                // Send final summary
                responseObserver.onNext(ExerciseFeedback.newBuilder()
                    .setMessage(summaryMessage)
                    .setRepetitionNumber(0) // Special value for summary
                    .setSeverity("summary")
                    .build());

                responseObserver.onCompleted();
            }
        };
    }
}
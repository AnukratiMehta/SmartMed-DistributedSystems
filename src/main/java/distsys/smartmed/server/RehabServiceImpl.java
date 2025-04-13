package distsys.smartmed.server;

import com.healthcare.grpc.rehab.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

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
                try {
                    // Validate before processing
                    ValidationUtils.validatePatientId(exerciseInput.getPatientId());
                    
                    if (totalReps == 0) {
                        currentExercise = exerciseInput.getExerciseName();
                        patientId = exerciseInput.getPatientId();
                        logger.info(String.format(
                            "Starting rehab session for %s: %s",
                            patientId, currentExercise));
                    }
                    
                    totalReps++;
                    double angle = exerciseInput.getPostureAngle();
                    logger.fine(String.format(
                        "Rep %d for %s - Angle: %.1f°",
                        exerciseInput.getRepetitionNumber(),
                        currentExercise,
                        angle));

                    String feedbackMessage;
                    String severity;
                    
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

                    responseObserver.onNext(ExerciseFeedback.newBuilder()
                        .setMessage(feedbackMessage)
                        .setRepetitionNumber(exerciseInput.getRepetitionNumber())
                        .setSeverity(severity)
                        .build());

                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid exercise input: " + e.getMessage());
                    responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription(e.getMessage())
                        .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.warning(String.format(
                    "Rehab session error for %s: %s",
                    patientId, t.getMessage()));
            }

            @Override
            public void onCompleted() {
                if (totalReps == 0) {
                    logger.warning("Empty rehab session received");
                    responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("No exercise data received")
                        .asRuntimeException());
                    return;
                }

                double goodPosturePercentage = (double) goodPostureCount / totalReps * 100;
                String summaryMessage;
                
                if (goodPosturePercentage >= 80) {
                    summaryMessage = String.format(
                        "Excellent work %s! You maintained good posture in %.1f%% of your %s reps (%d/%d).",
                        patientId, goodPosturePercentage, currentExercise, goodPostureCount, totalReps);
                } else if (goodPosturePercentage >= 60) {
                    summaryMessage = String.format(
                        "Good job %s. You had good posture in %.1f%% of %s reps (%d/%d). Try to maintain form consistently.",
                        patientId, goodPosturePercentage, currentExercise, goodPostureCount, totalReps);
                } else {
                    summaryMessage = String.format(
                        "%s, you had good posture in only %.1f%% of %s reps (%d/%d). Focus on your form next time.",
                        patientId, goodPosturePercentage, currentExercise, goodPostureCount, totalReps);
                }

                responseObserver.onNext(ExerciseFeedback.newBuilder()
                    .setMessage(summaryMessage)
                    .setRepetitionNumber(0)
                    .setSeverity("summary")
                    .build());

                responseObserver.onCompleted();
                logger.info(String.format(
                    "Completed rehab session for %s: %d reps of %s (%d good, %d warnings, %d critical)",
                    patientId, totalReps, currentExercise, goodPostureCount, warningCount, criticalCount));
            }
        };
    }
}
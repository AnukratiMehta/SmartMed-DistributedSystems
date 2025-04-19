/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 * gRPC service implementation for streaming patient vitals in the SmartMed system.
 * Simulates real-time heart rate and oxygen saturation data for monitoring purposes.
 *
 * @author anukratimehta
 */

import com.healthcare.grpc.monitoring.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import distsys.smartmed.common.LoggingUtils;

public class MonitoringServiceImpl extends MonitoringServiceGrpc.MonitoringServiceImplBase {
    private static final Logger logger = Logger.getLogger(MonitoringServiceImpl.class.getName());

    /**
     * Simulates the generation of a vitals update.
     * Generates heart rate and oxygen saturation with random but realistic values.
     *
     * @return VitalsUpdate message containing simulated data
     */
    private VitalsUpdate generateVitalsUpdate() {
        return VitalsUpdate.newBuilder()
            .setHeartRate(70 + (int)(Math.random() * 10))  // 70-80 bpm
            .setOxygenLevel(95 + (float)(Math.random() * 5))  // 95-100%
            .setTimestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * Streams vitals updates for a given duration.
     * Validates patient ID, sends simulated vitals every second for the requested duration.
     *
     * @param request VitalsRequest message containing patient ID and duration
     * @param responseObserver StreamObserver to send VitalsUpdate messages to the client
     */
    @Override
    public void streamVitals(VitalsRequest request, StreamObserver<VitalsUpdate> responseObserver) {
        String patientId = request.getPatientId();

        try {
            // Validate the provided patient ID
            ValidationUtils.validatePatientId(patientId);

            // Log the start of service
            LoggingUtils.logServiceStart(logger, "MonitoringService", patientId);

            // Stream vitals data for the requested duration
            for (int i = 0; i < request.getDurationSeconds(); i++) {
                VitalsUpdate update = generateVitalsUpdate();
                responseObserver.onNext(update);

                // Log each vitals update in detail
                LoggingUtils.logFine(logger, "Sent vitals for %s: HR=%d, SpO2=%.1f",
                    patientId, update.getHeartRate(), update.getOxygenLevel());

                // Sleep for one second between updates
                Thread.sleep(1000);
            }

            // Complete the response stream
            responseObserver.onCompleted();
            LoggingUtils.logServiceEnd(logger, "MonitoringService", patientId, "Stream completed");

        } catch (IllegalArgumentException e) {
            // Handle invalid patient ID
            LoggingUtils.logError(logger, "MonitoringService", patientId, e, true);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (InterruptedException e) {
            // Handle interruption gracefully and reset the thread state
            LoggingUtils.logError(logger, "MonitoringService", patientId, e, false);
            Thread.currentThread().interrupt();
        }
    }
}

package distsys.smartmed.server;

import com.healthcare.grpc.monitoring.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

public class MonitoringServiceImpl extends MonitoringServiceGrpc.MonitoringServiceImplBase {
    private static final Logger logger = Logger.getLogger(MonitoringServiceImpl.class.getName());

    @Override
    public void streamVitals(VitalsRequest request, StreamObserver<VitalsUpdate> responseObserver) {
        String patientId = request.getPatientId();
        
        try {
            // Added validation
            ValidationUtils.validatePatientId(patientId);
            
            int duration = request.getDurationSeconds();
            logger.info(String.format(
                "Starting vitals stream for patient %s (duration: %ds)", 
                patientId, duration));

            for (int i = 0; i < duration; i++) {
                VitalsUpdate update = VitalsUpdate.newBuilder()
                    .setHeartRate(70 + (int)(Math.random() * 10))
                    .setOxygenLevel(95 + (float)(Math.random() * 5))
                    .setTimestamp(System.currentTimeMillis())
                    .build();

                responseObserver.onNext(update);
                logger.fine(String.format(
                    "Sent vitals update for %s: HR=%d, SpO2=%.1f", 
                    patientId, 
                    update.getHeartRate(), 
                    update.getOxygenLevel()));
                
                Thread.sleep(1000);
            }
            logger.info("Completed vitals stream for: " + patientId);
            
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid vitals request: " + e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException());
            return;
        } catch (InterruptedException e) {
            logger.warning("Vitals stream interrupted for: " + patientId);
            Thread.currentThread().interrupt();
        } finally {
            responseObserver.onCompleted();
        }
    }
}
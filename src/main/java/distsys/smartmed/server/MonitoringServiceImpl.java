package distsys.smartmed.server;

import com.healthcare.grpc.monitoring.*;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import distsys.smartmed.common.LoggingUtils;


public class MonitoringServiceImpl extends MonitoringServiceGrpc.MonitoringServiceImplBase {
    private static final Logger logger = Logger.getLogger(MonitoringServiceImpl.class.getName());

    private VitalsUpdate generateVitalsUpdate() {
    return VitalsUpdate.newBuilder()
        .setHeartRate(70 + (int)(Math.random() * 10))  // 70-80 bpm
        .setOxygenLevel(95 + (float)(Math.random() * 5))  // 95-100%
        .setTimestamp(System.currentTimeMillis())
        .build();
}
    
@Override
public void streamVitals(VitalsRequest request, StreamObserver<VitalsUpdate> responseObserver) {
    String patientId = request.getPatientId();
    
    try {
        ValidationUtils.validatePatientId(patientId);
        LoggingUtils.logServiceStart(logger, "MonitoringService", patientId);

        for (int i = 0; i < request.getDurationSeconds(); i++) {
            VitalsUpdate update = generateVitalsUpdate();
            responseObserver.onNext(update);
            
            LoggingUtils.logFine(logger, "Sent vitals for %s: HR=%d, SpO2=%.1f",
                patientId, update.getHeartRate(), update.getOxygenLevel());
            
            Thread.sleep(1000);
        }

        responseObserver.onCompleted();
        LoggingUtils.logServiceEnd(logger, "MonitoringService", patientId, "Stream completed");

    } catch (IllegalArgumentException e) {
        LoggingUtils.logError(logger, "MonitoringService", patientId, e, true);
        responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (InterruptedException e) {
        LoggingUtils.logError(logger, "MonitoringService", patientId, e, false);
        Thread.currentThread().interrupt();
    }
}    
}
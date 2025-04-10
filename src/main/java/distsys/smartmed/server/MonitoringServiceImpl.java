/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 *
 * @author anukratimehta
 */

import com.healthcare.grpc.monitoring.*;
import io.grpc.stub.StreamObserver;

public class MonitoringServiceImpl extends MonitoringServiceGrpc.MonitoringServiceImplBase {
    @Override
    public void streamVitals(VitalsRequest request, StreamObserver<VitalsUpdate> responseObserver) {
        String patientId = request.getPatientId();
        int duration = request.getDurationSeconds();
        System.out.println("[SmartMed] Streaming vitals for patient: " + patientId);

        try {
            for (int i = 0; i < duration; i++) {
                VitalsUpdate update = VitalsUpdate.newBuilder()
                    .setHeartRate(70 + (int)(Math.random() * 10))
                    .setOxygenLevel(95 + (float)(Math.random() * 5))
                    .setTimestamp(System.currentTimeMillis())
                    .build();

                responseObserver.onNext(update);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.err.println("[SmartMed] Vitals streaming interrupted: " + e.getMessage());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
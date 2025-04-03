/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.client;

/**
 *
 * @author anukratimehta
 */

import com.healthcare.grpc.consultation.*;
import com.healthcare.grpc.diagnostic.*;
import com.healthcare.grpc.monitoring.*;
import com.healthcare.grpc.patient.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SmartMedClient {
    private final ManagedChannel channel;
    private final PatientServiceGrpc.PatientServiceBlockingStub patientStub;
    private final MonitoringServiceGrpc.MonitoringServiceStub monitoringStub;
    private final DiagnosticServiceGrpc.DiagnosticServiceStub diagnosticStub;
    private final ConsultationServiceGrpc.ConsultationServiceStub consultationStub;

    public SmartMedClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext() // For testing only (no TLS)
            .build();
        this.patientStub = PatientServiceGrpc.newBlockingStub(channel);
        this.monitoringStub = MonitoringServiceGrpc.newStub(channel);
        this.diagnosticStub = DiagnosticServiceGrpc.newStub(channel);
        this.consultationStub = ConsultationServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // 1. Test Simple RPC (Patient Records)
    public void testPatientService(String patientId) {
        System.out.println("\n=== Testing Patient Service ===");
        PatientRequest request = PatientRequest.newBuilder()
            .setPatientId(patientId)
            .build();

        PatientResponse response = patientStub.getPatientRecord(request);
        System.out.println("Patient Record Received:");
        System.out.println("ID: " + response.getPatientId());
        System.out.println("Name: " + response.getName());
        System.out.println("Medication: " + response.getCurrentMedication());
        System.out.println("Medical History: " + response.getMedicalHistoryList());
    }

    // 2. Test Server Streaming (Vitals Monitoring)
    public void testMonitoringService(String patientId, int duration) throws InterruptedException {
        System.out.println("\n=== Testing Monitoring Service ===");
        CountDownLatch latch = new CountDownLatch(1);

        VitalsRequest request = VitalsRequest.newBuilder()
            .setPatientId(patientId)
            .setDurationSeconds(duration)
            .build();

        monitoringStub.streamVitals(request, new StreamObserver<VitalsUpdate>() {
            @Override
            public void onNext(VitalsUpdate update) {
                System.out.printf("Vitals Update [%tT]: HR=%d, SpO2=%.1f%%\n",
                    update.getTimestamp(),
                    update.getHeartRate(),
                    update.getOxygenLevel());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Monitoring Error: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Monitoring completed");
                latch.countDown();
            }
        });

        latch.await();
    }

    // 3. Test Client Streaming (Medical Imaging)
//    public void testDiagnosticService() throws InterruptedException {
//        System.out.println("\n=== Testing Diagnostic Service ===");
//        CountDownLatch latch = new CountDownLatch(1);
//
//        StreamObserver<ImageChunk> requestObserver = diagnosticStub.uploadMedicalImage(
//            new StreamObserver<UploadStatus>() {
//                @Override
//                public void onNext(UploadStatus status) {
//                    System.out.println("Upload Status: " + status.getMessage());
//                    System.out.println("Image ID: " + status.getImageId());
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    System.err.println("Upload Failed: " + t.getMessage());
//                    latch.countDown();
//                }
//
//                @Override
//                public void onCompleted() {
//                    System.out.println("Upload completed");
//                    latch.countDown();
//                }
//            });
//
//        // Simulate sending 5 chunks
//        String imageId = "img-" + System.currentTimeMillis();
//        for (int i = 1; i <= 5; i++) {
//            ImageChunk chunk = ImageChunk.newBuilder()
//                .setImageId(imageId)
//                .setChunkNumber(i)
//                .setImageData(("chunk-data-" + i).getBytes())
//                .build();
//            requestObserver.onNext(chunk);
//            Thread.sleep(500); // Simulate delay between chunks
//        }
//        requestObserver.onCompleted();
//        latch.await();
//    }
//
//    // 4. Test Bi-directional Streaming (Consultation)
//    public void testConsultationService() throws InterruptedException {
//        System.out.println("\n=== Testing Consultation Service ===");
//        CountDownLatch latch = new CountDownLatch(1);
//
//        StreamObserver<ConsultationMessage> requestObserver = 
//            consultationStub.liveConsultation(new StreamObserver<ConsultationMessage>() {
//                @Override
//                public void onNext(ConsultationMessage message) {
//                    System.out.println("AI Doctor: " + message.getText());
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    System.err.println("Consultation Error: " + t.getMessage());
//                    latch.countDown();
//                }
//
//                @Override
//                public void onCompleted() {
//                    System.out.println("Consultation ended");
//                    latch.countDown();
//                }
//            });
//
//        // Send 3 messages
//        String[] patientMessages = {
//            "Hello, I have a headache",
//            "It's been persistent for 2 days",
//            "What should I do?"
//        };
//
//        for (String msg : patientMessages) {
//            ConsultationMessage message = ConsultationMessage.newBuilder()
//                .setSenderId("patient-123")
//                .setText(msg)
//                .setTimestamp(System.currentTimeMillis())
//                .build();
//            requestObserver.onNext(message);
//            Thread.sleep(1000);
//        }
//        
//        requestObserver.onCompleted();
//        latch.await();
//    }

    public static void main(String[] args) throws Exception {
        SmartMedClient client = new SmartMedClient("localhost", 50051);
        
        try {
            // Test all services
            client.testPatientService("patient-123");
            client.testMonitoringService("patient-123", 5); // 5 seconds of vitals
//            client.testDiagnosticService();
//            client.testConsultationService();
        } finally {
            client.shutdown();
        }
    }
}

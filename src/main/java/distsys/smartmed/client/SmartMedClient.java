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
import com.healthcare.grpc.monitoring.*;
import com.healthcare.grpc.patient.*;
import com.healthcare.grpc.medication.*;
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
    private final ConsultationServiceGrpc.ConsultationServiceStub consultationStub;
    private final MedicationServiceGrpc.MedicationServiceStub medicationStub;

    public SmartMedClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext() // For testing only (no TLS)
            .build();
        this.patientStub = PatientServiceGrpc.newBlockingStub(channel);
        this.monitoringStub = MonitoringServiceGrpc.newStub(channel);
        this.consultationStub = ConsultationServiceGrpc.newStub(channel);
        this.medicationStub = MedicationServiceGrpc.newStub(channel);
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
        System.out.println("Age: " + response.getAge());
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

//3. Test Client Stream (Medication)
public void testMedicationService(String patientId) throws InterruptedException {
    System.out.println("\n=== Testing Medication Service ===");
    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<MedicationRecord> requestObserver = medicationStub.analyzeMedicationSchedule(
        new StreamObserver<MedicationAnalysis>() {
            @Override
            public void onNext(MedicationAnalysis analysis) {
                System.out.println("\n=== Server Analysis Received ===");
                System.out.printf("Adherence: %.1f%%\n", analysis.getAdherencePercentage());
                System.out.println("Doses Taken: " + analysis.getTakenDoses() + 
                                 "/" + analysis.getTotalDoses());
                System.out.println("Summary: " + analysis.getSummary());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Medication analysis completed");
                latch.countDown();
            }
        });

    // Simulate medication records
    MedicationRecord record1 = MedicationRecord.newBuilder()
        .setPatientId(patientId)
        .setMedicationName("Ibuprofen")
        .setDosageMg(400)
        .setScheduledTime("08:00")
        .setWasTaken(true)
        .setActualTimeTaken("08:23")
        .build();
        
    MedicationRecord record2 = MedicationRecord.newBuilder()
        .setPatientId(patientId)
        .setMedicationName("Ibuprofen")
        .setDosageMg(400)
        .setScheduledTime("20:00")
        .setWasTaken(false)
        .setActualTimeTaken("")
        .build();
        
    MedicationRecord record3 = MedicationRecord.newBuilder()
        .setPatientId(patientId)
        .setMedicationName("Vitamin D")
        .setDosageMg(1000)
        .setScheduledTime("12:00")
        .setWasTaken(true)
        .setActualTimeTaken("12:47")
        .build();

    requestObserver.onNext(record1);
    requestObserver.onNext(record2);
    requestObserver.onNext(record3);
    
    requestObserver.onCompleted();
    latch.await();
}
    private String getCurrentTimestamp() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }


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
            client.testPatientService("12");
            client.testMonitoringService("12", 5); // 5 seconds of vitals
        client.testMedicationService("12");
//            client.testConsultationService();
        } finally {
            client.shutdown();
        }
    }
}

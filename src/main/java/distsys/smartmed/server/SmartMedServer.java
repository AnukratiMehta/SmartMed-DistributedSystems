/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 *
 * @author anukratimehta
 */

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class SmartMedServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;
        Server server = ServerBuilder.forPort(port)
            .addService(new PatientServiceImpl())
            .addService(new MonitoringServiceImpl())
            .addService(new MedicationServiceImpl())
            .addService(new ConsultationServiceImpl())
            .build();

        server.start();
        System.out.println("[SmartMed] Server started on port " + port);
        System.out.println("[SmartMed] Services registered:");
        System.out.println("- Patient Records");
        System.out.println("- Vitals Monitoring"); 
        System.out.println("- Medication Management");
        System.out.println("- Live Consultation");

        server.awaitTermination();
    }
}

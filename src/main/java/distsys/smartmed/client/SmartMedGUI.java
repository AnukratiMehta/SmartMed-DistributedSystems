/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package distsys.smartmed.client;

/**
 *
 * @author anukratimehta
 */
import com.healthcare.grpc.monitoring.*;
import com.healthcare.grpc.patient.*;
import com.healthcare.grpc.medication.*;
import com.healthcare.grpc.rehab.*;
import com.healthcare.grpc.auth.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import distsys.smartmed.security.JwtClientInterceptor;
import distsys.smartmed.security.JwtUtil;
import distsys.smartmed.common.ValidationUtils;
import io.grpc.StatusRuntimeException;


public class SmartMedGUI extends javax.swing.JFrame {

    private ManagedChannel channel;
    private String jwtToken;

    public SmartMedGUI() {
        initComponents();
        initializeGRPCChannel();
        idField.requestFocusInWindow();
    }
    
    private boolean performLogin() {
        try {
            // First create unauthenticated channel
            ManagedChannel tempChannel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
            
            AuthServiceGrpc.AuthServiceBlockingStub authStub = AuthServiceGrpc.newBlockingStub(tempChannel);
            
            LoginResponse response = authStub.login(
                LoginRequest.newBuilder()
                    .setUsername("admin")
                    .setPassword("smartmed123")
                    .build());
            
            this.jwtToken = response.getToken();
            resultArea.append("Login successful! Welcome admin :)\n");
            tempChannel.shutdown();
            return true;
        } catch (Exception e) {
            resultArea.append("Login failed: " + e.getMessage() + "\n");
            // Fallback to default token if login fails
            this.jwtToken = JwtUtil.generateToken("fallback-user");
            return false;
        }
    }

    private void initializeGRPCChannel() {
        performLogin(); // Get token first
        
        // Create authenticated channel
        this.channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .intercept(new JwtClientInterceptor(jwtToken))
            .usePlaintext()
            .build();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        patientBtn = new javax.swing.JButton();
        monitoringBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultArea = new javax.swing.JTextArea();
        medicationBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        rehabBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        patientBtn.setText("Patient Records");
        patientBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patientBtnActionPerformed(evt);
            }
        });

        monitoringBtn.setText("Monitor Vitals");
        monitoringBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monitoringBtnActionPerformed(evt);
            }
        });

        resultArea.setColumns(20);
        resultArea.setRows(5);
        jScrollPane1.setViewportView(resultArea);

        medicationBtn.setText("Track Medicines");
        medicationBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                medicationBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("Patient Id:");

        rehabBtn.setText("Start Rehab");
        rehabBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rehabBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(medicationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(patientBtn))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(idField)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 89, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rehabBtn)
                            .addComponent(monitoringBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monitoringBtn)
                    .addComponent(patientBtn))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(medicationBtn)
                    .addComponent(rehabBtn))
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void log(String message) {
        resultArea.append(message + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }
    
    private void logClientError(String context, Throwable t) {
    String message;
    if (t instanceof StatusRuntimeException) {
        message = ((StatusRuntimeException) t).getStatus().getDescription();
    } else {
        message = t.getMessage();
    }
    resultArea.append(String.format("[ERROR] %s: %s\n", context, message));
}

    private void patientBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patientBtnActionPerformed
        // TODO add your handling code here:
        String patientId = idField.getText().trim();
    
    if (!ValidationUtils.isValidPatientId(patientId)) {
        log("\nError: Patient ID must be between " + 
            ValidationUtils.PATIENT_ID_MIN + "-" + 
            ValidationUtils.PATIENT_ID_MAX);
        return;
    }

    new Thread(() -> {
        try {
            log("\n===Fetching record for patient: " + patientId + "===\n");
            PatientResponse response = PatientServiceGrpc.newBlockingStub(channel)
                .getPatientRecord(PatientRequest.newBuilder()
                    .setPatientId(patientId)
                    .build());

            log("Name: " + response.getName());
            log("Age: " + response.getAge());
            log("Medication: " + response.getCurrentMedication());
            log("History: " + response.getMedicalHistoryList());

        } catch (StatusRuntimeException e) {
            logClientError("Patient Records", e);
        } catch (Exception e) {
            logClientError("Unexpected Error", e);
        }
    }).start();
    }//GEN-LAST:event_patientBtnActionPerformed

    private void monitoringBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monitoringBtnActionPerformed
        // TODO add your handling code here:
       String patientId = idField.getText().trim();
    
    if (!ValidationUtils.isValidPatientId(patientId)) {
        log("\nError: Patient ID must be between " + 
            ValidationUtils.PATIENT_ID_MIN + "-" + 
            ValidationUtils.PATIENT_ID_MAX);
        return;
    }

    new Thread(() -> {
        try {
            log("\n=== Starting Vitals Monitoring for Patient " + patientId + "===\n");
            
            MonitoringServiceGrpc.MonitoringServiceStub stub = 
                MonitoringServiceGrpc.newStub(channel);

            stub.streamVitals(
                VitalsRequest.newBuilder()
                    .setPatientId(patientId)
                    .setDurationSeconds(10)
                    .build(),
                new StreamObserver<VitalsUpdate>() {
                    @Override
                    public void onNext(VitalsUpdate update) {
                        log(String.format("Heart Rate: %d | Oxygen: %.1f%% | Time: %tT",
                            update.getHeartRate(),
                            update.getOxygenLevel(),
                            update.getTimestamp()));
                    }

                    @Override
                    public void onError(Throwable t) {
                        logClientError("Vitals Monitoring", t);
                    }

                    @Override
                    public void onCompleted() {
                        log("\nMonitoring session ended\n");
                    }
                });

            Thread.sleep(10000); // Match 10s duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logClientError("Monitoring Setup", e);
        }
    }).start();
    }//GEN-LAST:event_monitoringBtnActionPerformed

    private void medicationBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_medicationBtnActionPerformed
        // TODO add your handling code here:
   String patientId = idField.getText().trim();
    
    if (!ValidationUtils.isValidPatientId(patientId)) {
        log("\nError: Patient ID must be between 1-100");
        return;
    }

    new Thread(() -> {
        try {
            StreamObserver<MedicationRecord> requestObserver = 
                MedicationServiceGrpc.newStub(channel).analyzeMedicationSchedule(
                    new StreamObserver<MedicationAnalysis>() {
                        @Override
                        public void onNext(MedicationAnalysis analysis) {
                            log("\n=== MEDICATION ANALYSIS ===");
                            log(String.format("Adherence: %.1f%%", analysis.getAdherencePercentage()));
                            log("Taken: %d/%d doses".formatted(
                                analysis.getTakenDoses(), 
                                analysis.getTotalDoses()));
                            log("Summary: " + analysis.getSummary());
                        }

                        @Override
                        public void onError(Throwable t) {
                            logClientError("Medication Analysis", t);
                        }

                        @Override
                        public void onCompleted() {
                            log("\nMedication analysis completed\n");
                        }
                    });

            List<MedicationRecord> records = generatePatientMedications(patientId, 
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            
            log("\n=== Streaming Medication Records for Patient " + patientId + "===\n");
            for (MedicationRecord record : records) {
                requestObserver.onNext(record);
                log("Sent: " + record.getMedicationName() + 
                    " at " + record.getScheduledTime() + 
                    (record.getWasTaken() ? "— [taken]" : "— [missed]"));
                Thread.sleep(500); // Simulate real-time delay
            }

            requestObserver.onCompleted();
            Thread.sleep(1000); // Wait for final analysis
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logClientError("Medication Tracking", e);
        }
    }).start();
}

private List<MedicationRecord> generatePatientMedications(String patientId, String currentTime) {
    List<MedicationRecord> records = new ArrayList<>();
    Random rand = new Random(patientId.hashCode());
    
    try {
        // Parse current time
        LocalTime now = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm"));
        
        // Get patient's medication profile
        String[][] medicationProfile = getPatientMedicationProfile(patientId);
        
        // First pass: Collect all eligible medications
        List<MedicationRecord> allEligible = new ArrayList<>();
        
        for (String[] med : medicationProfile) {
            String[] times = med[2].split(",");
            for (String time : times) {
                LocalTime scheduledTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
                
                if (!scheduledTime.isAfter(now)) {
                    boolean taken = rand.nextFloat() > 0.3; // 70% chance taken
                    String actualTime = taken ? 
                        scheduledTime.plusMinutes(rand.nextInt(61) - 30) // ±30 min variation
                            .format(DateTimeFormatter.ofPattern("HH:mm")) : "";
                    
                    allEligible.add(MedicationRecord.newBuilder()
                        .setPatientId(patientId)
                        .setMedicationName(med[0])
                        .setDosageMg(Float.parseFloat(med[1]))
                        .setScheduledTime(time)
                        .setWasTaken(taken)
                        .setActualTimeTaken(actualTime)
                        .build());
                }
            }
        }
        
        // Sort by scheduled time
        Collections.sort(allEligible, new Comparator<MedicationRecord>() {
            @Override
            public int compare(MedicationRecord a, MedicationRecord b) {
                return a.getScheduledTime().compareTo(b.getScheduledTime());
            }
        });
        
        // Use eligible records, ensuring no duplicates
        records = new ArrayList<>(allEligible);
        
        // If we need more records to reach minimum of 3, add fallback records
        if (records.size() < 3) {
            // Simple fallback medications
            String[][] fallbackMeds = {
                {"Vitamin C", "500", "06:00"},
                {"Vitamin B12", "500", "07:00"},
                {"Iron", "65", "08:00"},
                {"Omega-3", "1000", "12:00"},
                {"Folic Acid", "400", "09:00"},
                {"Vitamin E", "400", "10:00"},
                {"Biotin", "300", "11:00"}
            };
            
            for (String[] med : fallbackMeds) {
                if (records.size() >= 3) break;
                
                // Create a new record
                boolean taken = rand.nextBoolean();
                String time = med[2];
                String actualTime = taken ? 
                    LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                        .plusMinutes(rand.nextInt(31))
                        .format(DateTimeFormatter.ofPattern("HH:mm")) : "";
                
                MedicationRecord newRecord = MedicationRecord.newBuilder()
                    .setPatientId(patientId)
                    .setMedicationName(med[0])
                    .setDosageMg(Float.parseFloat(med[1]))
                    .setScheduledTime(time)
                    .setWasTaken(taken)
                    .setActualTimeTaken(actualTime)
                    .build();
                
                // Simple check to avoid adding exactly the same record
                boolean isDuplicate = false;
                for (MedicationRecord existingRecord : records) {
                    if (existingRecord.getMedicationName().equals(newRecord.getMedicationName()) &&
                        existingRecord.getScheduledTime().equals(newRecord.getScheduledTime())) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                if (!isDuplicate) {
                    records.add(newRecord);
                }
            }
        }
        
        // Limit to maximum 10 records
        if (records.size() > 10) {
            records = records.subList(0, 10);
        }
        
        // Re-sort after any modifications
        Collections.sort(records, new Comparator<MedicationRecord>() {
            @Override
            public int compare(MedicationRecord a, MedicationRecord b) {
                return a.getScheduledTime().compareTo(b.getScheduledTime());
            }
        });
        
    } catch (Exception e) {
        System.err.println("Error generating medications: " + e.getMessage());
    }
    
    return records;
}

private String[][] getPatientMedicationProfile(String patientId) {
    int idHash = Math.abs(patientId.hashCode());

    switch (idHash % 5) {
        case 0: // Morning/Evening meds
            return new String[][]{
                {"Ibuprofen", "400", "07:30,19:30"},
                {"Omeprazole", "20", "07:15"},
                {"Levothyroxine", "75", "06:45"},
                {"Losartan", "50", "08:00"},
                {"Furosemide", "40", "07:00"},
                {"Metoprolol", "100", "08:15,20:15"},
                {"Glimepiride", "1", "07:45"},
                {"Ranitidine", "150", "21:00"}
            };

        case 1: // Three-times daily
            return new String[][]{
                {"Metformin", "500", "06:45,13:15,19:00"},
                {"Cefuroxime", "250", "07:30,14:00,20:00"},
                {"Paracetamol", "650", "08:00,15:00,22:00"},
                {"Salbutamol", "4", "06:00,12:00,18:00"},
                {"Pantoprazole", "40", "07:00"},
                {"Aspirin", "75", "09:30"},
                {"Telmisartan", "40", "10:00"},
                {"Esomeprazole", "20", "07:30,19:30"}
            };

        case 2: // Four times daily
            return new String[][]{
                {"Amoxicillin", "500", "06:00,12:00,18:00,00:00"},
                {"Theophylline", "200", "05:30,11:30,17:30,23:30"},
                {"Doxycycline", "100", "08:30,14:30,20:30,02:30"},
                {"Linezolid", "600", "06:15,12:15,18:15,00:15"},
                {"Cetirizine", "10", "07:45"},
                {"Ciprofloxacin", "500", "06:30,14:30,22:30"},
                {"Famotidine", "20", "09:00,21:00"},
                {"Budesonide", "200", "05:45,11:45,17:45,23:45"}
            };

        case 3: // Evening focused
            return new String[][]{
                {"Atorvastatin", "20", "21:00"},
                {"Melatonin", "3", "22:30"},
                {"Zolpidem", "10", "23:15"},
                {"Clonazepam", "0.5", "21:45"},
                {"Trazodone", "50", "22:15"},
                {"Ramipril", "10", "20:00"},
                {"Olmesartan", "20", "18:30"},
                {"Hydralazine", "25", "19:30,23:00"}
            };

        default: // Morning focused
            return new String[][]{
                {"Lisinopril", "10", "07:00"},
                {"Amlodipine", "5", "07:30"},
                {"Insulin Glargine", "10", "06:00"},
                {"Atenolol", "50", "08:30"},
                {"Hydrochlorothiazide", "25", "09:00"},
                {"Bisoprolol", "5", "07:15"},
                {"Duloxetine", "30", "08:00"},
                {"Enalapril", "10", "07:45"}
            };
    }



    }//GEN-LAST:event_medicationBtnActionPerformed

    private void rehabBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rehabBtnActionPerformed
        // TODO add your handling code here:
    String patientId = idField.getText().trim();
    
    if (!ValidationUtils.isValidPatientId(patientId)) {
        log("\nError: Patient ID must be between 1-100");
        return;
    }

    new Thread(() -> {
        try {
            log("\n=== Starting Rehab Session for Patient " + patientId + "===\n");

            StreamObserver<ExerciseInput> requestObserver = 
                RehabServiceGrpc.newStub(channel).liveExerciseFeedback(
                    new StreamObserver<ExerciseFeedback>() {
                        @Override
                        public void onNext(ExerciseFeedback feedback) {
                            if (feedback.getRepetitionNumber() == 0) {
                                log("\n=== FINAL REPORT ===");
                                log(feedback.getMessage());
                            } else {
                                log(String.format("[Rep %d] %s: %s", 
                                    feedback.getRepetitionNumber(),
                                    feedback.getSeverity().toUpperCase(),
                                    feedback.getMessage()));
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            logClientError("Rehab Feedback", t);
                        }

                        @Override
                        public void onCompleted() {
                            log("Rehab session ended");
                        }
                    });

            String[] exercises = {"Squat", "Lunge", "Leg Raise"};
            String exercise = exercises[new Random().nextInt(exercises.length)];
            
            log("Performing 10 reps of: " + exercise);
            for (int i = 1; i <= 10; i++) {
                double angle = 20 + new Random().nextDouble() * 30; // 20-50°
                
                requestObserver.onNext(ExerciseInput.newBuilder()
                    .setPatientId(patientId)
                    .setExerciseName(exercise)
                    .setRepetitionNumber(i)
                    .setPostureAngle(angle)
                    .build());
                
                log("\nSent rep " + i + " - Angle: " + String.format("%.1f°", angle));
                Thread.sleep(1000); // Realistic delay between reps
            }

            requestObserver.onCompleted();
            Thread.sleep(500); // Wait for final report
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logClientError("Rehab Session", e);
        }
    }).start();
    }//GEN-LAST:event_rehabBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SmartMedGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SmartMedGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SmartMedGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SmartMedGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SmartMedGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SmartMedGUI().setVisible(true);
            }
        });
    }
    

private String getCurrentTimestamp() {
    return java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}

 @Override
    public void dispose() {
        if (channel != null) {
            channel.shutdown();
        }
        super.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton medicationBtn;
    private javax.swing.JButton monitoringBtn;
    private javax.swing.JButton patientBtn;
    private javax.swing.JButton rehabBtn;
    private javax.swing.JTextArea resultArea;
    // End of variables declaration//GEN-END:variables
}

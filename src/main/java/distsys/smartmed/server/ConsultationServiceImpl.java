package distsys.smartmed.server;

import com.healthcare.grpc.consultation.*;
import io.grpc.stub.StreamObserver;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ConsultationServiceImpl extends ConsultationServiceGrpc.ConsultationServiceImplBase {

    private static class ConsultationState {
        String currentSymptom = "";
        int questionCount = 0;
        boolean diagnosisGiven = false;
        Map<String, String> patientAnswers = new HashMap<>();
    }

    @Override
    public StreamObserver<ConsultationMessage> liveConsultation(
        StreamObserver<ConsultationMessage> responseObserver) {
        
        return new ConsultationStreamObserver(responseObserver);
    }

    private class ConsultationStreamObserver implements StreamObserver<ConsultationMessage> {
        private final ConsultationState state = new ConsultationState();
        private final StreamObserver<ConsultationMessage> responseObserver;

        public ConsultationStreamObserver(StreamObserver<ConsultationMessage> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        public void onNext(ConsultationMessage message) {
            String patientMsg = message.getText().toLowerCase();
            String response = generateAIResponse(patientMsg);
            
            simulateTypingDelay();
            
            ConsultationMessage reply = ConsultationMessage.newBuilder()
                .setSenderId("doctor-ai")
                .setText(response)
                .setTimestamp(getCurrentTimestamp())
                .build();
            
            responseObserver.onNext(reply);
        }

        @Override
        public void onError(Throwable t) {
            System.err.println("Consultation error: " + t.getMessage());
        }

        @Override
        public void onCompleted() {
            responseObserver.onCompleted();
        }

        private String generateAIResponse(String patientMsg) {
            if (state.questionCount > 0) {
                state.patientAnswers.put("q" + state.questionCount, patientMsg);
            }

            if (state.currentSymptom.isEmpty()) {
                return handleInitialSymptomDetection(patientMsg);
            }
            
            state.questionCount++;
            switch (state.currentSymptom) {
                case "headache": return handleHeadacheConversation();
                case "fever": return handleFeverConversation();
                case "cold": return handleColdConversation();
                default: return "Can you tell me more about how you're feeling?";
            }
        }

        private String handleInitialSymptomDetection(String patientMsg) {
            if (containsAny(patientMsg, "headache", "head pain", "migraine")) {
                state.currentSymptom = "headache";
                return "I understand you're experiencing headaches. How would you describe the pain? " +
                       "(e.g., throbbing, constant, sharp)";
            }
            else if (containsAny(patientMsg, "fever", "temperature", "hot")) {
                state.currentSymptom = "fever";
                return "You mentioned a fever. What is your current temperature in Fahrenheit?";
            }
            else if (containsAny(patientMsg, "cough", "sneeze", "cold", "congested")) {
                state.currentSymptom = "cold";
                return "You seem to have cold symptoms. Is your cough dry or productive with phlegm?";
            }
            return "Could you describe your primary symptom or concern in more detail?";
        }

        private String handleHeadacheConversation() {
            switch (state.questionCount) {
                case 1: return "How long have you had this headache? (hours/days)";
                case 2: return "Have you taken any medication for it? If yes, what and how much?";
                case 3: return "Does the pain get worse with movement or light sensitivity?";
                case 4: 
                    state.diagnosisGiven = true;
                    String diagnosis = assessHeadacheType();
                    return "Based on your answers, this sounds like " + diagnosis + ". " +
                           "I recommend " + getHeadacheAdvice(diagnosis) + 
                           "\n\nIs there anything else you'd like to discuss?";
                default: return wrapUpConversation();
            }
        }

        private String handleFeverConversation() {
            switch (state.questionCount) {
                case 1: return "How long have you had this fever?";
                case 2: return "Are you experiencing any additional symptoms? " +
                              "(e.g., chills, sweating, body aches)";
                case 3: return "Have you taken any fever-reducing medication? " +
                              "(e.g., acetaminophen, ibuprofen)";
                case 4: 
                    state.diagnosisGiven = true;
                    String temp = state.patientAnswers.getOrDefault("q1", "");
                    String duration = state.patientAnswers.getOrDefault("q2", "");
                    String additional = state.patientAnswers.getOrDefault("q3", "");
                    
                    StringBuilder advice = new StringBuilder("Based on your ");
                    advice.append(temp.contains("1") ? "high fever" : "elevated temperature");
                    
                    if (containsAny(duration, "day", "days")) {
                        advice.append(" lasting several days");
                    }
                    
                    if (containsAny(additional, "chills", "ache")) {
                        advice.append(" with systemic symptoms");
                    }
                    
                    advice.append(", this may indicate ")
                          .append(assessFeverSeverity(temp, duration))
                          .append(". ")
                          .append(getFeverAdvice(temp));
                    
                    return advice.toString();
                default: return wrapUpConversation();
            }
        }

        private String handleColdConversation() {
            switch (state.questionCount) {
                case 1: return "How long have you had these symptoms?";
                case 2: return "Are you experiencing any of the following? " +
                              "(fever, shortness of breath, loss of taste/smell)";
                case 3: return "Have you tried any medications or home remedies?";
                case 4: 
                    state.diagnosisGiven = true;
                    String coughType = state.patientAnswers.getOrDefault("q1", "");
                    String duration = state.patientAnswers.getOrDefault("q2", "");
                    String additional = state.patientAnswers.getOrDefault("q3", "");
                    
                    StringBuilder diagnosis = new StringBuilder();
                    if (containsAny(additional, "fever", "breath")) {
                        diagnosis.append("possible respiratory infection");
                    } else if (containsAny(coughType, "productive", "phlegm")) {
                        diagnosis.append("likely bronchitis");
                    } else {
                        diagnosis.append("viral upper respiratory infection");
                    }
                    
                    return "Based on your symptoms, this appears to be " + diagnosis.toString() + ". " +
                           getColdAdvice(coughType, additional) + 
                           "\n\nWould you like me to suggest over-the-counter medications?";
                default: return wrapUpConversation();
            }
        }

        // Helper methods
        private String assessHeadacheType() {
            String painType = state.patientAnswers.getOrDefault("q1", "");
            String duration = state.patientAnswers.getOrDefault("q2", "");
            
            if (containsAny(painType, "throbbing", "one side") && 
                containsAny(duration, "day", "days")) {
                return "a migraine";
            } else if (containsAny(painType, "pressure", "tight")) {
                return "a tension headache";
            }
            return "a common headache";
        }

        private String assessFeverSeverity(String temp, String duration) {
            try {
                float tempF = Float.parseFloat(temp.replaceAll("[^0-9.]", ""));
                if (tempF > 103) return "a potentially serious infection";
                if (tempF > 101) return "a moderate infection";
                return "a mild viral illness";
            } catch (NumberFormatException e) {
                return "a febrile illness";
            }
        }

        private String getHeadacheAdvice(String diagnosis) {
            switch (diagnosis) {
                case "a migraine": return "resting in a dark room, hydration, and considering prescription migraine medication";
                case "a tension headache": return "applying a warm compress, gentle neck stretches, and stress reduction techniques";
                default: return "400mg ibuprofen every 6 hours as needed, and ensuring proper hydration";
            }
        }

        private String getFeverAdvice(String temp) {
            try {
                float tempF = Float.parseFloat(temp.replaceAll("[^0-9.]", ""));
                StringBuilder advice = new StringBuilder("I recommend ");
                
                if (tempF > 102) {
                    advice.append("taking acetaminophen every 4-6 hours, ")
                          .append("staying well hydrated, and ");
                }
                
                advice.append("monitoring your temperature regularly. ");
                
                if (tempF > 103) {
                    advice.append("If it persists beyond 72 hours or reaches 104°F, ")
                          .append("please seek immediate medical attention.");
                } else {
                    advice.append("If symptoms worsen or persist beyond 3 days, ")
                          .append("consult your physician.");
                }
                
                return advice.toString();
            } catch (NumberFormatException e) {
                return "I recommend monitoring your temperature and staying hydrated. " +
                       "If symptoms persist, consult your doctor.";
            }
        }

        private String getColdAdvice(String coughType, String additional) {
            StringBuilder advice = new StringBuilder("I suggest ");
            
            if (containsAny(additional, "fever")) {
                advice.append("taking fever reducers as needed, ");
            }
            
            if (containsAny(coughType, "dry")) {
                advice.append("using cough suppressants at night, ");
            } else if (containsAny(coughType, "productive")) {
                advice.append("using expectorants during the day, ");
            }
            
            advice.append("staying hydrated with warm fluids, ")
                  .append("and getting plenty of rest. ");
            
            if (containsAny(additional, "breath")) {
                advice.append("If you experience significant breathing difficulties, ")
                      .append("seek medical attention immediately.");
            }
            
            return advice.toString();
        }

        private String wrapUpConversation() {
            return "Thank you for the consultation. " + 
                   (state.diagnosisGiven ? "" : "Please monitor your symptoms. ") +
                   "Would you like me to schedule a follow-up or provide additional resources?";
        }

        private boolean containsAny(String input, String... terms) {
            for (String term : terms) {
                if (input.contains(term)) return true;
            }
            return false;
        }

        private void simulateTypingDelay() {
            try {
                Thread.sleep(500 + new Random().nextInt(1500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private String getCurrentTimestamp() {
            return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
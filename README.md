# SmartMed Healthcare System  
*Distributed Systems Project for Healthcare Automation*  

---

## **Project Files Overview**  

### **Core Files**  
| File | Purpose |  
|------|---------|  
| `SmartMedServer.java` | Main server hosting all gRPC services |  
| `SmartMedGUI.java` | **Primary GUI** for patient management (run this) |  
| `SmartMedClient.java` | Discovers services via JmDNS (optional) |  
| `AuthClient.java` | Tests login functionality (debug tool) |  

### **Services**  
| File | Functionality |  
|------|--------------|  
| `PatientServiceImpl.java` | Manages medical records |  
| `MonitoringServiceImpl.java` | Streams live vitals (heart rate/O₂) |  
| `MedicationServiceImpl.java` | Tracks medication adherence |  
| `RehabServiceImpl.java` | Provides rehab exercise feedback |  
| `AuthServiceImpl.java` | Handles JWT authentication |  

### **Supporting Files**  
| File | Role |  
|------|-----|  
| `JwtUtil.java` | Generates/validates JWT tokens |  
| `JwtServerInterceptor.java` | Authenticates gRPC calls |  
| `JwtClientInterceptor.java` | Attaches tokens to requests |  
| `ValidationUtils.java` | Validates patient IDs (1-100) |  
| `LoggingUtils.java` | Standardized logging format |  

---

## **Setup in NetBeans**  

### **1. Launch the Server**  
1. Open `SmartMedServer.java`  
2. Click **Run** ▶️ (starts on port `50051`)  

### **2. Start the GUI**  
1. Open `SmartMedGUI.java`  
2. Click **Run** ▶️  
3. **Default Credentials**: Auto-login as `admin` / `smartmed123`  

---

## **GUI Workflow**  
1. **Enter Patient ID** (1-100)  
2. Click:  
   - **Patient Records**: View history/medications  
   - **Monitor Vitals**: 10-second live stream  
   - **Track Medicines**: Adherence analysis  
   - **Start Rehab**: Posture-guided exercises   

---

## **Additional Tools**  
- **`SmartMedClient.java`**:  
  - Discovers services automatically via JmDNS  
  - *Note: Optional if using fixed `localhost` in GUI*  

- **`AuthClient.java`**:  
  - Tests login credentials (debug only)  
  - Usage: Run → Checks `admin` / `smartmed123`  

---

## **Troubleshooting**  
| Issue | Fix |  
|-------|-----|  
| Port conflict | Ensure no other app uses `50051` |  
| JWT errors | Restart GUI to regenerate token |  
| Missing data | Patient IDs **must** be 1-100 |  

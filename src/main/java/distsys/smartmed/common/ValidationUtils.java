/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.common;

/**
 * Utility class for validating patient IDs.
 * Provides methods for checking the validity of a given patient ID based on predefined minimum and maximum limits.
 * 
 * @author anukratimehta
 */
public class ValidationUtils {
    // Minimum and maximum allowed values for patient IDs
    public static final int PATIENT_ID_MIN = 1;
    public static final int PATIENT_ID_MAX = 100;

    /**
     * Checks whether the provided patient ID is valid by ensuring it is within the allowed range.
     * 
     * @param id the patient ID to validate
     * @return true if the patient ID is valid, false otherwise
     */
    public static boolean isValidPatientId(String id) {
        try {
            int num = Integer.parseInt(id);
            return num >= PATIENT_ID_MIN && num <= PATIENT_ID_MAX;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates the provided patient ID and throws an IllegalArgumentException if the ID is not valid.
     * 
     * @param id the patient ID to validate
     * @throws IllegalArgumentException if the patient ID is not within the allowed range
     */
    public static void validatePatientId(String id) throws IllegalArgumentException {
        if (!isValidPatientId(id)) {
            throw new IllegalArgumentException(
                String.format("Patient ID must be between %d-%d", PATIENT_ID_MIN, PATIENT_ID_MAX)
            );
        }
    }
}

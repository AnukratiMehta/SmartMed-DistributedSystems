/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.common;

/**
 *
 * @author anukratimehta
 */

public class ValidationUtils {
    public static final int PATIENT_ID_MIN = 1;
    public static final int PATIENT_ID_MAX = 100;

    public static boolean isValidPatientId(String id) {
        try {
            int num = Integer.parseInt(id);
            return num >= PATIENT_ID_MIN && num <= PATIENT_ID_MAX;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void validatePatientId(String id) throws IllegalArgumentException {
        if (!isValidPatientId(id)) {
            throw new IllegalArgumentException(
                String.format("Patient ID must be between %d-%d", PATIENT_ID_MIN, PATIENT_ID_MAX)
            );
        }
    }
}
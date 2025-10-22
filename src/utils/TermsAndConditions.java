package utils;
public class TermsAndConditions {
    
    public static void displayTermsAndConditions() {
        System.out.println("\n" + "=".repeat(75));
        System.out.println("                          TERMS AND CONDITIONS");
        System.out.println("=".repeat(75));
        System.out.println("| These Terms and Conditions govern the use of the STI Merch System.      |");
        System.out.println("| By creating an account, you ('Student') agree to comply with the        |");
        System.out.println("| following terms:                                                        |");
        System.out.println("|                                                                         |");
        System.out.println("| 1. Account Responsibility:                                              |");
        System.out.println("| - You are responsible for maintaining the confidentiality of your       |");
        System.out.println("|   account credentials.                                                  |");
        System.out.println("| - You must provide accurate and current information.                    |");
        System.out.println("| - Only one account per student is allowed.                              |");
        System.out.println("|                                                                         |");
        System.out.println("| 2. Reservation Policy:                                                  |");
        System.out.println("| - All reservations require admin approval.                              |");
        System.out.println("| - Reservations are subject to item availability.                        |");
        System.out.println("| - False or fraudulent reservations will result in account suspension.   |");
        System.out.println("|                                                                         |");
        System.out.println("| 3. Payment and Pickup:                                                  |");
        System.out.println("| - Payment must be made upon pickup of reserved items.                   |");
        System.out.println("| - Accepted payment methods: Cash only.                                  |");
        System.out.println("| - Students must present valid ID during pickup.                         |");
        System.out.println("| - Unclaimed items after 7 days will be returned to inventory.           |");
        System.out.println("|                                                                         |");
        System.out.println("| 4. Cancellation Policy:                                                 |");
        System.out.println("| - Reservations can be cancelled before approval.                        |");
        System.out.println("| - Approved reservations require admin permission to cancel.             |");
        System.out.println("| - No refunds for completed transactions.                                |");
        System.out.println("|                                                                         |");
        System.out.println("| 5. System Usage:                                                        |");
        System.out.println("| - The system is for STI students only.                                  |");
        System.out.println("| - Course-specific items are restricted to enrolled students.            |");
        System.out.println("| - Misuse of the system may result in account termination.               |");
        System.out.println("|                                                                         |");
        System.out.println("| 6. Privacy and Data:                                                    |");
        System.out.println("| - Your information is used solely for order processing.                 |");
        System.out.println("| - Data will not be shared with unauthorized third parties.              |");
        System.out.println("| - You consent to data collection for system functionality.              |");
        System.out.println("|                                                                         |");
        System.out.println("| 7. Amendments:                                                          |");
        System.out.println("| - These terms may be updated without prior notice.                      |");
        System.out.println("| - Continued use of the system constitutes acceptance of new terms.      |");
        System.out.println("|                                                                         |");
        System.out.println("| 8. Contact Information:                                                 |");
        System.out.println("| - For questions or concerns, contact the admin through the system.      |");
        System.out.println("| - Report technical issues to the IT department.                         |");
        System.out.println("=".repeat(75));
        System.out.println("| By proceeding, you acknowledge that you have read, understood, and      |");
        System.out.println("| agree to be bound by these Terms and Conditions.                        |");
        System.out.println("=".repeat(75));
    }
    
    public static boolean acceptTerms(InputValidator validator) {
        displayTermsAndConditions();
        System.out.println("\nDo you accept these Terms and Conditions?");
        return validator.getValidYesNo("Enter your choice");
    }
}
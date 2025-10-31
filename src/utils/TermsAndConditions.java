package utils;

public class TermsAndConditions {

    public static String getTermsAndConditionsText() {
        StringBuilder terms = new StringBuilder();
        terms.append("TERMS AND CONDITIONS\n\n");
        terms.append("These Terms and Conditions govern the use of the STI ProWear System.\n");
        terms.append("By creating an account, you ('Student') agree to comply with the following terms:\n\n");

        terms.append("1. Account Responsibility:\n");
        terms.append("   - You are responsible for maintaining the confidentiality of your account credentials.\n");
        terms.append("   - You must provide accurate and current information.\n");
        terms.append("   - Only one account per student is allowed.\n\n");

        terms.append("2. Reservation Policy:\n");
        terms.append("   - All reservations require admin approval.\n");
        terms.append("   - Reservations are subject to item availability.\n");
        terms.append("   - False or fraudulent reservations will result in account suspension.\n\n");

        terms.append("3. Payment and Pickup:\n");
        terms.append("   - Payment must be made upon pickup of reserved items.\n");
        terms.append("   - Accepted payment methods: Cash only.\n");
        terms.append("   - Students must present valid ID during pickup.\n");
        terms.append("   - Unclaimed items after 7 days will be returned to inventory.\n\n");

        terms.append("4. Cancellation Policy:\n");
        terms.append("   - Reservations can be cancelled before approval.\n");
        terms.append("   - Approved reservations require admin permission to cancel.\n");
        terms.append("   - No refunds for completed transactions.\n\n");

        terms.append("5. System Usage:\n");
        terms.append("   - The system is for STI students only.\n");
        terms.append("   - Course-specific items are restricted to enrolled students.\n");
        terms.append("   - Misuse of the system may result in account termination.\n\n");

        terms.append("6. Privacy and Data:\n");
        terms.append("   - Your information is used solely for order processing.\n");
        terms.append("   - Data will not be shared with unauthorized third parties.\n");
        terms.append("   - You consent to data collection for system functionality.\n\n");

        terms.append("7. Amendments:\n");
        terms.append("   - These terms may be updated without prior notice.\n");
        terms.append("   - Continued use of the system constitutes acceptance of new terms.\n\n");

        terms.append("8. Contact Information:\n");
        terms.append("   - For questions or concerns, contact the admin through the system.\n");
        terms.append("   - Report technical issues to the IT department.\n\n");

        terms.append("By proceeding, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions.");

        return terms.toString();
    }
}
package cl.sonidovivo.backend.util;

public final class RutValidator {

    private RutValidator() {
    }

    public static boolean isValid(String rut) {
        if (rut == null || rut.isBlank()) {
            return false;
        }
        String clean = rut.replaceAll("[^0-9kK]", "").toUpperCase();
        if (clean.length() < 8 || clean.length() > 9) {
            return false;
        }
        String body = clean.substring(0, clean.length() - 1);
        char dv = clean.charAt(clean.length() - 1);

        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * multiplier;
            multiplier = (multiplier == 7) ? 2 : multiplier + 1;
        }

        int remainder = sum % 11;
        int expected = 11 - remainder;
        char calculatedDv;
        if (expected == 11) {
            calculatedDv = '0';
        } else if (expected == 10) {
            calculatedDv = 'K';
        } else {
            calculatedDv = Character.forDigit(expected, 10);
        }

        return dv == calculatedDv;
    }
}

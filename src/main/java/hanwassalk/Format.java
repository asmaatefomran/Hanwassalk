package hanwassalk;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Small helper so numbers print nicely: 2.0 -> "2", 5.700000000000001 -> "5.7". */
public class Format {
    public static String number(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
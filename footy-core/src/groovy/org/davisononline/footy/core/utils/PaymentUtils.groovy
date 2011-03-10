package org.davisononline.footy.core.utils

/**
 */
class PaymentUtils {

    /**
     * WTF does payment not do this itself?
     *
     * @param Payment
     * @return
     */

    static calculateSubTotal(payment) {
        if (!payment?.paymentItems || payment.paymentItems.size() == 0)
            return 0

        // sub total items
        def tot = 0
        payment.paymentItems.each { item ->
            tot += (item.amount * item.quantity)
        }
        tot
    }

    static calculateTax(payment) {
        def act = calculateSubTotal(payment) * (payment.tax)
        Math.round(act * 100) / 100
    }

    static calculateTotal(payment) {
        def act = calculateSubTotal(payment) * (payment.tax + 1)
        Math.round(act * 100) / 100
    }

}
package io.neow3j.utils;

import io.reactivex.Observable;

import java.math.BigInteger;

/**
 * Observable utility functions.
 */
public class Observables {

    public static Observable<BigInteger> range(
            final BigInteger startValue, final BigInteger endValue) {
        return range(startValue, endValue, true);
    }

    /**
     * Simple Observable implementation to emit a range of BigInteger values.
     *
     * @param startValue first value to emit in range
     * @param endValue   final value to emit in range
     * @param ascending  direction to iterate through range
     * @return Observable to emit this range of values
     */
    public static Observable<BigInteger> range(
            final BigInteger startValue, final BigInteger endValue, final boolean ascending) {
        if (startValue.compareTo(BigInteger.ZERO) == -1) {
            throw new IllegalArgumentException("Negative start index cannot be used");
        } else if (startValue.compareTo(endValue) > 0) {
            throw new IllegalArgumentException(
                    "Negative start index cannot be greater then end index");
        }

        if (ascending) {
            return Observable.create(subscriber -> {
                for (BigInteger i = startValue;
                     i.compareTo(endValue) < 1
                             && !subscriber.isDisposed();
                     i = i.add(BigInteger.ONE)) {
                    subscriber.onNext(i);
                }

                if (!subscriber.isDisposed()) {
                    subscriber.onComplete();
                }
            });
        } else {
            return Observable.create(subscriber -> {
                for (BigInteger i = endValue;
                     i.compareTo(startValue) > -1
                             && !subscriber.isDisposed();
                     i = i.subtract(BigInteger.ONE)) {
                    subscriber.onNext(i);
                }

                if (!subscriber.isDisposed()) {
                    subscriber.onComplete();
                }
            });
        }
    }
}

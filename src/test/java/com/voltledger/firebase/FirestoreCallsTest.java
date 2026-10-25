package com.voltledger.firebase;

import com.google.api.core.ApiFutures;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FirestoreCallsTest {
    @Test
    void preservesDuplicateKeyExceptionFromTransaction() {
        var future = ApiFutures.<Object>immediateFailedFuture(
            new DuplicateKeyException("Consumer ID or meter number already registered"));
        assertThrows(DuplicateKeyException.class, () -> FirestoreCalls.await(future));
    }

    @Test
    void preservesWrappedDuplicateKeyExceptionFromTransaction() {
        var future = ApiFutures.<Object>immediateFailedFuture(
            new IllegalStateException("Transaction callback failed",
                new DuplicateKeyException("Consumer ID or meter number already registered")));
        assertThrows(DuplicateKeyException.class, () -> FirestoreCalls.await(future));
    }
}

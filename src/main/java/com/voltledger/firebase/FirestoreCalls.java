package com.voltledger.firebase;

import com.google.api.core.ApiFuture;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.dao.DuplicateKeyException;

/**
 * Wait for Firestore operations while preserving recoverable duplicate-record errors.
 * Firestore transactions can wrap a callback's DuplicateKeyException in another
 * exception, so callers must be able to recognize it to show a form error.
 */
final class FirestoreCalls {
    private FirestoreCalls() { }

    static <T> T await(ApiFuture<T> future) {
        try {
            return future.get(25, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore request interrupted", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            // Unwrap Firestore transaction errors so MVC controllers can handle
            // duplicate consumer/meter identifiers without returning HTTP 500.
            for (Throwable nested = cause; nested != null; nested = nested.getCause()) {
                if (nested instanceof DuplicateKeyException duplicate) {
                    throw duplicate;
                }
                if (nested instanceof AlreadyExistsException ||
                    (nested instanceof ApiException api && api.getStatusCode() != null &&
                     api.getStatusCode().getCode() == StatusCode.Code.ALREADY_EXISTS)) {
                    throw new DuplicateKeyException("Record already exists", nested);
                }
                if (nested == nested.getCause()) break;
            }
            throw new IllegalStateException("Firestore request unsuccessful", cause);
        } catch (TimeoutException ex) {
            throw new IllegalStateException("Firestore request timed out", ex);
        }
    }
}

package previewcode.backend.api.filter;

/**
 * Build a JWT Token from the integration ID.
 * This interface is used in tests to stub the signing algorithm.
 */
@FunctionalInterface
public interface IJWTTokenCreator {
    String create(String integrationId);
}
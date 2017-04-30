package previewcode.backend.api.exceptionmapper;

import javax.ws.rs.ext.Provider;

/**
 * Catches all Throwables and lets AbstractExceptionMapper create an default response.
 */
@Provider
public class RootExceptionMapper extends AbstractExceptionMapper<Throwable> { }

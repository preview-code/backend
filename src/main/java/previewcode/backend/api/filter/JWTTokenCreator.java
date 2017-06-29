package previewcode.backend.api.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

/**
 * Build a JWT Token from the integration ID.
 * Used to authenticate GitHub installations.
 */
public class JWTTokenCreator implements IJWTTokenCreator {

    private final Algorithm jwtSigningAlgorithm;

    @Inject
    public JWTTokenCreator(Algorithm jwtSigningAlgorithm) {
        this.jwtSigningAlgorithm = jwtSigningAlgorithm;
    }

    @Override
    public String create(String integrationId) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10);
        Date exp = calendar.getTime();

        return JWT.create()
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withIssuer(integrationId)
                .sign(jwtSigningAlgorithm);
    }
}

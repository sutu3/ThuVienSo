package org.example.thuvienso.Service.Impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.*;
import org.example.thuvienso.Dto.Response.AuthenticationResponse;
import org.example.thuvienso.Dto.Response.IntrospectResponse;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Module.AccountEntity;
import org.example.thuvienso.Module.InvalidateTokenEntity;
import org.example.thuvienso.Repo.AccountRepo;
import org.example.thuvienso.Repo.InvalidateTokenRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    AccountRepo accountRepo;
    InvalidateTokenRepo invalidateTokenRepo;

    @NonFinal
    @Value("aGUDzL2OOlvQcHJE5fpfzxv0w6MUZtFhF4IRZo+Tc/z5FnKjn8xxJvoot4OVj6LP")
    protected String SIGN_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long REFRESH_DURATION;
    public IntrospectResponse introspect(IntrospectRequest request)  {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var account = accountRepo
                .findByUserName(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(account,VALID_DURATION);

        return AuthenticationResponse.builder().token(token).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidateTokenEntity invalidatedToken =
                    InvalidateTokenEntity.builder().id(jit).expiryTime(expiryTime).build();

            invalidateTokenRepo.save(invalidatedToken);
        } catch (AppException exception){
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidateTokenEntity invalidatedToken =
                InvalidateTokenEntity.builder().id(jit).expiryTime(expiryTime).build();

        invalidateTokenRepo.save(invalidatedToken);

        var userName = signedJWT.getJWTClaimsSet().getClaim("userName").toString();

        var user =
                accountRepo.findByUserName(userName).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user,VALID_DURATION);

        return AuthenticationResponse.builder().token(token).build();
    }

    private String generateToken(AccountEntity account, Long Duration) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getIdAccount())
                .issuer("")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(Duration, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account))
                .claim("userName", account.getUserName())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidateTokenRepo.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }
    public Boolean checkUserByToken(String token,String idUser) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(token);
        String Iduser=signedJWT.getJWTClaimsSet().getSubject();
        if(Iduser.matches(idUser)){
            return true;
        }
        return false;
    }
    //    public AuthenticationResponse forgotPassword(String email) {
//        User user=userRepo.findByEmailWithRoles(email)
//                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
//        NotificationForgotPassword notification = NotificationForgotPassword.builder()
//                .to(user.getEmail())
//                .toName(user.getUsername())
//                .content("Reset password")
//                .resetLink("http://localhost:5173/reset-password?user="+user.getIdUser())
//                .subject("Reset password")
//                .build();
//
//        try {
//            javaMailSenderService.javaSendMailForgotPassword(notification);
//        } catch (Exception e) {
//            log.error("Error sending notification: {}", e.getMessage(), e);
//            throw new AppException(ErrorCode.NOTIFICATION_SEND_FAILED);
//        }
//        var token = generateToken(user, 10 * 60L);
//
//        return AuthenticationResponse.builder().token(token).build();
//    }
//    public AuthenticationResponse resetPassword(ResetPasswordRequest request) {
//        User user = userRepo.findById(request.userId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        if (!request.newPassword().equals(request.newPassword())) {
//            throw new AppException(ErrorCode.PASSWORD_INVALID);
//        }
//
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
//        user.setPassword(passwordEncoder.encode(request.newPassword()));
//        userRepo.save(user);
//        var token = generateToken(user,VALID_DURATION);
//
//        return AuthenticationResponse.builder().token(token).build();
//    }
    public void changePassword(NewPasswordRequest request) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String scope = jwt.getClaim("sub");
        AccountEntity user = accountRepo.findById(scope)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if(passwordEncoder.matches(
                request.getMatKhau(),
                user.getPassword()
        )) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        user.setPassword(passwordEncoder.encode(request.getMatKhau()));
        accountRepo.save(user);

        ;
    }
    private String buildScope(AccountEntity account) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (account.getRoleEntity() != null)
        {

                stringJoiner.add("ROLE_" + account.getRoleEntity().getRoleName());


        }

        return stringJoiner.toString();
    }

    private record TokenInfo(String token, Date expiryDate) {}
}

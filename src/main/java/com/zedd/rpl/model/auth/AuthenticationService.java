package com.zedd.rpl.model.auth;


import com.zedd.rpl.enums.Role;
import com.zedd.rpl.model.auth.req.AuthenticationRequest;
import com.zedd.rpl.model.auth.req.RefreshRequest;
import com.zedd.rpl.model.auth.res.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest req);
}

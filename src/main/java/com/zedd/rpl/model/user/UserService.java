package com.zedd.rpl.model.user;

import com.zedd.rpl.model.user.req.ChangePasswordRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface UserService extends UserDetailsService {
  void changePassword(ChangePasswordRequest request, Long userId);

}

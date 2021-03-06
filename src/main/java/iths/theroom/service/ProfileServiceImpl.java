package iths.theroom.service;

import iths.theroom.entity.UserEntity;
import iths.theroom.exception.UnauthorizedException;
import iths.theroom.factory.ProfileFactory;
import iths.theroom.model.ProfileModel;
import iths.theroom.pojos.ProfileForm;
import iths.theroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserPrincipalDetailsService userPrincipalDetailsService;

    @Autowired
    public ProfileServiceImpl(UserRepository userRepository, UserService userService, UserPrincipalDetailsService userPrincipalDetailsService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userPrincipalDetailsService = userPrincipalDetailsService;
    }

    @Override
    public ProfileModel getProfile(String email) {
        return null;
    }

    @Override
    public ProfileModel getProfiles() {
        return null;
    }

    @Override
    public ProfileModel save(ProfileForm form, HttpServletRequest req) {

        UserDetails userDetails = userPrincipalDetailsService.loadUserByUsername(form.getUsername());
        UserEntity user = userService.getByUserName(userDetails.getUsername());
        user.getProfile().setAboutMe(form.getAboutMe());
        user.getProfile().setAge(form.getAge());
        user.getProfile().setCountry(form.getCountry());
        user.getProfile().setGender(form.getGender());
        user.getProfile().setStarSign(form.getStarSign());

        if(!userService.isUserWhoItClaimsToBe(userDetails.getUsername(), req)){
            throw new UnauthorizedException("Invalid credentials");
        }
        return ProfileFactory.toModel(userService.updateUser(user).getProfile());
    }

    @Override
    public ProfileModel setAboutMe(String email, String aboutMe) {
        return null;
    }

    @Override
    public ProfileModel setGender(String email, String gender) {
        return null;
    }

    @Override
    public ProfileModel setAge(String email, int age) {
        return null;
    }

    @Override
    public ProfileModel setCountry(String email, String country) {
        return null;
    }

    @Override
    public ProfileModel setStarSign(String email, String starSign) {
        return null;
    }

    @Override
    public ProfileModel incrementVisitor(String email) {
        return null;
    }
}


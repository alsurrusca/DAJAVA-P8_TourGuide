package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;

@Service
public class UserPreferencesService {

    @Autowired
    private UserService userService;

    private final Logger log = LoggerFactory.getLogger(UserPreferencesService.class);



    public UserPreferences userUpdatePreferences (String username, UserPreferencesDTO userPreferencesDTO ) {
        User user = userService.getUsersByUsername(username);
        user.setUserPreferences(new UserPreferences(userPreferencesDTO));
        return user.getUserPreferences();
    }
}

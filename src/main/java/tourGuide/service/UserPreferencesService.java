package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.DTO.UserPreferencesDTO;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;

@Service
public class UserPreferencesService {

    @Autowired
    private UserService userService;


    public UserPreferences userUpdatePreferences (String username, UserPreferencesDTO userPreferencesDTO ) {
        User user = userService.getUsersByUsername(username);
        user.setUserPreferences(new UserPreferences(userPreferencesDTO));
        return user.getUserPreferences();
    }
}

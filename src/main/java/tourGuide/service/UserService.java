package tourGuide.service;

import tourGuide.model.User;

import java.util.List;

public interface UserService {

    /**
     * Get User with username
     * @param username
     * @return a user if exist
     */
    User getUser(String username);

    /**
     * Return list of all user
     * @return list of all users
     */
    List<User> getAllUser();

    /**
     * Add new User
     * @param user
     */
    void addUser(User user);

    /**
     * Creates test users.
     */
    void initializeInternalUsers();


}

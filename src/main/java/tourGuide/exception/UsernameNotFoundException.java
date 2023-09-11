package tourGuide.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String username){
        super("Username not found" + username);
        Logger log = LoggerFactory.getLogger(UserPreferencesNotFoundException.class);
        log.error("Username not found" + username);
    }
}

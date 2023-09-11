package tourGuide.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserPreferencesNotFoundException extends RuntimeException{

    public UserPreferencesNotFoundException(){
        super("User preferences is empty");
        Logger log = LoggerFactory.getLogger(UserPreferencesNotFoundException.class);
        log.error("User preferences is empty");
    }
}
